/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.render

import com.mojang.blaze3d.systems.RenderSystem
import dev.lyzev.api.events.*
import dev.lyzev.api.opengl.shader.blur.BlurHelper
import dev.lyzev.api.setting.settings.OptionEnum
import dev.lyzev.api.setting.settings.option
import dev.lyzev.schizoid.feature.features.module.ModuleToggleable
import net.minecraft.client.render.*
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.TntEntity
import net.minecraft.text.Text
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.RotationAxis
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import org.joml.Quaternionf
import org.joml.Vector3f
import org.lwjgl.opengl.GL11
import kotlin.math.cos
import kotlin.math.sin

// TODO: NOT FINISHED!!!
// apply shaders on torus and more
// improve performance
object ModuleToggleableTorus :
    ModuleToggleable("Torus", "Renders a Torus.", category = Category.RENDER),
    EventListener {

    private val toruses = mutableListOf<Torus>()

    override val shouldHandleEvents: Boolean
        get() = isEnabled

    init {
        on<EventAttackEntity> {
            toruses.add(Torus(mc.crosshairTarget!!.pos, mc.player!!.rotationClient))
        }

        on<EventRenderWorld> { event ->
            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            RenderSystem.defaultBlendFunc()
            RenderSystem.enableBlend()
            RenderSystem.setShader { GameRenderer.getPositionColorProgram() }
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f)

            toruses.removeIf { System.currentTimeMillis() - it.spawn > 1000L }
            for (torus in toruses) {
                torus.render(event.matrices)
            }

            RenderSystem.enableBlend()
            GL11.glDisable(GL11.GL_LINE_SMOOTH)
        }
    }

    class Torus(private val pos: Vec3d, private val rotation: Vec2f) {

        val spawn = System.currentTimeMillis()

        fun render(matrices: MatrixStack) {
            val delta = System.currentTimeMillis() - spawn
            val cam = mc.gameRenderer.camera.pos
            val bufferBuilder = Tessellator.getInstance().buffer
            matrices.push()
            matrices.translate(pos.x - cam.x, pos.y - cam.y, pos.z - cam.z)
            fun wrapYaw(yaw: Double): Double {
                val wrappedYaw = yaw % 360.0
                return if (wrappedYaw < 0) wrappedYaw + 360.0 else wrappedYaw
            }

            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-rotation.y))
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(rotation.x))
            val matrix = matrices.peek().positionMatrix
            val slices = 10
            val loops = 20
            val innerRad = 0.5f * (delta / 500f)
            val outerRad = 2f * (delta / 500f)
            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)
            for (i in 0..slices) {
                val theta = 2.0 * Math.PI * i / slices
                val nextTheta = 2.0 * Math.PI * (i + 1) / slices

                for (j in 0..loops) {
                    val phi = 2.0 * Math.PI * j / loops
                    val nextPhi = 2.0 * Math.PI * (j + 1) / loops

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

                    bufferBuilder.vertex(
                        matrix,
                        x1.toFloat(),
                        y1.toFloat(),
                        z1.toFloat()
                    ).color(1f, 1f, 1f, 1 - delta / 1000f).next()
                    bufferBuilder.vertex(
                        matrix,
                        x2.toFloat(),
                        y2.toFloat(),
                        z2.toFloat()
                    ).color(1f, 1f, 1f, 1 - delta / 1000f).next()
                    bufferBuilder.vertex(
                        matrix,
                        x3.toFloat(),
                        y3.toFloat(),
                        z3.toFloat()
                    ).color(1f, 1f, 1f, 1 - delta / 1000f).next()
                    bufferBuilder.vertex(
                        matrix,
                        x4.toFloat(),
                        y4.toFloat(),
                        z4.toFloat()
                    ).color(1f, 1f, 1f, 1 - delta / 1000f).next()
                }
            }
            BufferRenderer.drawWithGlobalProgram(bufferBuilder.end())
            matrices.pop()
        }
    }
}
