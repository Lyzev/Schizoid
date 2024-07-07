/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.combat

import dev.lyzev.api.events.*
import dev.lyzev.api.math.get
import dev.lyzev.schizoid.Schizoid
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.ModuleToggleable
import imgui.ImGui
import net.minecraft.entity.LivingEntity
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Vec3d
import net.minecraft.world.RaycastContext
import java.io.File
import kotlin.math.min

object ModuleToggleableAimBotDataCollector : ModuleToggleable(
    "Aim Bot Data Collector", "Collect data for Aim Bot.", category = IFeature.Category.COMBAT
), EventListener {

    override val hide = !Schizoid.DEVELOPER_MODE

    val input = StringBuilder()
    val output = StringBuilder()

    override val shouldHandleEvents
        get() = isEnabled

    override fun onDisable() {
        File(Schizoid.root, "input.csv").writeText(input.toString())
        File(Schizoid.root, "output.csv").writeText(output.toString())
    }

    init {
        var lastHitVec: Vec3d? = null
        var data = 0

        on<EventUpdateCrosshairTargetTick>(Event.Priority.LOWEST) { event ->
            if (!mc.player!!.isAlive || mc.currentScreen != null) {
                toggle()
                lastHitVec = null
                return@on
            }
            if (mc.targetedEntity is LivingEntity && mc.targetedEntity!!.isAlive) {
                val box = mc.targetedEntity!!.boundingBox
                val crosshairAim = box[mc.crosshairTarget!!.pos]
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
                val lookVecEntity = Vec3d.fromPolar(mc.player!!.eyePos[mc.targetedEntity!!.eyePos]).normalize().add(1.0, 1.0, 1.0).normalize()
                val lookVec = mc.player!!.rotationVecClient.normalize().add(1.0, 1.0, 1.0).normalize()
                val reach = mc.player!!.eyePos.distanceTo(box[mc.player!!.eyePos]) / mc.player!!.entityInteractionRange
                val speedTarget = min(Vec3d(mc.targetedEntity!!.x - mc.targetedEntity!!.prevX, mc.targetedEntity!!.y - mc.targetedEntity!!.prevY, mc.targetedEntity!!.z - mc.targetedEntity!!.prevZ).lengthSquared() / 100.0, 1.0)
                val speedPlayer = min(Vec3d(mc.player!!.x - mc.player!!.prevX, mc.player!!.y - mc.player!!.prevY, mc.player!!.z - mc.player!!.prevZ).lengthSquared() / 100.0, 1.0)
                val velocityTarget = Vec3d(mc.targetedEntity!!.x - mc.targetedEntity!!.prevX, mc.targetedEntity!!.y - mc.targetedEntity!!.prevY, mc.targetedEntity!!.z - mc.targetedEntity!!.prevZ).normalize().add(1.0, 1.0, 1.0).normalize()
                val velocityPlayer = Vec3d(mc.player!!.x - mc.player!!.prevX, mc.player!!.y - mc.player!!.prevY, mc.player!!.z - mc.player!!.prevZ).normalize().add(1.0, 1.0, 1.0).normalize()
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
                input.appendLine("$reach;${hitVec.x};${hitVec.y};${hitVec.z};${lookVecEntity.x};${lookVecEntity.y};${lookVecEntity.z};$wasAiming;${speedTarget};${speedPlayer};${lookVec.x};${lookVec.y};${lookVec.z}${clicks};${raycast.joinToString(";")};${velocityTarget.x};${velocityTarget.y};${velocityTarget.z};${velocityPlayer.x};${velocityPlayer.y};${velocityPlayer.z}")
                lastHitVec = Vec3d((crosshairAim.x - box.minX) / (box.maxX - box.minX), (crosshairAim.y - box.minY) / (box.maxY - box.minY), (crosshairAim.z - box.minZ) / (box.maxZ - box.minZ))
                output.appendLine("${lastHitVec!!.x};${lastHitVec!!.y};${lastHitVec!!.z}")
                data++
            } else {
                lastHitVec = null
            }
        }

        on<EventRenderImGuiContent> {
            ImGui.getForegroundDrawList().addText(ImGui.getMainViewport().centerX, 10f, -1, "Data: $data")
        }
    }
}
