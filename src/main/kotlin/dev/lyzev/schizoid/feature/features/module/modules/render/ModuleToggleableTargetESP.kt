/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.render

import com.mojang.blaze3d.systems.RenderSystem
import dev.lyzev.api.animation.EasingFunction
import dev.lyzev.api.animation.TimeAnimator
import dev.lyzev.api.events.*
import dev.lyzev.api.opengl.Render
import dev.lyzev.api.opengl.WrappedFramebuffer
import dev.lyzev.api.opengl.clear
import dev.lyzev.api.opengl.shader.Shader
import dev.lyzev.api.opengl.shader.ShaderPassThrough
import dev.lyzev.api.opengl.shader.ShaderReflection
import dev.lyzev.api.setting.settings.option
import dev.lyzev.api.setting.settings.slider
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.ModuleToggleable
import dev.lyzev.schizoid.feature.features.module.modules.render.ModuleToggleableTorus.Torus.Companion.normal1
import dev.lyzev.schizoid.feature.features.module.modules.render.ModuleToggleableTorus.Torus.Companion.normal2
import dev.lyzev.schizoid.feature.features.module.modules.render.ModuleToggleableTorus.Torus.Companion.normal3
import dev.lyzev.schizoid.feature.features.module.modules.render.ModuleToggleableTorus.Torus.Companion.normal4
import dev.lyzev.schizoid.feature.features.module.modules.render.ModuleToggleableTorus.frequency
import net.minecraft.client.network.PlayerListEntry
import net.minecraft.client.render.BufferRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.texture.AbstractTexture
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.MathHelper.cos
import net.minecraft.util.math.MathHelper.sin
import org.joml.Matrix4f
import org.lwjgl.opengl.GL13

object ModuleToggleableTargetESP : ModuleToggleable(
    "Target ESP", "Renders a target ESP around the target player.", category = IFeature.Category.RENDER
), EventListener {

    private val fbo = WrappedFramebuffer(useDepth = true)
    private val timeAnimator = TimeAnimator(1000)

    val duration by slider("Duration", "The duration of the target hud to show after hit.", 2000, 500, 10000, "ms")
    val animationSpeed by slider("Animation Speed", "The speed of the animation.", 1000, 1, 5000, "ms") {
        timeAnimator.animationLength = it.toLong()
    }
    val easingFunction by option(
        "Easing Function",
        "The easing function of the animation.",
        EasingFunction.LINEAR,
        EasingFunction.entries
    )

    var target: PlayerEntity? = null
    private var abstractTexture: AbstractTexture? = null
    private var lastHit = 0L

    override val shouldHandleEvents: Boolean
        get() = isEnabled && isIngame

    init {
        on<EventAttackEntityPre> {
            if (it.entity !is PlayerEntity) return@on
            target = it.entity
            val skinTextures = PlayerListEntry.texturesSupplier(target!!.gameProfile).get()
            abstractTexture = mc.textureManager.getTexture(skinTextures!!.texture)
            lastHit = System.currentTimeMillis()
        }
        on<EventRenderWorld>(Event.Priority.LOW) { event ->
            if (System.currentTimeMillis() - lastHit > duration) {
                target = null
            }
            if (target != null && !target!!.isAlive) {
                target = null
            }
            if (target == null) return@on
            fbo.clear()
            fbo.copyDepthFrom(mc.framebuffer)
            fbo.beginWrite(false)
            RenderSystem.enableDepthTest()
            val cam = mc.gameRenderer.camera.pos
            @Suppress("NAME_SHADOWING") val modelViewMat = Matrix4f(event.modelViewMat)
            val tickDelta = Render.tickDelta.toDouble()
            val x = MathHelper.lerp(tickDelta, target!!.prevX, target!!.x)
            if (timeAnimator.getProgressNotClamped() > 1 || timeAnimator.getProgressNotClamped() < 0) {
                timeAnimator.setReversed(!timeAnimator.reversed)
            }
            val percentage = easingFunction(timeAnimator.getProgress())
            val y = MathHelper.lerp(tickDelta, target!!.prevY, target!!.y) + percentage * target!!.height
            val z = MathHelper.lerp(tickDelta, target!!.prevZ, target!!.z)
            modelViewMat.translate((x - cam.x).toFloat(), (y - cam.y).toFloat(), (z - cam.z).toFloat())
            modelViewMat.rotateX(MathHelper.HALF_PI) // pitch
            ShaderReflection.bind()
            ShaderReflection["ModelViewMat", false] = modelViewMat
            ShaderReflection["ProjMat", false] = event.projMat
            RenderSystem.activeTexture(GL13.GL_TEXTURE0)
            mc.framebuffer.beginRead()
            ShaderReflection["Tex0"] = 0
            ShaderReflection["Freq"] = frequency / 100f
            ShaderReflection["CamPos"] = cam.toVector3f()

            val slices = 10
            val loops = 30
            val outerRad = .6f
            val innerRad = .1f
            val bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION)
            for (i in 0 until slices) {
                val theta = 2 * Math.PI.toFloat() * i / slices
                val nextTheta = 2 * Math.PI.toFloat() * (i + 1) / slices

                for (j in 0 until loops) {
                    val phi = 2 * Math.PI.toFloat() * j / loops
                    val nextPhi = 2 * Math.PI.toFloat() * (j + 1) / loops

                    val x1 = (outerRad + innerRad * cos(theta)) * cos(phi)
                    val y1 = (outerRad + innerRad * cos(theta)) * sin(phi)
                    val z1 = innerRad * sin(theta)

                    val x2 = (outerRad + innerRad * cos(nextTheta)) * cos(phi)
                    val y2 = (outerRad + innerRad * cos(nextTheta)) * sin(phi)
                    val z2 = innerRad * sin(nextTheta)

                    val x3 = (outerRad + innerRad * cos(nextTheta)) * cos(nextPhi)
                    val y3 = (outerRad + innerRad * cos(nextTheta)) * sin(nextPhi)
                    val z3 = innerRad * sin(nextTheta)

                    val x4 = (outerRad + innerRad * cos(theta)) * cos(nextPhi)
                    val y4 = (outerRad + innerRad * cos(theta)) * sin(nextPhi)
                    val z4 = innerRad * sin(theta)

                    normal1.set(cos(theta) * cos(phi), cos(theta) * sin(phi), sin(theta)).normalize()
                    normal2.set(cos(nextTheta) * cos(phi), cos(nextTheta) * sin(phi), sin(nextTheta)).normalize()
                    normal3.set(cos(nextTheta) * cos(nextPhi), cos(nextTheta) * sin(nextPhi), sin(nextTheta))
                        .normalize()
                    normal4.set(cos(theta) * cos(nextPhi), cos(theta) * sin(nextPhi), sin(theta)).normalize()

                    bufferBuilder.vertex(x1, y1, z1).normal(normal1.x, normal1.y, normal1.z)
                    bufferBuilder.vertex(x2, y2, z2).normal(normal2.x, normal2.y, normal2.z)
                    bufferBuilder.vertex(x3, y3, z3).normal(normal3.x, normal3.y, normal3.z)
                    bufferBuilder.vertex(x4, y4, z4).normal(normal4.x, normal4.y, normal4.z)
                }
            }
            BufferRenderer.draw(bufferBuilder.end())
            ShaderReflection.unbind()
            RenderSystem.disableDepthTest()
            mc.framebuffer.beginWrite(false)
            ShaderPassThrough.bind()
            RenderSystem.activeTexture(GL13.GL_TEXTURE0)
            fbo.beginRead()
            ShaderPassThrough["Tex0"] = 0
            ShaderPassThrough["Scale"] = 1f
            ShaderPassThrough["Alpha"] = false
            Shader.drawFullScreen()
            ShaderPassThrough.unbind()
        }
    }
}
