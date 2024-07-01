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
import net.minecraft.util.math.Vec3d
import java.io.File

object ModuleToggleableAimBotDataCollector : ModuleToggleable(
    "Aim Bot Data Collector", "Collect data for Aim Bot.", category = IFeature.Category.COMBAT
), EventListener {

    override val hide = !Schizoid.DEVELOPER_MODE

    override val shouldHandleEvents
        get() = isEnabled

    init {
        var lastHitVec: Vec3d? = null
        var data = 0

        val input = StringBuilder()
        val output = StringBuilder()

        on<EventUpdateCrosshairTargetTick>(Event.Priority.LOWEST) { event ->
            if (mc.targetedEntity is LivingEntity) {
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
                val lookVecEntity = Vec3d.fromPolar(mc.cameraEntity!!.eyePos[mc.targetedEntity!!.eyePos]).normalize().add(1.0, 1.0, 1.0).normalize()
                val lookVec = mc.player!!.rotationVecClient.normalize().add(1.0, 1.0, 1.0).normalize()
                val reach = mc.cameraEntity!!.eyePos.distanceTo(box[mc.cameraEntity!!.eyePos]) / mc.player!!.entityInteractionRange
                val velocityTarget = Vec3d(mc.targetedEntity!!.x - mc.targetedEntity!!.prevX, mc.targetedEntity!!.y - mc.targetedEntity!!.prevY, mc.targetedEntity!!.z - mc.targetedEntity!!.prevZ).normalize().add(1.0, 1.0, 1.0).normalize()
                val velocityPlayer = Vec3d(mc.player!!.x - mc.player!!.prevX, mc.player!!.y - mc.player!!.prevY, mc.player!!.z - mc.player!!.prevZ).normalize().add(1.0, 1.0, 1.0).normalize()
                input.appendLine("$reach;${hitVec.x};${hitVec.y};${hitVec.z};${lookVecEntity.x};${lookVecEntity.y};${lookVecEntity.z};$wasAiming;${velocityTarget.x};${velocityTarget.y};${velocityTarget.z};${velocityPlayer.x};${velocityPlayer.y};${velocityPlayer.z};${lookVec.x};${lookVec.y};${lookVec.z}")
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

        on<EventShutdown> {
            File(Schizoid.root, "input.csv").writeText(input.toString())
            File(Schizoid.root, "output.csv").writeText(output.toString())
        }
    }
}
