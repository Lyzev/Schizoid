/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid

import dev.lyzev.api.events.ShutdownEvent
import dev.lyzev.api.events.StartupEvent
import dev.lyzev.api.settings.SettingInitializer
import net.fabricmc.api.ModInitializer
import net.minecraft.client.MinecraftClient
import org.apache.logging.log4j.LogManager
import java.io.File

object Schizoid : ModInitializer {

    const val MOD_ID = "schizoid"
    const val VERSION = "1.0.0"
    val AUTHORS = listOf("Lyzev", "Redstonecrafter0", "Sumandora")

    val root = File(MinecraftClient.getInstance().runDirectory, MOD_ID).also { if (!it.exists()) it.mkdir() }
    val logger = LogManager.getLogger(MOD_ID)

    override fun onInitialize() {
        val init = System.currentTimeMillis()
        runCatching {
            logger.info("Initializing Schizoid v$VERSION by ${AUTHORS.joinToString(" & ")}...")
            SettingInitializer
            Runtime.getRuntime().addShutdownHook(Thread { ShutdownEvent.fire() })
            StartupEvent.fire()
            TODO("Finish client base before initializing!")
        }.onSuccess {
            logger.info("Initialized Schizoid v$VERSION by ${AUTHORS.joinToString(" & ")} in ${System.currentTimeMillis() - init}ms!")
        }.onFailure {
            logger.error("Failed to initialize Schizoid!", it)
        }
    }
}