/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.misc

import dev.lyzev.schizoid.Schizoid
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.ModuleToggleable
import meteordevelopment.discordipc.DiscordIPC
import meteordevelopment.discordipc.RichPresence
import kotlin.concurrent.thread


object ModuleToggleableDiscordRichPresence : ModuleToggleable(
    "Discord Rich Presence",
    "Show on Discord that you're using Schizoid.",
    category = IFeature.Category.MISC
) {

    override fun onEnable() {
        if (!DiscordIPC.isConnected() && !DiscordIPC.start(1243687298126843954) {
                Schizoid.logger.info("[$name] Logged in account: " + DiscordIPC.getUser().username)
                val rpc = RichPresence()
                rpc.setStart(System.currentTimeMillis() / 1000L)
                rpc.setLargeImage("client", "Schizoid v" + Schizoid.MOD_VERSION + " by " + Schizoid.MOD_AUTHORS.joinToString(
                    " & "
                ) { it.name })
                rpc.setState("Elevating my Minecraft gameplay with Schizoid.")
                DiscordIPC.setActivity(rpc);
                Schizoid.logger.info("[$name] Finished setting up Discord Rich Presence")
            }) {
            Schizoid.logger.error("[$name] Failed to start Discord IPC")
            thread {
                Schizoid.logger.info("[$name] Retrying in 5 seconds...")
                Thread.sleep(5000)
                Schizoid.logger.info("[$name] Retrying now...")
                if (isEnabled)
                    onEnable()
            }
        }
    }

    override fun onDisable() {
        DiscordIPC.stop();
    }
}
