/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.combat

import dev.lyzev.api.events.EventListener
import dev.lyzev.api.events.EventRenderImGuiContent
import dev.lyzev.api.events.EventUpdateCrosshairTargetTick
import dev.lyzev.api.events.on
import dev.lyzev.api.math.getNearest
import dev.lyzev.api.opengl.Render
import dev.lyzev.api.setting.settings.slider
import dev.lyzev.api.setting.settings.switch
import dev.lyzev.api.settings.Setting.Companion.neq
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.ModuleToggleable
import dev.lyzev.schizoid.feature.features.module.modules.player.FeatureRotation
import imgui.ImGui.getForegroundDrawList
import imgui.ImGui.getMainViewport
import net.minecraft.client.option.KeyBinding
import net.minecraft.entity.LivingEntity
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import org.joml.Vector2f
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.tan

object ModuleToggleableTriggerBot : ModuleToggleable(
    "Trigger Bot", "Automatically attacks entities in the player's crosshair.", category = IFeature.Category.COMBAT
), EventListener {

    val miss by switch("Miss", "Hits in the air to simulate a miss.", false)
    val fov by slider(
        "FOV",
        "If the entity is within the FOV, but not in the crosshair, it will simulate a miss.",
        30,
        1,
        256,
        "°",
        hide = ::miss neq true
    )



    override val shouldHandleEvents
        get() = isEnabled && mc.player != null && mc.crosshairTarget != null

    fun Vec3d.getRotation(target: Vec3d): Vector2f {
        val x = target.x - x
        val y = target.y - y
        val z = target.z - z
        val yaw = MathHelper.wrapDegrees(Math.toDegrees(MathHelper.atan2(z, x)).toFloat() - 90f)
        val pitch = MathHelper.wrapDegrees(-Math.toDegrees(MathHelper.atan2(y, sqrt(x.pow(2) + z.pow(2)))).toFloat())
        return Vector2f(yaw, pitch)
    }

    init {
        // Attack the entity in the player's crosshair.
        on<EventUpdateCrosshairTargetTick> {
            if (mc.attackCooldown <= 0 && mc.player!!.getAttackCooldownProgress(1f) == 1f) {
                if (mc.targetedEntity != null) {
                    KeyBinding.onKeyPressed(mc.options.attackKey.boundKey)
                } else {
                    if (miss) { // TODO: Fix FOV check and move rotation utils to dev.lyzev.api.math.Math
                        val maxDistance = mc.player!!.entityInteractionRange
                        val rotation = Vector2f(FeatureRotation.current)
                        rotation.set(MathHelper.wrapDegrees(rotation.x), MathHelper.clamp(rotation.y, -90f, 90f))
                        mc.world?.entities?.filter { it is LivingEntity && mc.player!!.squaredDistanceTo(it) <= maxDistance * maxDistance && mc.player!!.canSee(it) }?.map { it.boundingBox.getNearest(mc.cameraEntity!!.eyePos).getRotation(mc.cameraEntity!!.eyePos) }?.minByOrNull {
                            it.distanceSquared(rotation)
                        }?.let {
                            if (abs(MathHelper.wrapDegrees(it.distance(rotation)) / 2.0) <= fov) {
                                KeyBinding.onKeyPressed(mc.options.attackKey.boundKey)
                            }
                        }
                    }
                }
            }
        }

        on<EventRenderImGuiContent> {
            if (!mc.gameRenderer.camera.isThirdPerson) {
                val fov = mc.gameRenderer.getFov(mc.gameRenderer.camera, Render.tickDelta, true)
                val radius = (tan(
                    Math.toRadians((this.fov / 256.0) * fov / 2.0).toFloat()
                ) / tan(Math.toRadians(fov / 2.0)).toFloat()) * getMainViewport().sizeX
                if (radius > 0) {
                    getForegroundDrawList().addCircle(
                        getMainViewport().centerX,
                        getMainViewport().centerY,
                        radius,
                        -1,
                        360,
                        2f
                    )
                }
            }
        }
    }
}
