/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.render

import com.mojang.blaze3d.systems.RenderSystem
import dev.lyzev.api.events.*
import dev.lyzev.api.opengl.WrappedFramebuffer
import dev.lyzev.api.opengl.clear
import dev.lyzev.api.opengl.shader.*
import dev.lyzev.api.opengl.shader.Shader.Companion.drawFullScreen
import dev.lyzev.api.opengl.shader.blur.BlurHelper
import dev.lyzev.api.setting.settings.*
import dev.lyzev.api.settings.Setting.Companion.neq
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.ModuleToggleable
import dev.lyzev.schizoid.injection.accessor.WorldRendererAccessor
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.BufferAllocator
import net.minecraft.entity.LivingEntity
import net.minecraft.registry.Registries
import net.minecraft.util.math.MathHelper
import org.joml.Vector2f
import org.lwjgl.opengl.GL13.GL_TEXTURE0
import org.lwjgl.opengl.GL13.GL_TEXTURE1
import org.lwjgl.opengl.GL43
import java.awt.Color
import kotlin.math.*

object ModuleToggleableESP : ModuleToggleable("ESP", "Extra Sensory Perception.", category = IFeature.Category.RENDER),
    EventListener {

    private val entitiesFbo = WrappedFramebuffer("[ESP] Entities", useDepth = true, linear = false)
    private val entitiesVertexConsumer = VertexConsumerProvider.immediate(BufferAllocator(1536))

    private val entitiesHurtTimeFbo = WrappedFramebuffer("[ESP] Entities Hurttime", useDepth = true, linear = false)

    private val outlines = WrappedFramebuffer("[ESP] Outlines", internalFormat = GL43.GL_RGBA16, linear = false)
    private val fbos = arrayOf(
        WrappedFramebuffer("[ESP] FBO 0"),
        WrappedFramebuffer("[ESP] FBO 1")
    )
    private val jumpFloodFbos = arrayOf(
        WrappedFramebuffer("[ESP] FBO 0", lod = 1, internalFormat = GL43.GL_RGBA16, linear = false),
        WrappedFramebuffer("[ESP] FBO 1", lod = 1, internalFormat = GL43.GL_RGBA16, linear = false)
    )
    private val texelSize = Vector2f()
    private val screenSize = Vector2f()

    private val jumpFloodSteps = mutableListOf(4, 2, 1)

    val entities = multiOption("Entities",
        "The entities to render the esp on.",
        Registries.ENTITY_TYPE.map { it.name.string to (it.name.string == "Player") }.sortedBy { it.first }.toSet()
    )
    val hurtTime by switch("Hurt Time Color", "Make the outline color based on hurt time.", false)
    val throughWall by switch("Through Wall", "Render entities through walls.", false)
    val alphaOcclusion by switch("Alpha Occlusion", "Enable alpha occlusion.", false)
    val visibleColor by color(
        "Visible Color", "Color of visible entities.", Color(0, 255, 0, 120), true, hide = ::alphaOcclusion neq true
    )
    val invisibleColor by color(
        "Invisible Color", "Color of invisible entities.", Color(255, 0, 0, 120), true, hide = ::alphaOcclusion neq true
    )
    val outline by switch("Outline", "Render an outline around entities.", true)

    val solid by switch("Solid", "Render a solid outline.", true, hide = ::outline neq true) {
        updateJumpFloodSteps()
    }
    val solidColor = color("Solid Outline Color",
        "Color of the solid outline.",
        Color.GREEN,
        useAlpha = true,
        useRGBPuke = true,
        hide = {
            !outline || !solid
        })
    val solidHurtTimeColor = color(
        "Solid Hurt Time Color",
        "Color of the hurt time.",
        Color.RED,
        useAlpha = true,
        useRGBPuke = true,
        hide = {
            !outline || !solid || !hurtTime
        }
    )
    val solidLength by slider("Solid Outline Length", "Length of the solid outline.", 7, 1, 256, "px", hide = {
        !outline || !solid
    }) {
        updateJumpFloodSteps()
    }
    val solidBlur by switch("Solid Outline Blur", "Blur the outline.", false, hide = {
        !outline || !solid
    })
    val solidBlurStrength by slider("Solid Outline Blur Strength", "Strength of the outline blur.", 6, 1, 20, hide = {
        !outline || !solid || !solidBlur
    })
    val solidBlurCutout by switch("Solid Outline Blur Cutout", "Cutout the blur.", false, hide = {
        !outline || !solid || !solidBlur
    })
    val solidBlurAlphaMultiplier by slider("Solid Outline Blur Alpha Multiplier",
        "Multiplier for the alpha of the outline blur.",
        170,
        0,
        200,
        hide = {
            !outline || !solid || !solidBlur
        })

    val smooth by switch("Smooth", "Render a smooth outline.", true, hide = ::outline neq true) {
        updateJumpFloodSteps()
    }
    val smoothColor = color("Smooth Outline Color",
        "Color of the smooth outline.",
        Color.GREEN,
        useAlpha = true,
        useRGBPuke = true,
        hide = {
            !outline || !smooth
        })
    val smoothHurtTimeColor = color(
        "Smooth Hurt Time Color",
        "Color of the hurt time.",
        Color.RED,
        useAlpha = true,
        useRGBPuke = true,
        hide = {
            !outline || !smooth || !hurtTime
        }
    )
    val smoothLength by slider("Smooth Outline Length", "Length of the smooth outline.", 7, 1, 256, "px", hide = {
        !outline || !smooth
    }) {
        updateJumpFloodSteps()
    }
    val smoothBlur by switch("Smooth Outline Blur", "Blur the outline.", false, hide = {
        !outline || !smooth
    })
    val smoothBlurStrength by slider("Smooth Outline Blur Strength", "Strength of the outline blur.", 6, 1, 20, hide = {
        !outline || !smooth || !smoothBlur
    })
    val smoothBlurCutout by switch("Smooth Outline Blur Cutout", "Cutout the blur.", false, hide = {
        !outline || !smooth || !smoothBlur
    })
    val smoothBlurAlphaMultiplier by slider("Smooth Outline Blur Alpha Multiplier",
        "Multiplier for the alpha of the outline blur.",
        170,
        0,
        200,
        hide = {
            !outline || !smooth || !smoothBlur
        })

    private fun updateJumpFloodSteps() {
        jumpFloodSteps.clear()

        val length = max(if (solid) solidLength else 2, if (smooth) smoothLength else 2)

        // Calculate the amount of steps needed for the jump flood algorithm
        // See: https://en.wikipedia.org/wiki/Jump_flooding_algorithm#Implementation
        var steps = 2.0.pow(ceil(log2(length.toDouble())))

        while (steps >= 1) {
            jumpFloodSteps.add(steps.toInt())
            steps = floor(steps / 2)
        }
    }

    override val shouldHandleEvents: Boolean
        get() = isEnabled

    init {
        fun renderOutline(source: WrappedFramebuffer, solidColor: SettingClientColor, smoothColor: SettingClientColor) {
            if (solid) {
                fbos[jumpFloodSteps.size % 2].clear()
                fbos[jumpFloodSteps.size % 2].beginWrite(false)
                ShaderOutlineSolid.bind()
                RenderSystem.activeTexture(GL_TEXTURE0)
                outlines.beginRead()
                ShaderOutlineSolid["Tex0"] = 0
                ShaderOutlineSolid["Length"] = sqrt(solidLength * solidLength + solidLength * solidLength.toFloat())
                ShaderOutlineSolid["ScreenSize"] =
                    screenSize.set(mc.framebuffer.textureWidth.toFloat(), mc.framebuffer.textureHeight.toFloat())
                drawFullScreen()
                ShaderOutlineSolid.unbind()

                if (solidBlur && solidBlurCutout) {
                    BlurHelper.mode.switchStrength(solidBlurStrength)
                    BlurHelper.mode.render(fbos[jumpFloodSteps.size % 2].colorAttachment, true)
                }

                fbos[(jumpFloodSteps.size - 1) % 2].clear()
                fbos[(jumpFloodSteps.size - 1) % 2].beginWrite(false)
                ShaderMask.bind()
                RenderSystem.activeTexture(GL_TEXTURE1)
                source.beginRead()
                RenderSystem.activeTexture(GL_TEXTURE0)
                if (solidBlur && solidBlurCutout) {
                    BlurHelper.mode.output.beginRead()
                } else {
                    fbos[jumpFloodSteps.size % 2].beginRead()
                }
                ShaderMask["Tex0"] = 0
                ShaderMask["Tex1"] = 1
                ShaderMask["Invert"] = true
                drawFullScreen()
                ShaderMask.unbind()

                if (solidBlur && !solidBlurCutout) {
                    BlurHelper.mode.switchStrength(solidBlurStrength)
                    BlurHelper.mode.render(fbos[(jumpFloodSteps.size - 1) % 2].colorAttachment, true)
                }

                mc.framebuffer.beginWrite(false)
                ShaderTint.bind()
                RenderSystem.activeTexture(GL_TEXTURE0)
                if (solidBlur && !solidBlurCutout) {
                    BlurHelper.mode.output.beginRead()
                } else {
                    fbos[(jumpFloodSteps.size - 1) % 2].beginRead()
                }
                ShaderTint["Tex0"] = 0
                ShaderTint["Color"] = solidColor.value
                ShaderTint["RGBPuke"] = solidColor.isRGBPuke
                ShaderTint["Opacity"] = 1f
                ShaderTint.set("SV", solidColor.saturation / 100f, solidColor.brightness / 100f)
                ShaderTint["Opacity"] = 1f
                ShaderTint["Alpha"] = true
                ShaderTint["Multiplier"] =
                    if (solidBlur) solidBlurAlphaMultiplier / 100f else solidColor.value.alpha / 255f
                ShaderTint["Time"] = (System.nanoTime() - ShaderTint.initTime) / 1000000000f
                ShaderTint["Yaw"] = mc.player?.yaw ?: 0f
                ShaderTint["Pitch"] = mc.player?.pitch ?: 0f
                drawFullScreen()
                ShaderTint.unbind()
            }

            if (smooth) {
                fbos[jumpFloodSteps.size % 2].clear()
                fbos[jumpFloodSteps.size % 2].beginWrite(false)
                ShaderOutlineSmooth.bind()
                RenderSystem.activeTexture(GL_TEXTURE0)
                outlines.beginRead()
                ShaderOutlineSmooth["Tex0"] = 0
                ShaderOutlineSmooth["Length"] = sqrt(smoothLength * smoothLength + smoothLength * smoothLength.toFloat())
                ShaderOutlineSmooth["ScreenSize"] =
                    screenSize.set(mc.framebuffer.textureWidth.toFloat(), mc.framebuffer.textureHeight.toFloat())
                drawFullScreen()
                ShaderOutlineSmooth.unbind()

                if (smoothBlur && smoothBlurCutout) {
                    BlurHelper.mode.switchStrength(smoothBlurStrength)
                    BlurHelper.mode.render(fbos[jumpFloodSteps.size % 2].colorAttachment, true)
                }

                fbos[(jumpFloodSteps.size - 1) % 2].clear()
                fbos[(jumpFloodSteps.size - 1) % 2].beginWrite(false)
                ShaderMask.bind()
                RenderSystem.activeTexture(GL_TEXTURE1)
                source.beginRead()
                RenderSystem.activeTexture(GL_TEXTURE0)
                if (smoothBlur && smoothBlurCutout) {
                    BlurHelper.mode.output.beginRead()
                } else {
                    fbos[jumpFloodSteps.size % 2].beginRead()
                }
                ShaderMask["Tex0"] = 0
                ShaderMask["Tex1"] = 1
                ShaderMask["Invert"] = true
                drawFullScreen()
                ShaderMask.unbind()

                if (smoothBlur && !smoothBlurCutout) {
                    BlurHelper.mode.switchStrength(smoothBlurStrength)
                    BlurHelper.mode.render(fbos[(jumpFloodSteps.size - 1) % 2].colorAttachment, true)
                }

                mc.framebuffer.beginWrite(false)
                ShaderTint.bind()
                RenderSystem.activeTexture(GL_TEXTURE0)
                if (smoothBlur && !smoothBlurCutout) {
                    BlurHelper.mode.output.beginRead()
                } else {
                    fbos[(jumpFloodSteps.size - 1) % 2].beginRead()
                }
                ShaderTint["Tex0"] = 0
                ShaderTint["Color"] = smoothColor.value
                ShaderTint["RGBPuke"] = smoothColor.isRGBPuke
                ShaderTint["Opacity"] = 1f
                ShaderTint.set("SV", smoothColor.saturation / 100f, smoothColor.brightness / 100f)
                ShaderTint["Opacity"] = 1f
                ShaderTint["Alpha"] = true
                ShaderTint["Multiplier"] =
                    if (smoothBlur) smoothBlurAlphaMultiplier / 100f else smoothColor.value.alpha / 255f
                ShaderTint["Time"] = (System.nanoTime() - ShaderTint.initTime) / 1000000000f
                ShaderTint["Yaw"] = mc.player?.yaw ?: 0f
                ShaderTint["Pitch"] = mc.player?.pitch ?: 0f
                drawFullScreen()
                ShaderTint.unbind()
            }
        }

        on<EventRenderWorld>(Event.Priority.LOWEST) {
            RenderSystem.disableDepthTest()

            if (alphaOcclusion) {
                outlines.clear()
                outlines.beginWrite(false)
                ShaderAlphaOcclusion.bind()
                RenderSystem.activeTexture(GL_TEXTURE1)
                mc.framebuffer.beginRead()
                ShaderAlphaOcclusion["Tex1"] = 1
                RenderSystem.activeTexture(GL_TEXTURE0)
                entitiesFbo.beginRead()
                ShaderAlphaOcclusion["Tex0"] = 0
                ShaderAlphaOcclusion.set(
                    "Visible",
                    visibleColor.red / 255f,
                    visibleColor.green / 255f,
                    visibleColor.blue / 255f,
                    visibleColor.alpha / 255f
                )
                ShaderAlphaOcclusion.set(
                    "Invisible",
                    invisibleColor.red / 255f,
                    invisibleColor.green / 255f,
                    invisibleColor.blue / 255f,
                    invisibleColor.alpha / 255f
                )
                drawFullScreen()
                ShaderAlphaOcclusion.unbind()
            }

            if (throughWall) {
                mc.framebuffer.beginWrite(false)
                ShaderPassThrough.bind()
                RenderSystem.activeTexture(GL_TEXTURE0)
                entitiesFbo.beginRead()
                ShaderPassThrough["Tex0"] = 0
                ShaderPassThrough["Scale"] = 1f
                ShaderPassThrough["Alpha"] = true
                drawFullScreen()
                ShaderPassThrough.unbind()

                if (hurtTime) {
                    mc.framebuffer.beginWrite(false)
                    ShaderPassThrough.bind()
                    RenderSystem.activeTexture(GL_TEXTURE0)
                    entitiesHurtTimeFbo.beginRead()
                    ShaderPassThrough["Tex0"] = 0
                    ShaderPassThrough["Scale"] = 1f
                    ShaderPassThrough["Alpha"] = true
                    drawFullScreen()
                    ShaderPassThrough.unbind()
                }
            }

            if (alphaOcclusion) {
                mc.framebuffer.beginWrite(false)
                ShaderMask.bind()
                RenderSystem.activeTexture(GL_TEXTURE1)
                entitiesFbo.beginRead()
                RenderSystem.activeTexture(GL_TEXTURE0)
                outlines.beginRead()
                ShaderMask["Tex0"] = 0
                ShaderMask["Tex1"] = 1
                ShaderMask["Invert"] = false
                drawFullScreen()
                ShaderMask.unbind()
            }

            if (outline) {
                jumpFloodFbos[1].clear()
                jumpFloodFbos[1].beginWrite(true)
                ShaderJumpFloodInit.bind()
                RenderSystem.activeTexture(GL_TEXTURE0)
                entitiesFbo.beginRead()
                ShaderJumpFloodInit["Tex0"] = 0
                drawFullScreen()
                ShaderJumpFloodInit.unbind()

                for (i in 0 until jumpFloodSteps.size) {
                    val targetFbo = jumpFloodFbos[i % 2]
                    targetFbo.clear()
                    targetFbo.beginWrite(true)
                    ShaderJumpFlood.bind()
                    RenderSystem.activeTexture(GL_TEXTURE0)
                    jumpFloodFbos[(i + 1) % 2].beginRead()
                    ShaderJumpFlood["Tex0"] = 0
                    ShaderJumpFlood["Length"] = jumpFloodSteps[i]
                    ShaderJumpFlood["TexelSize"] = texelSize.set(1f / targetFbo.textureWidth, 1f / targetFbo.textureHeight)
                    drawFullScreen()
                    ShaderJumpFlood.unbind()
                }

                outlines.clear()
                outlines.beginWrite(true)
                ShaderJumpFlood.bind()
                RenderSystem.activeTexture(GL_TEXTURE0)
                jumpFloodFbos[(jumpFloodSteps.size - 1) % 2].beginRead()
                ShaderJumpFlood["Tex0"] = 0
                ShaderJumpFlood["Length"] = 1
                ShaderJumpFlood["TexelSize"] = texelSize.set(1f / jumpFloodFbos[(jumpFloodSteps.size - 1) % 2].textureWidth, 1f / jumpFloodFbos[(jumpFloodSteps.size - 1) % 2].textureHeight)
                drawFullScreen()
                ShaderJumpFlood.unbind()

                renderOutline(entitiesFbo, solidColor, smoothColor)

                if (hurtTime) {
                    jumpFloodFbos[1].clear()
                    jumpFloodFbos[1].beginWrite(true)
                    ShaderJumpFloodInit.bind()
                    RenderSystem.activeTexture(GL_TEXTURE0)
                    entitiesHurtTimeFbo.beginRead()
                    ShaderJumpFloodInit["Tex0"] = 0
                    drawFullScreen()
                    ShaderJumpFloodInit.unbind()

                    for (i in 0 until jumpFloodSteps.size) {
                        val targetFbo = jumpFloodFbos[i % 2]
                        targetFbo.clear()
                        targetFbo.beginWrite(true)
                        ShaderJumpFlood.bind()
                        RenderSystem.activeTexture(GL_TEXTURE0)
                        jumpFloodFbos[(i + 1) % 2].beginRead()
                        ShaderJumpFlood["Tex0"] = 0
                        ShaderJumpFlood["Length"] = jumpFloodSteps[i]
                        ShaderJumpFlood["TexelSize"] = texelSize.set(1f / targetFbo.textureWidth, 1f / targetFbo.textureHeight)
                        drawFullScreen()
                        ShaderJumpFlood.unbind()
                    }

                    outlines.clear()
                    outlines.beginWrite(true)
                    ShaderJumpFlood.bind()
                    RenderSystem.activeTexture(GL_TEXTURE0)
                    jumpFloodFbos[(jumpFloodSteps.size - 1) % 2].beginRead()
                    ShaderJumpFlood["Tex0"] = 0
                    ShaderJumpFlood["Length"] = 1
                    ShaderJumpFlood["TexelSize"] = texelSize.set(1f / jumpFloodFbos[(jumpFloodSteps.size - 1) % 2].textureWidth, 1f / jumpFloodFbos[(jumpFloodSteps.size - 1) % 2].textureHeight)
                    drawFullScreen()
                    ShaderJumpFlood.unbind()

                    renderOutline(entitiesHurtTimeFbo, solidHurtTimeColor, smoothHurtTimeColor)
                }
            }

            entitiesFbo.clear()
            if (hurtTime) {
                entitiesHurtTimeFbo.clear()
            }
            mc.framebuffer.beginWrite(false)
        }

        var shouldHideLabel = false

        on<EventRenderEntity> { event ->
            if (!entities.enabled.contains(event.entity.type.name.string)) return@on
            val isHurtTime = hurtTime && event.entity is LivingEntity && event.entity.hurtTime > 1
            if (isHurtTime) entitiesHurtTimeFbo.beginWrite(false)
            else entitiesFbo.beginWrite(false)
            val d = MathHelper.lerp(event.tickDelta, event.entity.lastRenderX, event.entity.x)
            val e = MathHelper.lerp(event.tickDelta, event.entity.lastRenderY, event.entity.y)
            val f = MathHelper.lerp(event.tickDelta, event.entity.lastRenderZ, event.entity.z)
            val g = MathHelper.lerp(event.tickDelta.toFloat(), event.entity.prevYaw, event.entity.yaw)
            val worldRenderer = mc.worldRenderer as WorldRendererAccessor
            worldRenderer.entityRenderDispatcher.setRenderShadows(false)
            shouldHideLabel = true
            worldRenderer.entityRenderDispatcher.render(
                event.entity,
                d - event.cameraX,
                e - event.cameraY,
                f - event.cameraZ,
                g,
                event.tickDelta.toFloat(),
                event.matrices,
                entitiesVertexConsumer,
                -1 // render with full brightness
            )
            entitiesVertexConsumer.drawCurrentLayer()
            shouldHideLabel = false
            worldRenderer.entityRenderDispatcher.setRenderShadows(true)
            mc.framebuffer.beginWrite(false)
        }

        on<EventEntityHasLabel> { event ->
            if (shouldHideLabel) {
                event.hasLabel = false
            }
        }
    }
}
