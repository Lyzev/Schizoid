/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.render

import com.mojang.blaze3d.systems.RenderSystem
import dev.lyzev.api.animation.EasingFunction
import dev.lyzev.api.events.*
import dev.lyzev.api.math.get
import dev.lyzev.api.opengl.Render
import dev.lyzev.api.opengl.shader.ShaderReflection
import dev.lyzev.api.setting.settings.slider
import dev.lyzev.api.setting.settings.switch
import dev.lyzev.schizoid.feature.features.module.ModuleToggleable
import net.minecraft.client.render.BufferRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.network.packet.s2c.play.DamageTiltS2CPacket
import net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.math.RotationAxis
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import org.joml.Vector3f
import org.lwjgl.opengl.GL13
import kotlin.math.cos
import kotlin.math.sin

object ModuleToggleableTorus :
    ModuleToggleable("Torus", "Renders a Torus with reflection effect.", category = Category.RENDER), EventListener {

    val depth by switch("Depth", "Whether to render the torus in depth.", false)
    val frequency by slider("Noise frequency", "The strength of the noise effect.", 50, 0, 100, "%%")
    val lifetime by slider("Lifetime", "The lifetime of the torus.", 1000, 1, 5000, "ms")

    private val toruses = mutableListOf<Torus>()

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
            Render.store()
            if (depth) RenderSystem.enableDepthTest()
            else RenderSystem.disableDepthTest()
            RenderSystem.disableCull()
            try {
                toruses.forEach { torus ->
//                    torus.render(event.matrices)
                }
            } catch (ignored: Exception) {
            }
            RenderSystem.disableDepthTest()
            Render.restore()
        }
    }

    class Torus(private val pos: Vec3d, private val rotation: Vec2f) {

        val spawn = System.currentTimeMillis()

        fun render(matrices: MatrixStack) {
            val cam = mc.gameRenderer.camera.pos
            matrices.push()
            matrices.translate(pos.x - cam.x, pos.y - cam.y, pos.z - cam.z)
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-rotation.y)) // yaw
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(rotation.x)) // pitch
            val model = matrices.peek().positionMatrix

            ShaderReflection.bind()
            ShaderReflection["ModelViewMat", false] = model
            ShaderReflection["ProjMat", false] = RenderSystem.getProjectionMatrix()
            GL13.glActiveTexture(GL13.GL_TEXTURE0)
            mc.framebuffer.beginRead()
            ShaderReflection["Tex0"] = 0
            ShaderReflection["Freq"] = frequency / 100f
            ShaderReflection["CamPos"] = cam.toVector3f()

            val delta = System.currentTimeMillis() - spawn
            val bufferBuilder = Tessellator.getInstance().buffer

            val slices = 10
            val loops = 30
            val outerRad = .6 + 2 * EasingFunction.OUT_CUBIC(delta / lifetime.toDouble())
            val innerRad = .4 - .4 * EasingFunction.IN_CUBIC(delta / lifetime.toDouble())
            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION)
            for (i in 0 until slices) {
                val theta = 2 * Math.PI * i / slices
                val nextTheta = 2 * Math.PI * (i + 1) / slices

                for (j in 0 until loops) {
                    val phi = 2 * Math.PI * j / loops
                    val nextPhi = 2 * Math.PI * (j + 1) / loops

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

                    val t1 = Vector3f(
                        (-(outerRad + innerRad * cos(theta)) * sin(phi)).toFloat(),
                        ((outerRad + innerRad * cos(theta)) * cos(phi)).toFloat(),
                        0f
                    )
                    val t2 = Vector3f(
                        (-innerRad * sin(theta) * cos(phi)).toFloat(),
                        (-innerRad * sin(theta) * sin(phi)).toFloat(),
                        (innerRad * cos(theta)).toFloat()
                    )

                    val normal = t1.cross(t2).normalize()

                    bufferBuilder.vertex(x1, y1, z1).normal(normal.x, normal.y, normal.z).next()
                    bufferBuilder.vertex(x2, y2, z2).normal(normal.x, normal.y, normal.z).next()
                    bufferBuilder.vertex(x3, y3, z3).normal(normal.x, normal.y, normal.z).next()
                    bufferBuilder.vertex(x4, y4, z4).normal(normal.x, normal.y, normal.z).next()
                }
            }
            BufferRenderer.draw(bufferBuilder.end())
            matrices.pop()
            ShaderReflection.unbind()
            RenderSystem.activeTexture(GL13.GL_TEXTURE0)
        }
    }
}
