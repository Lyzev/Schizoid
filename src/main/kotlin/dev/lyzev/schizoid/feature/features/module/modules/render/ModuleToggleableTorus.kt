/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.render

import com.mojang.blaze3d.systems.RenderSystem
import dev.lyzev.api.animation.EasingFunction
import dev.lyzev.api.events.*
import dev.lyzev.api.math.get
import dev.lyzev.api.opengl.WrappedFramebuffer
import dev.lyzev.api.opengl.clear
import dev.lyzev.api.opengl.shader.Shader
import dev.lyzev.api.opengl.shader.ShaderPassThrough
import dev.lyzev.api.opengl.shader.ShaderReflection
import dev.lyzev.api.setting.settings.slider
import dev.lyzev.api.setting.settings.switch
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.ModuleToggleable
import net.minecraft.client.render.BufferRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.entity.Entity
import net.minecraft.network.packet.s2c.play.DamageTiltS2CPacket
import net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.math.MathHelper.cos
import net.minecraft.util.math.MathHelper.sin
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.opengl.GL13
import java.util.concurrent.CopyOnWriteArrayList

object ModuleToggleableTorus :
    ModuleToggleable("Torus", "Renders a Torus with reflection effect.", category = IFeature.Category.RENDER),
    EventListener {

    private val fbo = WrappedFramebuffer(useDepth = true)

    val depth by switch("Depth", "Whether to render the torus in depth.", false)
    val frequency by slider("Noise frequency", "The strength of the noise effect.", 50, 0, 100, "%%")
    val lifetime by slider("Lifetime", "The lifetime of the torus.", 1000, 1, 5000, "ms")

    private val toruses = CopyOnWriteArrayList<Torus>()

    override val shouldHandleEvents: Boolean
        get() = isEnabled && !mc.gameRenderer.isRenderingPanorama && mc.player?.eyePos != null

    private fun hit(entity: Entity) {
        if (entity == mc.player) return
        val target = mc.crosshairTarget
        val hitPos = if (target is EntityHitResult && target.entity == entity) target.pos
        else entity.eyePos
        toruses.add(Torus(hitPos, mc.player?.eyePos!![hitPos]))
    }

    init {
        on<EventPacket> { event ->
            when (event.packet) {
                is EntityDamageS2CPacket -> hit(mc.world?.getEntityById(event.packet.entityId) ?: return@on)
                is DamageTiltS2CPacket -> hit(mc.world?.getEntityById(event.packet.id) ?: return@on)
            }
        }

        on<EventRenderWorld>(Event.Priority.LOW) { event ->
            toruses.removeIf { System.currentTimeMillis() - it.spawn > lifetime }
            if (toruses.isEmpty()) return@on
            fbo.clear()
            if (depth) {
                fbo.copyDepthFrom(mc.framebuffer)
                RenderSystem.enableDepthTest()
            }
            fbo.beginWrite(false)
            toruses.forEach { torus ->
                torus.render(event.modelViewMat, event.projMat)
            }
            if (depth)
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

    class Torus(private val pos: Vec3d, private val rotation: Vec2f) {

        val spawn = System.currentTimeMillis()

        fun render(modelViewMat: Matrix4f, projMat: Matrix4f) {
            val cam = mc.gameRenderer.camera.pos
            @Suppress("NAME_SHADOWING") val modelViewMat = Matrix4f(modelViewMat)
            modelViewMat.translate((pos.x - cam.x).toFloat(), (pos.y - cam.y).toFloat(), (pos.z - cam.z).toFloat())
            modelViewMat.rotateY(Math.toRadians((-rotation.y).toDouble()).toFloat()) // yaw
            modelViewMat.rotateX(Math.toRadians((-rotation.x).toDouble()).toFloat()) // pitch
            ShaderReflection.bind()
            ShaderReflection["ModelViewMat", false] = modelViewMat
            ShaderReflection["ProjMat", false] = projMat
            RenderSystem.activeTexture(GL13.GL_TEXTURE0)
//            ModuleToggleableRearView.rearView.beginRead()
            mc.framebuffer.beginRead()
            ShaderReflection["Tex0"] = 0
            ShaderReflection["Freq"] = frequency / 100f
            ShaderReflection["CamPos"] = cam.toVector3f()

            val delta = System.currentTimeMillis() - spawn

            val slices = 10
            val loops = 30
            val outerRad = .6f + 2f * EasingFunction.OUT_CUBIC(delta / lifetime.toDouble()).toFloat()
            val innerRad = .4f - .4f * EasingFunction.IN_CUBIC(delta / lifetime.toDouble()).toFloat()
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
        }

        companion object {

            val normal1 = Vector3f()
            val normal2 = Vector3f()
            val normal3 = Vector3f()
            val normal4 = Vector3f()
        }
    }
}
