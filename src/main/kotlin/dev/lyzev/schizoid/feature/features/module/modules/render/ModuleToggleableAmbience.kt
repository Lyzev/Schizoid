/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.render

import dev.lyzev.api.events.EventClientPlayerEntityTick
import dev.lyzev.api.events.EventListener
import dev.lyzev.api.events.EventPacket
import dev.lyzev.api.events.on
import dev.lyzev.api.setting.settings.slider
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.ModuleToggleable
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket
import net.minecraft.world.World

object ModuleToggleableAmbience :
    ModuleToggleable("Ambience", "Changes to world time/weather!", category = IFeature.Category.RENDER), EventListener {

    /**
     * The time of day, in ticks.
     */
    val time by slider("Time", "The time of day.", World.field_30969 / 2, 0, World.field_30969, unit = "ticks")

    /**
     * The rain gradient, a value between 0 and 1.
     */
    val rainGradient by slider("Rain Gradient", "The rain gradient.", 0f, 0f, 1f)

    /**
     * The thunder gradient, a value between 0 and 1.
     */
    val thunderGradient by slider("Thunder Gradient", "The thunder gradient.", 0f, 0f, 1f)

    override val shouldHandleEvents: Boolean
        get() = isEnabled

    init {
        /**
         * This block of code is executed when the module is initialized.
         * It sets up an event listener that updates the world's time of day, rain gradient, and thunder gradient every time the player entity ticks.
         */
        on<EventClientPlayerEntityTick> {
            mc.world?.timeOfDay = time.toLong()
            mc.world?.setRainGradient(rainGradient)
            mc.world?.setThunderGradient(thunderGradient)
        }

        /**
         * This block of code is executed when the module receives a packet.
         * If the packet is a WorldTimeUpdateS2CPacket, it updates the packet's time of day.
         */
        on<EventPacket> {
            if (it.packet is WorldTimeUpdateS2CPacket)
                it.packet.timeOfDay = time.toLong()
        }
    }
}
