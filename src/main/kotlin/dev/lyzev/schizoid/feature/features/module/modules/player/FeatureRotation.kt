/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.player

import dev.lyzev.api.animation.EasingFunction
import dev.lyzev.api.events.*
import dev.lyzev.api.glfw.GLFWKey
import dev.lyzev.api.math.NoiseGenerator
import dev.lyzev.api.setting.settings.slider
import dev.lyzev.api.setting.settings.switch
import dev.lyzev.api.settings.Setting.Companion.neq
import dev.lyzev.schizoid.Schizoid
import dev.lyzev.schizoid.feature.IFeature
import net.minecraft.client.Mouse
import net.minecraft.entity.Entity
import net.minecraft.network.packet.s2c.play.LookAtS2CPacket
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import org.joml.Vector2f
import kotlin.math.*

object FeatureRotation : IFeature, EventListener {

    val current = Vector2f()
    private val last = Vector2f()
    private var time = System.currentTimeMillis()
    private val noiseGenerator = NoiseGenerator()

    val silent by switch("Silent", "Silent rotation", true)
    val clientSide by switch("Client Side", "Visualize rotation client side.", false, hide = ::silent neq true)

    val minYaw by slider("Min Yaw", "The minimum yaw to rotate.", 1.5f, 0f, 180f, 1, "°")
    val minPitch by slider("Min Pitch", "The minimum pitch to rotate.", 1f, 0f, 180f, 1, "°")

    val revert by switch("Revert", "Revert smooth to the actual yaw and pitch", true)
    val revertDelay by slider(
        "Revert Delay", "The delay before reverting to the actual yaw and pitch", 500, 0, 3000, "ms"
    )
    val revertWeight by slider(
        "Revert Weight", "Revert smooth to the actual yaw and pitch", 80, 1, 100, "%%", hide = ::revert neq true
    )

    val jitter by switch("Jitter", "Jitter the rotation", true)
    val jitterStrength by slider(
        "Jitter Strength", "The strength of the jitter effect.", 5, 0, 100, "%%", hide = ::jitter neq true
    )
    val jitterBasedOnClicks by switch(
        "Jitter Based On Clicks", "Jitter based on click speed.", true, hide = ::jitter neq true
    )

    val movementCorrection by switch("Movement Correction", "Corrects movement.", true)
    val silentMovementCorrection by switch(
        "Silent Movement Correction", "Corrects movement silently.", true, hide = ::movementCorrection neq true
    )

    override val name = "Rotation"
    override val desc = "Rotates the player's view"
    override var keybinds = setOf<GLFWKey>()
    override val category = IFeature.Category.PLAYER

    override fun keybindReleased() {}
    override val shouldHandleEvents = true

    /**
     * Corrects the mouse sensitivity.
     *
     * @see [Mouse.updateMouse]
     *
     * @param correct The correct vector.
     * @param incorrect The incorrect vector.
     */
    fun correctMouseSensitivity(correct: Vector2f, incorrect: Vector2f, client: Boolean) {
        incorrect.set(MathHelper.wrapDegrees(incorrect.x), MathHelper.clamp(incorrect.y, -90f, 90f))
        val mouseSensitivity = (mc.options.mouseSensitivity.value * 0.6f.toDouble() + 0.2f.toDouble()).pow(3.0) * 8.0
        var cursorDeltaX =
            (MathHelper.wrapDegrees(incorrect.x - correct.x) / 0.15f / mouseSensitivity).roundToInt().toDouble()
        var cursorDeltaY = ((incorrect.y - correct.y) / 0.15f / mouseSensitivity).roundToInt().toDouble()
        cursorDeltaX *= mouseSensitivity
        cursorDeltaY *= mouseSensitivity
        incorrect.set(correct.x + cursorDeltaX * 0.15f, MathHelper.clamp(correct.y + cursorDeltaY * 0.15, -90.0, 90.0))
        // Fixes the actual yaw and pitch
        if (!client)
            return
        cursorDeltaX =
            (MathHelper.wrapDegrees(mc.player!!.yaw - correct.x) / 0.15f / mouseSensitivity).roundToInt().toDouble()
        cursorDeltaY = ((mc.player!!.pitch - correct.y) / 0.15f / mouseSensitivity).roundToInt().toDouble()
        cursorDeltaX *= mouseSensitivity
        cursorDeltaY *= mouseSensitivity
        mc.player!!.yaw = correct.x + cursorDeltaX.toFloat() * 0.15f
        mc.player!!.pitch = MathHelper.clamp(correct.y + cursorDeltaY.toFloat() * 0.15f, -90f, 90f)
    }

    /**
     * Gets the rotation from the current vector to the target vector.
     *
     * @see [Entity.lookAt]
     *
     * @param target The target vector.
     * @return The rotation.
     */
    fun Vec3d.getRotation(target: Vec3d): Vector2f {
        val x = target.x - x
        val y = target.y - y
        val z = target.z - z
        val yaw = MathHelper.wrapDegrees(Math.toDegrees(MathHelper.atan2(z, x)).toFloat() - 90f)
        val pitch = MathHelper.wrapDegrees(-Math.toDegrees(MathHelper.atan2(y, sqrt(x.pow(2) + z.pow(2)))).toFloat())
        return Vector2f(yaw, pitch)
    }

    init {
        // Rotate on frame
        var requireRevert = false // revert to actual yaw and pitch
        on<EventSwapBuffers> {
            if (!isIngame || mc.player == null) {
                return@on
            }
            if (mc.currentScreen == null) {
                val event = EventRotationGoal()
                event.fire()
                var isReverting = false
                if (event.goal == null && requireRevert && revert) {
                    if (System.currentTimeMillis() - time > revertDelay || !silent) {
                        event.weight = revertWeight / 100f + 0.5f + Schizoid.random.nextFloat() * .2f + 2f
                        event.goal = mc.player!!.rotationVecClient.multiply(3.0).add(mc.player!!.eyePos)
                        isReverting = true
                    }
                } else if (event.goal != null) {
                    time = System.currentTimeMillis()
                }
                if (event.goal != null) {
                    requireRevert = true
                    val goal = mc.cameraEntity!!.eyePos.getRotation(event.goal!!)
                    // Calculate the delta yaw and pitch
                    var deltaYaw = MathHelper.wrapDegrees(goal.x - current.x)
                    var deltaPitch = goal.y - current.y

                    if (event.instant) {
                        correctMouseSensitivity(current, goal, isReverting)
                        current.set(goal)
                        val pitch = mc.player!!.pitch - current.y
                        val yaw = MathHelper.wrapDegrees(mc.player!!.yaw - current.x)
                        if (MathHelper.square(yaw) + MathHelper.square(pitch) <= 4f) {
                            requireRevert = false
                        }
                        return@on
                    }

                    val weightPitch = EasingFunction.IN_OUT_CIRC(event.weight.toDouble()).toFloat()
                    val weightYaw = EasingFunction.LINEAR(event.weight.toDouble()).toFloat()

                    // TODO: add miss aim
//                    if (!event.force) {
//                        val missOffsetYaw =
//                            Schizoid.random.nextFloat() * (deltaYaw * weightYaw) - deltaYaw * weightYaw / 2f
//                        val missOffsetPitch =
//                            Schizoid.random.nextFloat() * (deltaPitch * weightYaw) - deltaPitch * weightYaw / 2f
//                        deltaYaw += missOffsetYaw
//                        deltaPitch += missOffsetPitch
//                    }

                    val squaredDistance = MathHelper.square(deltaYaw) + MathHelper.square(deltaPitch)
                    val lastFrameDuration = mc.renderTickCounter.lastDuration

                    val minYaw = this.minYaw * (noiseGenerator.noise(System.currentTimeMillis() / 20.0).toFloat() + 1f) / 2f + this.minYaw
                    val minPitch = this.minPitch * (noiseGenerator.noise(System.currentTimeMillis() / 20.0).toFloat() + 1f) / 2f + this.minPitch

                    val maxYaw = max(MathHelper.square(deltaYaw) / squaredDistance * weightYaw * 30, minYaw * lastFrameDuration)
                    val maxPitch = max(MathHelper.square(deltaPitch) / squaredDistance * weightPitch * 30, minPitch * lastFrameDuration)

                    // Calculate the new yaw and pitch
                    var yawJitter =
                        if (jitter) Schizoid.random.nextFloat() * jitterStrength / 100f - jitterStrength / 200f else 0f
                    var pitchJitter =
                        if (jitter) Schizoid.random.nextFloat() * jitterStrength / 100f - jitterStrength / 200f else 0f
                    if (jitterBasedOnClicks) {
                        val cpt = max(mc.options.attackKey.timesPressed / 4f + 0.1f, 1f)
                        yawJitter *= cpt
                        pitchJitter *= cpt
                    }
                    val new = Vector2f(
                        current.x + deltaYaw.coerceIn(-maxYaw, maxYaw) * lastFrameDuration + yawJitter,
                        MathHelper.clamp(
                            current.y + deltaPitch.coerceIn(
                                -maxPitch, maxPitch
                            ) * lastFrameDuration + pitchJitter, -90f, 90f
                        )
                    )
                    correctMouseSensitivity(current, new, isReverting)
                    current.set(new)
                    val pitch = mc.player!!.pitch - current.y
                    val yaw = MathHelper.wrapDegrees(mc.player!!.yaw - current.x)
                    if (MathHelper.square(yaw) + MathHelper.square(pitch) <= 4f) {
                        requireRevert = false
                    }
                } else if (System.currentTimeMillis() - time > revertDelay) {
                    noiseGenerator.setSeed()
                    current.set(mc.player!!.yaw, mc.player!!.pitch)
                }
            }
            if (!silent) {
                mc.player!!.yaw = current.x
                mc.player!!.pitch = current.y
            }
        }
        // Set rotation on packet
        on<EventPacket> { event ->
            if (event.type == EventPacket.Type.S2C) {
                val packet = event.packet
                if (packet is PlayerPositionLookS2CPacket) {
                    current.set(packet.yaw, packet.pitch)
                } else if (packet is LookAtS2CPacket) {
                    val target = packet.getTargetPosition(mc.world)!!;
                    val anchor = packet.selfAnchor
                    val at = anchor.positionAt(mc.player);
                    val deltaX = target.x - at.x;
                    val deltaY = target.y - at.y;
                    val deltaZ = target.z - at.z;
                    val distXZ = sqrt(deltaX * deltaX + deltaZ * deltaZ);
                    current.set(
                        MathHelper.wrapDegrees(MathHelper.atan2(deltaZ, deltaX) * 180f / Math.PI - 90f),
                        MathHelper.wrapDegrees(-MathHelper.atan2(deltaY, distXZ) * 180f / Math.PI)
                    )
                }
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
            var minDistance = Double.MAX_VALUE
            for (x in -1..1) {
                for (z in -1..1) {
                    input = Vec3d(x.toDouble(), 0.0, z.toDouble())
                    val distance = actual.squaredDistanceTo(
                        Entity.movementInputToVelocity(
                            input, speed, current.x
                        )
                    ) // use squared distance to avoid unnecessary sqrt computation
                    if (distance <= minDistance || nearest == null) {
                        minDistance = distance
                        nearest = input
                    }
                }
            }
            if (nearest != null) {
                event.input.movementSideways = nearest.x.toFloat()
                event.input.movementForward = nearest.z.toFloat()
            }
        }
        /**
         * Correct rotation on lerp pos and rotation
         *
         * @see [net.minecraft.entity.Entity.lerpPosAndRotation]
         */
        on<EventLerpPosAndRotation> { event ->
            val delta = 1.0 / event.step.toDouble()
            val newYaw = MathHelper.lerpAngleDegrees(delta, current.x.toDouble(), event.yaw).toFloat()
            val newPitch = MathHelper.lerp(delta, current.y.toDouble(), event.pitch).toFloat()
            current.set(newYaw % 360.0f, newPitch % 360.0f);
        }
        // Visualize rotation client side
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
