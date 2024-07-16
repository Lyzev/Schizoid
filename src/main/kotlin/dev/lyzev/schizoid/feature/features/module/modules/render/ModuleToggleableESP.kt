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
import dev.lyzev.api.setting.settings.color
import dev.lyzev.api.setting.settings.multiOption
import dev.lyzev.api.setting.settings.slider
import dev.lyzev.api.setting.settings.switch
import dev.lyzev.api.settings.Setting.Companion.neq
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.gui.FeatureImGui
import dev.lyzev.schizoid.feature.features.module.ModuleToggleable
import dev.lyzev.schizoid.feature.features.module.modules.render.ModuleToggleableBlur.fogRGBPukeBrightness
import dev.lyzev.schizoid.feature.features.module.modules.render.ModuleToggleableBlur.fogRGBPukeOpacity
import dev.lyzev.schizoid.feature.features.module.modules.render.ModuleToggleableBlur.fogRGBPukeSaturation
import dev.lyzev.schizoid.injection.accessor.WorldRendererAccessor
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.BufferAllocator
import net.minecraft.registry.Registries
import net.minecraft.util.math.MathHelper
import org.joml.Vector2f
import org.lwjgl.opengl.GL13.GL_TEXTURE0
import org.lwjgl.opengl.GL13.GL_TEXTURE1
import java.awt.Color
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log2
import kotlin.math.pow

object ModuleToggleableESP : ModuleToggleable("ESP", "Extra Sensory Perception.", category = IFeature.Category.RENDER),
    EventListener {

    private val entitiesFbo = WrappedFramebuffer("[ESP] Entities", useDepth = true)
    private val outlines = WrappedFramebuffer("[ESP] Outlines")
    private val fbos = arrayOf(WrappedFramebuffer("[ESP] FBO 0"), WrappedFramebuffer("[ESP] FBO 1"))
    private val texelSize = Vector2f()
    private val vertexConsumer = VertexConsumerProvider.immediate(BufferAllocator(1536));

    private val jumpFloodSteps = mutableListOf(4, 2, 1)

    val entities by multiOption("Entities", "The entities to render the esp on.", Registries.ENTITY_TYPE.map { it.name.string to (it.name.string == "Player") }.sortedBy { it.first }.toSet())
    val throughWall by switch("Through Wall", "Render entities through walls.", false)
    val alphaOcclusion by switch("Alpha Occlusion", "Enable alpha occlusion.", false)
    val visibleColor by color(
        "Visible Color",
        "Color of visible entities.",
        Color(0, 255, 0, 120),
        true,
        hide = ::alphaOcclusion neq true
    )
    val invisibleColor by color(
        "Invisible Color",
        "Color of invisible entities.",
        Color(255, 0, 0, 120),
        true,
        hide = ::alphaOcclusion neq true
    )
    val outline by switch("Outline", "Render an outline around entities.", true)
    val outlineColor by color("Outline Color", "Color of the outline.", Color(0, 0, 0, 255), true, hide = ::outline neq true)
    val outlineLength by slider("Outline Length", "Length of the outline.", 7, 1, 100, "px", hide = ::outline neq true) {
        jumpFloodSteps.clear()

        // Calculate the amount of steps needed for the jump flood algorithm
        // See: https://en.wikipedia.org/wiki/Jump_flooding_algorithm#Implementation
        val outlineLength = it.toDouble()
        val maxSteps = ceil(log2(outlineLength)).toInt()

        var jumpDistance = floor(outlineLength / 2).toInt()

        for (i in 0 until maxSteps) {
            jumpFloodSteps += jumpDistance
            if (jumpDistance > 1) {
                jumpDistance /= 2
            }
        }
    }

    override val shouldHandleEvents: Boolean
        get() = isEnabled

    init {
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
                for (i in 0 until jumpFloodSteps.size) {
                    fbos[i % 2].clear()
                    fbos[i % 2].beginWrite(false)
                    ShaderJumpFlood.bind()
                    RenderSystem.activeTexture(GL_TEXTURE0)
                    if (i == 0) {
                        entitiesFbo.beginRead()
                    } else {
                        fbos[(i - 1) % 2].beginRead()
                    }
                    ShaderJumpFlood["Tex0"] = 0
                    ShaderJumpFlood.set("Color", outlineColor.red / 255f, outlineColor.green / 255f, outlineColor.blue / 255f, outlineColor.alpha / 255f)
                    ShaderJumpFlood["Length"] = jumpFloodSteps[i]
                    ShaderJumpFlood["TexelSize"] = texelSize.set(1f / mc.framebuffer.textureWidth, 1f / mc.framebuffer.textureHeight)
                    drawFullScreen()
                    ShaderJumpFlood.unbind()
                }

                fbos[0].clear()
                fbos[0].beginWrite(false)
                ShaderMask.bind()
                RenderSystem.activeTexture(GL_TEXTURE1)
                entitiesFbo.beginRead()
                RenderSystem.activeTexture(GL_TEXTURE0)
                fbos[1].beginRead()
                ShaderMask["Tex0"] = 0
                ShaderMask["Tex1"] = 1
                ShaderMask["Invert"] = true
                drawFullScreen()
                ShaderMask.unbind()

                BlurHelper.mode.switchStrength(7)
                BlurHelper.mode.render(fbos[0].colorAttachment, true)

                mc.framebuffer.beginWrite(false)
                ShaderTint.bind()
                RenderSystem.activeTexture(GL_TEXTURE0)
                BlurHelper.mode.output.beginRead()
                ShaderTint["Tex0"] = 0
                ShaderTint["RGBPuke"] = true
                ShaderTint["Opacity"] = 1f
                ShaderTint.set("SV", 70f / 100f, 100f / 100f)
                ShaderTint["Opacity"] = 100f / 100f
                ShaderTint["Alpha"] = true
                ShaderTint["Multiplier"] = 1.7f
                ShaderTint["Time"] = (System.nanoTime() - ShaderTint.initTime) / 1000000000f
                ShaderTint["Yaw"] = mc.player?.yaw ?: 0f
                ShaderTint["Pitch"] = mc.player?.pitch ?: 0f
                drawFullScreen()
                ShaderTint.unbind()

//                mc.framebuffer.beginWrite(false)
//                ShaderMask.bind()
//                RenderSystem.activeTexture(GL_TEXTURE1)
//                entitiesFbo.beginRead()
//                RenderSystem.activeTexture(GL_TEXTURE0)
//                fbos[0].beginRead()
//                ShaderMask["Tex0"] = 0
//                ShaderMask["Tex1"] = 1
//                ShaderMask["Invert"] = true
//                drawFullScreen()
//                ShaderMask.unbind()
            }

            entitiesFbo.clear()
            mc.framebuffer.beginWrite(false)
        }

        var shouldRender = false

        on<EventShouldRenderEntity> { event ->
            if (!event.shouldRender && entities.any { it.second && it.first == event.entity.type.name.string }) {
                shouldRender = true
                event.shouldRender = true
            } else {
                shouldRender = false
            }
        }

        var shouldHideLabel = false

        on<EventRenderEntity> { event ->
            if (!entities.any { it.second && it.first == event.entity.type.name.string })
                return@on
            entitiesFbo.beginWrite(false);
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
                vertexConsumer,
                worldRenderer.entityRenderDispatcher.getLight(event.entity, event.tickDelta.toFloat())
            )
            shouldHideLabel = false
            worldRenderer.entityRenderDispatcher.setRenderShadows(true)
            mc.framebuffer.beginWrite(false)
            if (shouldRender) {
                event.isCancelled = true
            }
        }

        on<EventEntityHasLabel> { event ->
            if (shouldHideLabel) {
                event.hasLabel = false
            }
        }
    }
}
