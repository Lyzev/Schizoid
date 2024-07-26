/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.render

import com.mojang.blaze3d.systems.RenderSystem
import dev.lyzev.api.events.*
import dev.lyzev.api.opengl.shader.ShaderPosCol
import dev.lyzev.api.setting.settings.color
import dev.lyzev.api.setting.settings.slider
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.ModuleToggleable
import net.minecraft.client.render.BufferRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.render.entity.model.PlayerEntityModel
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.math.MathHelper
import org.joml.Matrix4f
import java.awt.Color

object ModuleToggleableSkeletons : ModuleToggleable(
    "Skeletons",
    "Renders skeletons of player entities in the world, providing a visual representation of their bone structure.",
    category = IFeature.Category.RENDER
), EventListener {

    private val entityModels = HashMap<Entity, Array<FloatArray>>()

    val color by color("Color", "The color of the skeletons", Color.WHITE, true)

    override fun onDisable() {
        entityModels.clear()
        if (color.alpha == 0) {
            sendChatMessage(Text.of("Disabling Skeletons, as the alpha is 0!"))
        }
    }

    override val shouldHandleEvents: Boolean
        get() = isEnabled && isIngame

    init {
        on<EventModelSetAngles> { event ->
            if (event.model !is PlayerEntityModel) return@on
            entityModels.remove(event.entity)
            entityModels[event.entity] = arrayOf(
                floatArrayOf(event.model.head.pitch, event.model.head.yaw, event.model.head.roll),
                floatArrayOf(event.model.rightArm.pitch, event.model.rightArm.yaw, event.model.rightArm.roll),
                floatArrayOf(event.model.leftArm.pitch, event.model.leftArm.yaw, event.model.leftArm.roll),
                floatArrayOf(event.model.rightLeg.pitch, event.model.rightLeg.yaw, event.model.rightLeg.roll),
                floatArrayOf(event.model.leftLeg.pitch, event.model.leftLeg.yaw, event.model.leftLeg.roll)
            )
        }

        on<EventRenderWorld>(Event.Priority.LOWEST) { event ->
            if (color.alpha == 0) {
                toggle()
                return@on
            }
            for (entity in mc.world!!.entities) {
                if (entity is PlayerEntity && (mc.player != entity || !mc.options.perspective.isFirstPerson) && entity.isAlive && !entity.isInvisible && !entity.isSleeping) {
                    val model = entityModels[entity] ?: continue
                    val cam = mc.gameRenderer.camera.pos
                    val tickDelta = mc.renderTickCounter.getTickDelta(!mc.world!!.tickManager.shouldSkipTick(entity))
                    val pos = entity.getLerpedPos(tickDelta)
                    val modelViewMat = Matrix4f(event.modelViewMat)
                    modelViewMat.translate(
                        (pos.x - cam.x).toFloat(), (pos.y - cam.y).toFloat(), (pos.z - cam.z).toFloat()
                    )
                    val lerpedBodyYaw = MathHelper.lerp(tickDelta, entity.prevBodyYaw, entity.bodyYaw)
                    val lerpedHeadYaw = MathHelper.lerp(tickDelta, entity.prevHeadYaw, entity.headYaw)
                    modelViewMat.rotateY(-lerpedBodyYaw * MathHelper.RADIANS_PER_DEGREE)
                    modelViewMat.translate(0f, 0f, if (entity.isSneaking) -0.235f else 0f)
                    val yOff = if (entity.isSneaking) 0.6f else 0.75f
                    val boneRightLeg = Matrix4f(modelViewMat)
                    boneRightLeg.translate(-0.125f, yOff, 0f)
                    boneRightLeg.rotateXYZ(model[3][0], model[3][1], -model[3][2])
                    ShaderPosCol.bind()
                    ShaderPosCol["ModelViewMat", false] = boneRightLeg
                    ShaderPosCol["ProjMat", false] = event.projMat
                    var bufferBuilder = Tessellator.getInstance()
                        .begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR)
                    bufferBuilder.vertex(0f, 0f, 0f)
                        .color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f);
                    bufferBuilder.vertex(0f, -yOff, 0f)
                        .color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f);
                    BufferRenderer.draw(bufferBuilder.end())
                    val boneLeftLeg = Matrix4f(modelViewMat)
                    boneLeftLeg.translate(0.125f, yOff, 0f)
                    boneLeftLeg.rotateXYZ(model[4][0], model[4][1], -model[4][2])
                    ShaderPosCol["ModelViewMat", false] = boneLeftLeg
                    ShaderPosCol["ProjMat", false] = event.projMat
                    bufferBuilder = Tessellator.getInstance()
                        .begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR)
                    bufferBuilder.vertex(0f, 0f, 0f)
                        .color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f);
                    bufferBuilder.vertex(0f, -yOff, 0f)
                        .color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f);
                    BufferRenderer.draw(bufferBuilder.end())
                    modelViewMat.translate(0f, 0f, if (entity.isSneaking) 0.25f else 0f)
                    val boneNeck = Matrix4f(modelViewMat)
                    boneNeck.translate(
                        0f, if (entity.isSneaking) -0.05f else 0f, if (entity.isSneaking) -0.01725f else 0f
                    )
                    val rightArm = Matrix4f(boneNeck)
                    rightArm.translate(-0.375f, yOff + 0.55f, 0f)
                    rightArm.rotateXYZ(model[1][0], model[1][1], -model[1][2])
                    ShaderPosCol["ModelViewMat", false] = rightArm
                    ShaderPosCol["ProjMat", false] = event.projMat
                    bufferBuilder = Tessellator.getInstance()
                        .begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR)
                    bufferBuilder.vertex(0f, 0f, 0f)
                        .color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f);
                    bufferBuilder.vertex(0f, -0.5f, 0f)
                        .color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f);
                    BufferRenderer.draw(bufferBuilder.end())
                    val leftArm = Matrix4f(boneNeck)
                    leftArm.translate(0.375f, yOff + 0.55f, 0f)
                    leftArm.rotateXYZ(model[2][0], model[2][1], -model[2][2])
                    ShaderPosCol["ModelViewMat", false] = leftArm
                    ShaderPosCol["ProjMat", false] = event.projMat
                    bufferBuilder = Tessellator.getInstance()
                        .begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR)
                    bufferBuilder.vertex(0f, 0f, 0f)
                        .color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f);
                    bufferBuilder.vertex(0f, -0.5f, 0f)
                        .color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f);
                    BufferRenderer.draw(bufferBuilder.end())
                    boneNeck.rotateY((lerpedBodyYaw - lerpedHeadYaw) * MathHelper.RADIANS_PER_DEGREE)
                    val head = Matrix4f(boneNeck)
                    head.translate(0f, yOff + 0.55f, 0f)
                    head.rotateXYZ(model[0][0], model[0][1], -model[0][2])
                    ShaderPosCol["ModelViewMat", false] = head
                    ShaderPosCol["ProjMat", false] = event.projMat
                    bufferBuilder = Tessellator.getInstance()
                        .begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR)
                    bufferBuilder.vertex(0f, 0f, 0f)
                        .color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f);
                    bufferBuilder.vertex(0f, 0.3f, 0f)
                        .color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f);
                    BufferRenderer.draw(bufferBuilder.end())

                    modelViewMat.rotateX(if (entity.isSneaking) 25f * MathHelper.RADIANS_PER_DEGREE else 0f)
                    modelViewMat.translate(
                        0f, if (entity.isSneaking) -0.16175f else 0f, if (entity.isSneaking) -0.48025f else 0f
                    )

                    // pelvis
                    modelViewMat.translate(0f, yOff, 0f)
                    ShaderPosCol["ModelViewMat", false] = modelViewMat
                    ShaderPosCol["ProjMat", false] = event.projMat
                    bufferBuilder = Tessellator.getInstance()
                        .begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR)
                    bufferBuilder.vertex(-0.125f, 0f, 0f)
                        .color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f);
                    bufferBuilder.vertex(0.125f, 0f, 0f)
                        .color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f);
                    BufferRenderer.draw(bufferBuilder.end())

                    // spine
                    bufferBuilder = Tessellator.getInstance()
                        .begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR)
                    bufferBuilder.vertex(0f, 0f, 0f)
                        .color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f);
                    bufferBuilder.vertex(0f, 0.55f, 0f)
                        .color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f);
                    BufferRenderer.draw(bufferBuilder.end())

                    // clavicle
                    modelViewMat.translate(0f, 0.55f, 0f)
                    ShaderPosCol["ModelViewMat", false] = modelViewMat
                    ShaderPosCol["ProjMat", false] = event.projMat
                    bufferBuilder = Tessellator.getInstance()
                        .begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR)
                    bufferBuilder.vertex(-0.375f, 0f, 0f)
                        .color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f);
                    bufferBuilder.vertex(0.375f, 0f, 0f)
                        .color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f);
                    BufferRenderer.draw(bufferBuilder.end())

                    ShaderPosCol.unbind()
                }
            }
        }
    }
}
