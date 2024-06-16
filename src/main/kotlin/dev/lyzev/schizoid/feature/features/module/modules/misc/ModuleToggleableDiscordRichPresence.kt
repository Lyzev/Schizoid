/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.misc

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.jagrosh.discordipc.IPCClient
import com.jagrosh.discordipc.IPCListener
import com.jagrosh.discordipc.entities.Packet
import com.jagrosh.discordipc.entities.RichPresence
import com.jagrosh.discordipc.entities.User
import com.jagrosh.discordipc.entities.pipe.PipeStatus
import dev.lyzev.schizoid.Schizoid
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.ModuleToggleable


object ModuleToggleableDiscordRichPresence : ModuleToggleable(
    "Discord Rich Presence", "Show on Discord that you're using Schizoid.", category = IFeature.Category.MISC
) {

    private var client: IPCClient? = null
    private val timeStamp = System.currentTimeMillis()

    override fun onEnable() {
        runCatching {
            client = IPCClient(1243687298126843954L)
            client!!.setListener(object : IPCListener {

                override fun onPacketSent(client: IPCClient?, packet: Packet?) {
                    Schizoid.logger.info("Discord Rich Presence sent a packet.")
                }

                override fun onPacketReceived(client: IPCClient?, packet: Packet?) {
                    Schizoid.logger.info("Discord Rich Presence received a packet.")
                }

                override fun onActivityJoin(client: IPCClient?, secret: String?) {
                    Schizoid.logger.info("Discord Rich Presence is joining.")
                }

                override fun onActivitySpectate(client: IPCClient?, secret: String?) {
                    Schizoid.logger.info("Discord Rich Presence is spectating.")
                }

                override fun onActivityJoinRequest(client: IPCClient?, secret: String?, user: User?) {
                    Schizoid.logger.info("Discord Rich Presence is requesting to join.")
                }

                override fun onReady(client: IPCClient) {
                    val builder = RichPresence.Builder()
                    builder.setState("Elevating my Minecraft gameplay with ${Schizoid.MOD_NAME}.")
                        .setDetails("${Schizoid.MOD_NAME} v${Schizoid.MOD_VERSION}")
                        .setStartTimestamp(timeStamp).setLargeImage("client",
                            "${Schizoid.MOD_NAME} v${Schizoid.MOD_VERSION} by ${Schizoid.MOD_AUTHORS.joinToString(" & ") { it.name }}")
                        .setButtons(JsonArray().apply {
                            add(JsonObject().apply {
                                addProperty("label", "Download")
                                addProperty("url", "https://github.com/Lyzev/Schizoid")
                            })
                            add(JsonObject().apply {
                                addProperty("label", "Discord")
                                addProperty("url", "https://lyzev.dev/discord")
                            })
                        })
                    client.sendRichPresence(builder.build())
                    Schizoid.logger.info("Discord Rich Presence is ready.")
                }

                override fun onClose(client: IPCClient?, json: JsonObject?) {
                    Schizoid.logger.info("Discord Rich Presence is closed.")
                }

                override fun onDisconnect(client: IPCClient?, t: Throwable?) {
                    Schizoid.logger.info("Discord Rich Presence is disconnected.")
                }
            })
            client?.connect()
        }.onFailure {
            Schizoid.logger.error("Failed to enable Discord Rich Presence.", it)
        }.onSuccess {
            Schizoid.logger.info("Discord Rich Presence enabled.")
        }
    }

    override fun onDisable() {
        if (client == null || client!!.status != PipeStatus.CONNECTED) {
            return
        }
        runCatching {
            client!!.close()
            client = null
        }.onFailure {
            Schizoid.logger.error("Failed to close Discord Rich Presence.", it)
        }.onSuccess {
            Schizoid.logger.info("Discord Rich Presence closed.")
        }
    }
}
