/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.util

import dev.lyzev.api.events.EventListener
import dev.lyzev.api.events.EventPacket
import dev.lyzev.api.events.on
import dev.lyzev.api.setting.settings.multiText
import dev.lyzev.api.setting.settings.slider
import dev.lyzev.schizoid.Schizoid
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.ModuleToggleable
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket
import kotlin.concurrent.thread

object ModuleToggleableAutoText :
    ModuleToggleable("AutoText", "Automatically sends messages based on triggers.", category = IFeature.Category.UTIL),
    EventListener {

    val triggers by multiText(
        "Triggers", "The triggers to listen for in GameMessageS2CPacket.", mutableSetOf(
            "Your Overall Winstreak:",
            "1. Place -",
            "1st Place -",
            "1. Killer -",
            "1st Killer -",
            " - Damage Dealt -",
            "Winning Team -",
            "1st -",
            "Winners:",
            "Winner:",
            "Winning Team:",
            " won the game!",
            "Top Seeker:",
            "1st Place:",
            "Last team standing!",
            "Winner #1 (",
            "Top Survivors",
            "Winners -",
            "Sumo Duel -",
            "Most Wool Placed -"
        )
    )
    val messages by multiText("Messages", "The messages to send.", mutableSetOf("/ac GG", "/ac Good Game", "/ac gg"))
    val delay by slider("Delay", "The delay before sending the message.", 200, 0, 5000, "ms")

    override val shouldHandleEvents
        get() = isEnabled && triggers.isNotEmpty() && messages.isNotEmpty()

    init {
        on<EventPacket> { event ->
            if (event.packet is GameMessageS2CPacket) {
                val message = event.packet.content.string
                if (message != null) {
                    for (i in triggers.indices) {
                        if (message.contains(triggers.elementAt(i))) {
                            thread {
                                Thread.sleep(delay.toLong())
                                mc.networkHandler?.sendChatMessage(messages.random())
                            }
                            break
                        }
                    }
                }
            }
        }
    }
}
