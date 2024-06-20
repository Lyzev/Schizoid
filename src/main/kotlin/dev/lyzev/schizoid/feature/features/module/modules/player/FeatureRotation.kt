/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.player

import dev.lyzev.api.events.*
import dev.lyzev.api.glfw.GLFWKey
import dev.lyzev.api.setting.settings.slider
import dev.lyzev.api.setting.settings.switch
import dev.lyzev.api.settings.Setting.Companion.neq
import dev.lyzev.schizoid.feature.IFeature
import net.minecraft.entity.Entity
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import org.joml.Vector2f

object FeatureRotation : IFeature, EventListener {

    private val current = Vector2f()
    private val last = Vector2f()

    val clientSide by switch("Client Side", "Visualize rotation client side.", true)
    val revert by switch("Revert", "Revert smooth to the actual yaw and pitch", true)
    val revertWeight by slider("Revert Weight", "Revert smooth to the actual yaw and pitch", 80, 1, 100, "%%", hide = ::revert neq true)

    val movementCorrection by switch("Movement Correction", "Corrects movement.", true)
    val silentMovementCorrection by switch("Silent Movement Correction", "Corrects movement silently.", true, hide = ::movementCorrection neq true)

    override val name = "Rotation"
    override val desc = "Rotates the player's view"
    override var keybinds = setOf<GLFWKey>()
    override val category = IFeature.Category.PLAYER

    override fun keybindReleased() {}
    override val shouldHandleEvents = true

    init {
        // Rotate on frame
        on<EventSwapBuffers> {
            if (!isIngame || mc.player == null) {
                return@on
            }
            val event = EventRotationGoal()
            event.fire()
            if (event.goal == null && MathHelper.wrapDegrees(current.distance(mc.player!!.yaw, mc.player!!.pitch)) > 5 && revert) {
                event.weight = revertWeight / 100f
                event.goal = mc.player!!.rotationVecClient
            }
            if (event.goal != null) {
                val goal = event.goal!!
                // TODO: Implement rotation to goal
            } else {
                // TODO: Go back to actual yaw and pitch and correct GCD if needed
            }
        }
        var cachedYaw = 0f // actual yaw for silent movement correction
        // Set server side Rotation
        on<EventClientPlayerEntityTick> { event ->
            cachedYaw = event.player.yaw
            event.player.yaw = current.x
            event.player.pitch = current.y
            event.player.prevYaw = last.x
            event.player.prevPitch = last.y
            last.set(current)
        }
        // Crosshair target correction
        on<EventUpdateCrosshairTarget> { event ->
            event.camera.yaw = current.x
            event.camera.pitch = current.y
            event.camera.prevYaw = last.x
            event.camera.prevPitch = last.y
        }
        // Movement correction
        on<EventUpdateVelocity> { event ->
            if (!movementCorrection) {
                return@on
            }
            event.yaw = current.x
        }
        // Silent movement correction
        val speed = 0.02f // dummy value
        on<EventKeyboardInputTick> { event ->
            if (!movementCorrection || !silentMovementCorrection || mc.player == null) {
                return@on
            }
            // Bruteforce the nearest possible movement input to the real one
            var input = Vec3d(event.input.movementSideways.toDouble(), 0.0, event.input.movementForward.toDouble())
            val actual = Entity.movementInputToVelocity(input, speed, cachedYaw)
            var nearest: Vec3d? = null
            var minDiff = Double.MAX_VALUE
            for (x in -1..1) {
                for (z in -1..1) {
                    input = Vec3d(x.toDouble(), 0.0, z.toDouble())
                    val diff = actual.distanceTo(Entity.movementInputToVelocity(input, speed, current.x))
                    if (diff <= minDiff || nearest == null) {
                        minDiff = diff
                        nearest = input
                    }
                }
            }
            if (nearest != null) {
                event.input.movementSideways = nearest.x.toFloat()
                event.input.movementForward = nearest.z.toFloat()
            }
        }
        // Visualize rotation client side (TODO: Fix first person hand)
        on<EventClientPlayerEntityRender> {
            if (clientSide) {
                it.headYaw = current.x
                it.prevHeadYaw = last.x
                it.pitch = current.y
                it.prevPitch = last.y
            }
        }
    }
}
