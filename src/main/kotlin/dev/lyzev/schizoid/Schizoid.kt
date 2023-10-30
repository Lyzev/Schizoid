/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid

import dev.lyzev.api.events.ShutdownEvent
import dev.lyzev.api.events.StartupEvent
import dev.lyzev.api.settings.SettingInitializer
import dev.lyzev.schizoid.feature.FeatureManager
import net.fabricmc.api.ModInitializer
import net.minecraft.client.MinecraftClient
import org.apache.logging.log4j.LogManager
import java.io.File

/**
 * Elevate your Minecraft gameplay with this free and feature-rich client built with Fabric API and utilizing mixin-based injection techniques.
 *
 * @since 1.0.0
 */
object Schizoid : ModInitializer {

    // General information about the mod.
    const val MOD_ID = "schizoid" // The unique identifier for the mod.
    const val VERSION = "1.0.0" // The version of the mod.

    @Suppress("SpellCheckingInspection")
    val AUTHORS = listOf("Lyzev", "Redstonecrafter0", "Sumandora") // The list of authors contributing to the mod.

    // Directories and Logging.
    val root = File(
        MinecraftClient.getInstance().runDirectory, MOD_ID
    ).also { if (!it.exists()) it.mkdir() } // The root directory of the Schizoid mod, used for storing mod-specific data.
    val logger = LogManager.getLogger(MOD_ID) // The logger for the Schizoid mod.

    /**
     * Initialize the Schizoid mod.
     */
    override fun onInitialize() {
        val init = System.currentTimeMillis()
        runCatching {
            // Initialize the Schizoid mod, log mod initialization information, initialize settings, register a shutdown hook for cleanup, and fire the startup event.
            logger.info("Initializing Schizoid v$VERSION by ${AUTHORS.joinToString(" & ")}...")
            SettingInitializer
            FeatureManager
            Runtime.getRuntime().addShutdownHook(Thread { ShutdownEvent.fire() })
            StartupEvent.fire()
            TODO("Finish client base before initializing!")
        }.onSuccess { // Log successful initialization.
            logger.info("Initialized Schizoid v$VERSION by ${AUTHORS.joinToString(" & ")} in ${System.currentTimeMillis() - init}ms!")
        }.onFailure { exception -> // Log initialization failure and the associated exception.
            logger.error("Failed to initialize Schizoid!", exception)
        }
    }
}
