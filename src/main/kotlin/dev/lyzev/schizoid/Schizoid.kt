/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid

import dev.lyzev.api.events.ShutdownEvent
import dev.lyzev.api.events.StartupEvent
import dev.lyzev.api.setting.SettingInitializer
import dev.lyzev.schizoid.feature.FeatureManager
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File

/**
 * Elevate your Minecraft gameplay with this free and feature-rich client built with Fabric API and utilizing mixin-based injection techniques.
 */
object Schizoid : ClientModInitializer {

    // The unique identifier for the mod.
    const val MOD_ID = "schizoid"

    val METADATA = FabricLoader.getInstance().getModContainer(MOD_ID).get().metadata

    // The version of the mod.
    val VERSION = METADATA.version.friendlyString

    // The list of authors contributing to the mod.
    @Suppress("SpellCheckingInspection")
    val AUTHORS = METADATA.authors

    // The root directory of the Schizoid mod, used for storing mod-specific data.
    val root = File(
        MinecraftClient.getInstance().runDirectory, MOD_ID
    ).also { if (!it.exists()) it.mkdir() }

    // The logger for the Schizoid mod.
    val logger: Logger = LogManager.getLogger(MOD_ID)

    /**
     * Initialize the Schizoid mod.
     */
    override fun onInitializeClient() {
        val init = System.currentTimeMillis()
        runCatching {
            // Initialize the Schizoid mod, log mod initialization information, initialize settings, register a shutdown hook for cleanup, and fire the startup event.
            logger.info("Initializing Schizoid v$VERSION by ${AUTHORS.joinToString(" & ") { it.name }}...")
            SettingInitializer
            FeatureManager
            Runtime.getRuntime().addShutdownHook(Thread { ShutdownEvent.fire() })
            StartupEvent.fire()
            TODO("Finish client base before initializing!")
        }.onSuccess { // Log successful initialization.
            logger.info("Initialized Schizoid in ${System.currentTimeMillis() - init}ms!")
        }.onFailure { exception -> // Log initialization failure and the associated exception.
            logger.error("Failed to initialize Schizoid!", exception)
        }
    }
}
