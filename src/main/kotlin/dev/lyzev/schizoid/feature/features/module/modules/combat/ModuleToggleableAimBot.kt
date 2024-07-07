/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.combat

import com.mojang.blaze3d.systems.RenderSystem
import dev.lyzev.api.events.*
import dev.lyzev.api.math.AI
import dev.lyzev.api.math.canSee
import dev.lyzev.api.math.get
import dev.lyzev.api.opengl.Render
import dev.lyzev.api.opengl.shader.ShaderPosCol
import dev.lyzev.api.opengl.shader.ShaderReflection
import dev.lyzev.api.setting.settings.*
import dev.lyzev.api.settings.Setting.Companion.eq
import dev.lyzev.api.settings.Setting.Companion.neq
import dev.lyzev.schizoid.Schizoid
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.ModuleToggleable
import dev.lyzev.schizoid.feature.features.module.modules.player.FeatureRotation.current
import net.minecraft.client.render.BufferRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.registry.Registries
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.RaycastContext
import org.joml.Matrix4f
import org.lwjgl.opengl.GL13
import java.awt.Color
import kotlin.math.min

object ModuleToggleableAimBot : ModuleToggleable(
    "Aim Bot", "Collect data for Aim Bot.", category = IFeature.Category.COMBAT
), EventListener {

    private var aimVec: Vec3d? = null
    private var lastAimVec: Vec3d? = null
    private var time = System.currentTimeMillis()

    val targets by multiOption(
        "Targets",
        "The targets to aim at.",
        Registries.ENTITY_TYPE.map { it.name.string to (it.name.string == "Player") }.sortedBy { it.first }.toSet()
    )
    val aimExtensionReach by slider("Aim Extension Reach", "The reach of the aim extension.", 1.8f, 0f, 5f, 1, "blocks")
    val aimThroughWalls by switch("Aim Through Walls", "Aims through walls.", false)
    val aimInstant by switch("Aim Instant", "Instantly aim at the target.", false)
    val aimSpeed by slider("Aim Speed", "The speed of the aim.", 55, 1, 100, "%%", hide = ::aimInstant eq true)
    val aimSpeedDistanceWeight by slider(
        "Aim Speed Distance Weight",
        "The weight of the distance to the target in the aim speed.",
        4,
        0,
        100,
        "%%",
        hide = ::aimInstant eq true
    )
    val aimSpeedCrosshairWeight by slider(
        "Aim Speed Crosshair Weight",
        "The weight of the target in the crosshair in the aim speed.",
        12,
        0,
        100,
        "%%",
        hide = ::aimInstant eq true
    )
    val aimSpeedHurtTimeWeight by slider(
        "Aim Speed Hurt Time Weight",
        "The weight of the hurt time in the aim speed.",
        6,
        0,
        100,
        "%%",
        hide = ::aimInstant eq true
    )
    val aimSpeedVelocityWeight by slider(
        "Aim Speed Velocity Weight",
        "The weight of the target velocity in the aim speed.",
        4,
        0,
        100,
        "%%",
        hide = ::aimInstant eq true
    )
    val aimSpeedRandomWeight by slider(
        "Aim Speed Random Weight",
        "The weight of the random factor in the aim speed.",
        2,
        0,
        100,
        "%%",
        hide = ::aimInstant eq true
    )
    val aimVector by option("Aim Vector", "The aim vector.", AimVector.Intelligent, AimVector.entries)
    val forceHit by switch("Force Hit", "Aims on the nearest vector if the artificial intelligence vector is not in reach.", true, hide = ::aimVector eq "Nearest")
    val aimSpeedForceHitWeight by slider(
        "Aim Force Hit Weight",
        "The weight of the force hit in the aim speed.",
        20,
        0,
        100,
        "%%",
        hide = ::aimInstant eq true
    )
    val visualizeAimVector by switch("Visualize Aim Vector", "Visualize the aim vector.", true)
    val reflection by switch(
        "Reflection", "Reflection effect on aim vector.", false, hide = ::visualizeAimVector neq true
    )
    val color by color("Color", "The color of the aim vector.", Color(1f, 0f, 0f, 0.8f), true, hide = {
        !visualizeAimVector || reflection
    })
    val fov by slider("Field of View", "The field of view.", 140, 1, 255, "°")
    val fovRotation by option(
        "Field of View Rotation", "Which rotation to use for the field of view.", "Server", arrayOf("Server", "Client")
    )
    val priority by option("Priority", "The priority of the target.", Priority.FOV, Priority.entries)
    val switchDelay by slider("Switch Delay", "The delay before switching targets.", 300, 0, 3000, "ms")

    override fun onDisable() {
        aimVec = null
    }

    override val shouldHandleEvents
        get() = isEnabled && isIngame

    init {

        var lastHitVec: Vec3d? = null
        var target: Entity? = null

        on<EventUpdateCrosshairTargetTick> {
            val reach = mc.player!!.entityInteractionRange
            val maxReach = reach + aimExtensionReach
            val possibleTarget = mc.world!!.entities.filter { entity ->
                entity != mc.player && entity.isAlive && targets.any { it.second && it.first == entity.type.name.string } && entity.boundingBox[mc.player!!.eyePos].let { nearest ->
                    mc.player!!.squaredDistanceTo(nearest) <= MathHelper.square(maxReach) && mc.player!!.eyePos[nearest].let { rotation ->
                        val deltaPitch = (if (fovRotation == "Server") current.y else mc.player!!.pitch) - rotation.x
                        val deltaYaw =
                            MathHelper.wrapDegrees((if (fovRotation == "Server") current.x else mc.player!!.yaw) - rotation.y)
                        MathHelper.square(deltaPitch) + MathHelper.square(deltaYaw)
                    } > MathHelper.square(255 - fov)
                }
            }.minByOrNull {
                priority.getEntityValue(it)
            }
            if (target == null || possibleTarget == null || System.currentTimeMillis() - time > switchDelay || !target!!.isAlive || target?.let { entity ->
                    entity.boundingBox[mc.player!!.eyePos].let { nearest ->
                        mc.player!!.squaredDistanceTo(nearest) <= MathHelper.square(maxReach) && mc.player!!.eyePos[nearest].let { rotation ->
                            val deltaPitch =
                                (if (fovRotation == "Server") current.y else mc.player!!.pitch) - rotation.x
                            val deltaYaw =
                                MathHelper.wrapDegrees(MathHelper.wrapDegrees((if (fovRotation == "Server") current.x else mc.player!!.yaw)) - rotation.y)
                            MathHelper.square(deltaPitch) + MathHelper.square(deltaYaw)
                        } > MathHelper.square(255 - fov)
                    }
                } == false) {
                if (possibleTarget != null) {
                    time = System.currentTimeMillis()
                }
                target = possibleTarget
            }
            if (target != null) {
                lastAimVec = aimVec
                aimVec = aimVector.getAimVec(target!!, reach, lastHitVec)

                val box = target!!.boundingBox
                val crosshairAim = box[mc.crosshairTarget!!.pos]
                lastHitVec = Vec3d(
                    (crosshairAim.x - box.minX) / (box.maxX - box.minX),
                    (crosshairAim.y - box.minY) / (box.maxY - box.minY),
                    (crosshairAim.z - box.minZ) / (box.maxZ - box.minZ)
                )
            } else {
                aimVec = null
                lastHitVec = null
            }
        }

        on<EventRotationGoal> { event ->
            if (aimVec == null || (!aimThroughWalls && !mc.player!!.canSee(aimVec!!))) return@on
            if (mc.player!!.boundingBox.contains(aimVec) || target!!.boundingBox.contains(mc.player!!.eyePos)) {
                aimVec = Vec3d(
                    target!!.boundingBox.minX + (target!!.boundingBox.maxX - target!!.boundingBox.minX) * 0.5,
                    aimVec!!.y,
                    target!!.boundingBox.minZ + (target!!.boundingBox.maxZ - target!!.boundingBox.minZ) * 0.5
                )
            }
            event.goal = aimVec
            if (aimInstant) {
                event.instant = true
                return@on
            }
            if (aimInstant) {
                event.weight = 1f
            } else {
                event.weight = aimSpeed / 100f

                // Distance
                val aimSpeedDistanceWeight = aimSpeedDistanceWeight / 100f
                val reach = mc.player!!.entityInteractionRange
                val maxReach = reach + aimExtensionReach
                val distance = mc.player!!.eyePos.distanceTo(target!!.boundingBox[mc.player!!.eyePos])
                if (distance <= reach && (target !is LivingEntity || (target as LivingEntity).hurtTime <= 3)) {
                    event.force = true
                }
                event.weight += (distance / maxReach).toFloat() * aimSpeedDistanceWeight

                // Crosshair
                if (mc.targetedEntity == target) {
                    val aimSpeedCrosshairWeight = aimSpeedCrosshairWeight / 100f
                    event.weight -= Schizoid.random.nextFloat() * (aimSpeedCrosshairWeight * 0.2f) + aimSpeedCrosshairWeight * 0.8f
                }

                // Hurt time
                val aimSpeedHurtTimeWeight = aimSpeedHurtTimeWeight / 100f
                if (target is LivingEntity) {
                    event.weight += (1 - (target as LivingEntity).hurtTime / (target as LivingEntity).maxHurtTime.toFloat()) * aimSpeedHurtTimeWeight
                }

                // Velocity
                val aimSpeedVelocityWeight = aimSpeedVelocityWeight / 100f
                val velocity = Vec3d(
                    target!!.x - target!!.prevX, target!!.y - target!!.prevY, target!!.z - target!!.prevZ
                ).lengthSquared() / 12.0
                event.weight += velocity.toFloat() * aimSpeedVelocityWeight

                // Random
                val aimSpeedRandomWeight = aimSpeedRandomWeight / 100f
                event.weight += aimSpeedRandomWeight - Schizoid.random.nextFloat() * aimSpeedRandomWeight / 2f

                if (forceHit && (target !is LivingEntity || (target as LivingEntity).hurtTime <= 3) && mc.player!!.eyePos.squaredDistanceTo(aimVec) < reach * reach) {
                    event.weight += aimSpeedForceHitWeight / 100f + Schizoid.random.nextFloat() * 0.1f
                }
            }
        }

        on<EventRenderWorld>(Event.Priority.LOW) { event ->
            if (aimVec == null || !visualizeAimVector) return@on
            RenderSystem.enableDepthTest()
            val cam = mc.gameRenderer.camera.pos
            val modelViewMat = Matrix4f(event.modelViewMat)
            val x = if (lastAimVec == null) aimVec!!.x else MathHelper.lerp(
                Render.tickDelta.toDouble(), lastAimVec!!.x, aimVec!!.x
            )
            val y = if (lastAimVec == null) aimVec!!.y else MathHelper.lerp(
                Render.tickDelta.toDouble(), lastAimVec!!.y, aimVec!!.y
            )
            val z = if (lastAimVec == null) aimVec!!.z else MathHelper.lerp(
                Render.tickDelta.toDouble(), lastAimVec!!.z, aimVec!!.z
            )
            modelViewMat.translate(
                (x - cam.x).toFloat(), (y - cam.y).toFloat(), (z - cam.z).toFloat()
            )

            val size = 0.04f

            val x1 = -size
            val y1 = -size
            val z1 = -size
            val x2 = size
            val y2 = size
            val z2 = size

            if (reflection) {
                ShaderReflection.bind()
                ShaderReflection["ModelViewMat", false] = modelViewMat
                ShaderReflection["ProjMat", false] = event.projMat
                GL13.glActiveTexture(GL13.GL_TEXTURE0)
                mc.framebuffer.beginRead()
                ShaderReflection["Tex0"] = 0
                ShaderReflection["Freq"] = .05f
                ShaderReflection["CamPos"] = cam.toVector3f()

                // back
                var bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION)
                bufferBuilder.vertex(x1, y1, z1).normal(0f, 0f, -1f)
                bufferBuilder.vertex(x1, y2, z1).normal(0f, 0f, -1f)
                bufferBuilder.vertex(x2, y2, z1).normal(0f, 0f, -1f)
                bufferBuilder.vertex(x2, y1, z1).normal(0f, 0f, -1f)
                BufferRenderer.draw(bufferBuilder.end())

                // left
                bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION)
                bufferBuilder.vertex(x1, y2, z2).normal(-1f, 0f, 0f)
                bufferBuilder.vertex(x1, y2, z1).normal(-1f, 0f, 0f)
                bufferBuilder.vertex(x1, y1, z1).normal(-1f, 0f, 0f)
                bufferBuilder.vertex(x1, y1, z2).normal(-1f, 0f, 0f)
                BufferRenderer.draw(bufferBuilder.end())

                // front
                bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION)
                bufferBuilder.vertex(x2, y1, z2).normal(0f, 0f, 1f)
                bufferBuilder.vertex(x2, y2, z2).normal(0f, 0f, 1f)
                bufferBuilder.vertex(x1, y2, z2).normal(0f, 0f, 1f)
                bufferBuilder.vertex(x1, y1, z2).normal(0f, 0f, 1f)
                BufferRenderer.draw(bufferBuilder.end())

                // right
                bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION)
                bufferBuilder.vertex(x2, y2, z1).normal(1f, 0f, 0f)
                bufferBuilder.vertex(x2, y2, z2).normal(1f, 0f, 0f)
                bufferBuilder.vertex(x2, y1, z2).normal(1f, 0f, 0f)
                bufferBuilder.vertex(x2, y1, z1).normal(1f, 0f, 0f)
                BufferRenderer.draw(bufferBuilder.end())

                // top
                bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION)
                bufferBuilder.vertex(x2, y2, z2).normal(0f, 1f, 0f)
                bufferBuilder.vertex(x2, y2, z1).normal(0f, 1f, 0f)
                bufferBuilder.vertex(x1, y2, z1).normal(0f, 1f, 0f)
                bufferBuilder.vertex(x1, y2, z2).normal(0f, 1f, 0f)
                BufferRenderer.draw(bufferBuilder.end())

                // bottom
                bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION)
                bufferBuilder.vertex(x1, y1, z1).normal(0f, -1f, 0f)
                bufferBuilder.vertex(x2, y1, z1).normal(0f, -1f, 0f)
                bufferBuilder.vertex(x2, y1, z2).normal(0f, -1f, 0f)
                bufferBuilder.vertex(x1, y1, z2).normal(0f, -1f, 0f)
                BufferRenderer.draw(bufferBuilder.end())
                ShaderReflection.unbind()
            } else {
                ShaderPosCol.bind()
                ShaderPosCol["ModelViewMat", false] = modelViewMat
                ShaderPosCol["ProjMat", false] = event.projMat

                // back
                var bufferBuilder =
                    Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)
                bufferBuilder.vertex(x1, y1, z1)
                    .color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
                bufferBuilder.vertex(x1, y2, z1)
                    .color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
                bufferBuilder.vertex(x2, y2, z1)
                    .color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
                bufferBuilder.vertex(x2, y1, z1)
                    .color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
                BufferRenderer.draw(bufferBuilder.end())

                // left
                bufferBuilder =
                    Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)
                bufferBuilder.vertex(x1, y2, z2)
                    .color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
                bufferBuilder.vertex(x1, y2, z1)
                    .color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
                bufferBuilder.vertex(x1, y1, z1)
                    .color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
                bufferBuilder.vertex(x1, y1, z2)
                    .color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
                BufferRenderer.draw(bufferBuilder.end())

                // front
                bufferBuilder =
                    Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)
                bufferBuilder.vertex(x2, y1, z2)
                    .color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
                bufferBuilder.vertex(x2, y2, z2)
                    .color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
                bufferBuilder.vertex(x1, y2, z2)
                    .color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
                bufferBuilder.vertex(x1, y1, z2)
                    .color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
                BufferRenderer.draw(bufferBuilder.end())

                // right
                bufferBuilder =
                    Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)
                bufferBuilder.vertex(x2, y2, z1)
                    .color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
                bufferBuilder.vertex(x2, y2, z2)
                    .color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
                bufferBuilder.vertex(x2, y1, z2)
                    .color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
                bufferBuilder.vertex(x2, y1, z1)
                    .color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
                BufferRenderer.draw(bufferBuilder.end())

                // top
                bufferBuilder =
                    Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)
                bufferBuilder.vertex(x2, y2, z2)
                    .color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
                bufferBuilder.vertex(x2, y2, z1)
                    .color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
                bufferBuilder.vertex(x1, y2, z1)
                    .color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
                bufferBuilder.vertex(x1, y2, z2)
                    .color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
                BufferRenderer.draw(bufferBuilder.end())

                // bottom
                bufferBuilder =
                    Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)
                bufferBuilder.vertex(x1, y1, z1)
                    .color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
                bufferBuilder.vertex(x2, y1, z1)
                    .color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
                bufferBuilder.vertex(x2, y1, z2)
                    .color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
                bufferBuilder.vertex(x1, y1, z2)
                    .color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
                BufferRenderer.draw(bufferBuilder.end())
                ShaderPosCol.unbind()
            }
            RenderSystem.activeTexture(GL13.GL_TEXTURE0)
        }
    }

    enum class AimVector : OptionEnum {
        Intelligent {

            private val layers = mutableListOf<Pair<Array<DoubleArray>, DoubleArray>>()

            init {
                for (i in 1..4) {
                    val weights = AI.loadWeights("layer_${i}_weights.csv")
                    val biases = AI.loadBiases("layer_${i}_biases.csv")
                    layers.add(Pair(weights, biases))
                }
            }

            override fun getAimVec(target: Entity, reach: Double, lastHitVec: Vec3d?): Vec3d {
                val maxReach = reach + aimExtensionReach
                val box = target.boundingBox
                var wasAiming = 0
                val hitVec = lastHitVec ?: run {
                    wasAiming = 1
                    val nearest = box[mc.player!!.eyePos]
                    Vec3d(
                        (nearest.x - box.minX) / (box.maxX - box.minX),
                        (nearest.y - box.minY) / (box.maxY - box.minY),
                        (nearest.z - box.minZ) / (box.maxZ - box.minZ)
                    )
                }
                val lookVecEntity =
                    Vec3d.fromPolar(mc.cameraEntity!!.eyePos[target.eyePos]).normalize().add(1.0, 1.0, 1.0)
                        .normalize()
                val reachPercentage = mc.cameraEntity!!.eyePos.distanceTo(box[mc.cameraEntity!!.eyePos]) / maxReach
                val velocityTarget = Vec3d(
                    target.x - target.prevX, target.y - target.prevY, target.z - target.prevZ
                ).normalize().add(1.0, 1.0, 1.0).normalize()
                val velocityPlayer = Vec3d(mc.player!!.x - mc.player!!.prevX, mc.player!!.y - mc.player!!.prevY, mc.player!!.z - mc.player!!.prevZ).normalize().add(1.0, 1.0, 1.0).normalize()
                val lookVec = Vec3d.fromPolar(current.y, current.x).normalize().add(1.0, 1.0, 1.0).normalize()

                val speedTarget = min(Vec3d(target.x - target.prevX, target.y - target.prevY, target.z - target.prevZ).lengthSquared() / 100.0, 1.0)
                val speedPlayer = min(Vec3d(mc.player!!.x - mc.player!!.prevX, mc.player!!.y - mc.player!!.prevY, mc.player!!.z - mc.player!!.prevZ).lengthSquared() / 100.0, 1.0)
                val clicks = min(mc.options.attackKey.timesPressed / 4.0, 1.0)
                val raycast = DoubleArray(8)
                val from = mc.player!!.eyePos
                for (x in 0..1) {
                    for (y in 0..1) {
                        for (z in 0..1) {
                            val to = Vec3d(
                                box.minX + x * (box.maxX - box.minX) * 0.5 + 0.25 * (box.maxX - box.minX),
                                box.minY + y * (box.maxY - box.minY) * 0.5 + 0.25 * (box.maxY - box.minY),
                                box.minZ + z * (box.maxZ - box.minZ) * 0.5 + 0.25 * (box.maxZ - box.minZ)
                            )
                            val hitResult = mc.world!!.raycast(RaycastContext(from, to, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mc.player))
                            if (hitResult == null || hitResult.type == HitResult.Type.MISS) {
                                raycast[x * 4 + y * 2 + z] = 1.0
                            } else {
                                raycast[x * 4 + y * 2 + z] = 0.0
                            }
                        }
                    }
                }

                val input = doubleArrayOf(
                    reachPercentage,
                    hitVec.x,
                    hitVec.y,
                    hitVec.z,
                    lookVecEntity.x,
                    lookVecEntity.y,
                    lookVecEntity.z,
                    wasAiming.toDouble(),
                    speedTarget,
                    speedPlayer,
                    lookVec.x,
                    lookVec.y,
                    lookVec.z,
                    clicks,
                    *raycast,
                    velocityTarget.x,
                    velocityTarget.y,
                    velocityTarget.z,
                    velocityPlayer.x,
                    velocityPlayer.y,
                    velocityPlayer.z
                )

                // Initial hidden layer
                var hiddenLayerOutput = AI.applyLayer(input, layers[0].first, layers[0].second)
                // Subsequent hidden layers
                for (i in 1..2) {
                    val hiddenLayerWeights = layers[i].first
                    val hiddenLayerBiases = layers[i].second
                    hiddenLayerOutput = AI.applyLayer(hiddenLayerOutput, hiddenLayerWeights, hiddenLayerBiases)
                }
                // Final output layer
                val output = AI.applyLayer(hiddenLayerOutput, layers[3].first, layers[3].second, useSigmoid = true)
                val aimVec = Vec3d(
                    output[0] * (box.maxX - box.minX) + box.minX,
                    output[1] * (box.maxY - box.minY) + box.minY,
                    output[2] * (box.maxZ - box.minZ) + box.minZ
                )
                return if (shouldForceHit(target, reach, aimVec)) {
                    target.boundingBox[mc.player!!.eyePos]
                } else {
                    aimVec
                }
            }
        },
        Nearest {
            override fun getAimVec(target: Entity, reach: Double, lastHitVec: Vec3d?): Vec3d {
                return target.boundingBox[mc.player!!.eyePos]
            }
        },
        Head {
            override fun getAimVec(target: Entity, reach: Double, lastHitVec: Vec3d?): Vec3d {
                return Vec3d(target.x, target.eyeY, target.z)
            }
        },
        Center {
            override fun getAimVec(target: Entity, reach: Double, lastHitVec: Vec3d?): Vec3d {
                val box = target.boundingBox
                return Vec3d(
                    box.minX + (box.maxX - box.minX) * 0.5,
                    box.minY + (box.maxY - box.minY) * 0.5,
                    box.minZ + (box.maxZ - box.minZ) * 0.5
                )
            }
        };

        abstract fun getAimVec(target: Entity, reach: Double, lastHitVec: Vec3d?): Vec3d

        protected fun shouldForceHit(target: Entity, reach: Double, aimVec: Vec3d): Boolean {
            if (forceHit && (target !is LivingEntity || target.hurtTime <= 3)) {
                val nearest = target.boundingBox[mc.player!!.eyePos]
                if (mc.player!!.eyePos.squaredDistanceTo(aimVec) > reach * reach && mc.player!!.eyePos.squaredDistanceTo(nearest) <= reach * reach) {
                    return true
                }
            }
            return false
        }

        override val key = name
    }

    enum class Priority : OptionEnum {
        FOV {
            override fun getEntityValue(entity: Entity): Float {
                val rotation = mc.player!!.eyePos[entity.boundingBox[mc.player!!.eyePos]]
                val deltaPitch =
                    MathHelper.wrapDegrees(rotation.x - if (fovRotation == "Server") current.y else mc.player!!.pitch)
                val deltaYaw =
                    MathHelper.wrapDegrees(rotation.y - if (fovRotation == "Server") current.x else mc.player!!.yaw)
                return -(MathHelper.square(deltaPitch) + MathHelper.square(deltaYaw))
            }
        },
        Distance {
            override fun getEntityValue(entity: Entity) = entity.squaredDistanceTo(mc.player).toFloat()
        },
        Health {
            override fun getEntityValue(entity: Entity) = (entity as LivingEntity).health
        },
        HurtTime {
            override fun getEntityValue(entity: Entity) = (entity as LivingEntity).hurtTime.toFloat()
        },
        Random {
            override fun getEntityValue(entity: Entity) = Schizoid.random.nextFloat()
        };

        abstract fun getEntityValue(entity: Entity): Float

        override val key = name
    }
}
