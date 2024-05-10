/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.movement

import dev.lyzev.api.events.EventClientPlayerEntityTick
import dev.lyzev.api.events.EventListener
import dev.lyzev.api.events.on
import dev.lyzev.api.setting.settings.slider
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.ModuleToggleable
import net.minecraft.entity.Entity
import net.minecraft.entity.vehicle.BoatEntity
import net.minecraft.util.math.Vec3d

object ModuleToggleableBoatBounce : ModuleToggleable("Boat Bounce", "Makes boats bounce.", category = IFeature.Category.MOVEMENT), EventListener {

    val bounce by slider("Bounce", "Sets the bounce of the boat.", 10, 5, 15)

    init {
        on<EventClientPlayerEntityTick> { event ->
            val vehicle = event.player.vehicle
            if (vehicle is BoatEntity) {
                val vel = Entity.movementInputToVelocity(
                    Vec3d(
                        event.player.sidewaysSpeed.toDouble(),
                        event.player.upwardSpeed.toDouble(),
                        event.player.forwardSpeed.toDouble()
                    ),
                    ((if (mc.options.sprintKey.isPressed) .1f else .05f) * bounce / 2f),
                    event.player.yaw
                )
                vehicle.setPosition(
                    vehicle.x + vel.getX(),
                    vehicle.y + vel.getY() + (if (mc.options.sprintKey.isPressed) 1f else .5f) * bounce / 10f,
                    vehicle.z + vel.getZ()
                )
            }
        }
    }

    override val shouldHandleEvents: Boolean
        get() = isEnabled
}
