/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.world

import dev.lyzev.api.events.EventClientPlayerEntityTick
import dev.lyzev.api.events.EventListener
import dev.lyzev.api.events.EventReceivePacket
import dev.lyzev.api.events.on
import dev.lyzev.api.setting.settings.slider
import dev.lyzev.schizoid.feature.features.module.ModuleToggleable
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket
import net.minecraft.world.World


object ModuleToggleableAmbience : ModuleToggleable("Ambience", "Changes to world time/weather!", category = Category.WORLD), EventListener {

    private val time by slider("Time", "The time of day.", World.field_30969 / 2, 0, World.field_30969, unit = "ticks")
    private val rainGradient by slider("Rain Gradient", "The rain gradient.", 0f, 0f, 1f)
    private val thunderGradient by slider("Thunder Gradient", "The thunder gradient.", 0f, 0f, 1f)

    override val shouldHandleEvents: Boolean
        get() = isEnabled

    init {
        on<EventClientPlayerEntityTick> {
            mc.world?.timeOfDay = time.toLong()
            mc.world?.setRainGradient(rainGradient)
            mc.world?.setThunderGradient(thunderGradient)
        }

        on<EventReceivePacket> {
            if (it.packet is WorldTimeUpdateS2CPacket)
                it.packet.timeOfDay = time.toLong()
        }
    }
}
