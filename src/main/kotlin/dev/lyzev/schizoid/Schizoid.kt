/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid

import dev.lyzev.api.events.EventShutdown
import dev.lyzev.api.events.EventStartup
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

    // The unique identifier of the mod.
    const val MOD_ID = "schizoid"

    // The metadata of the mod.
    val METADATA = FabricLoader.getInstance().getModContainer(MOD_ID).get().metadata

    // The name of the mod.
    val MOD_NAME = METADATA.name

    // The version of the mod.
    val MOD_VERSION = METADATA.version.friendlyString

    // The list of authors contributing to the mod.
    @Suppress("SpellCheckingInspection")
    val MOD_AUTHORS = METADATA.authors

    // Whether the mod is running in a continuous integration environment.
    val CI = System.getProperty("CI").toBooleanStrict()

    // The root directory of the Schizoid mod, used for storing mod-specific data.
    val root = File(
        MinecraftClient.getInstance().runDirectory, MOD_ID
    ).also { if (!it.exists()) it.mkdir() }

    // The logger for the Schizoid mod.
    val logger: Logger = LogManager.getLogger(MOD_ID)

    // The Minecraft client instance.
    val mc = MinecraftClient.getInstance()

    /**
     * Initialize the Schizoid mod.
     */
    override fun onInitializeClient() {
        val init = System.currentTimeMillis()
        runCatching {
            // Initialize the Schizoid mod, log mod initialization information, initialize settings, register a shutdown hook for cleanup, and fire the startup event.
            logger.info("Initializing Schizoid v$MOD_VERSION by ${MOD_AUTHORS.joinToString(" & ") { it.name }}...")
            if (CI)
                logger.warn("Running in a continuous integration environment!")
            SettingInitializer
            FeatureManager
            Runtime.getRuntime().addShutdownHook(Thread { EventShutdown.fire() })
            EventStartup.fire()
            logger.info("Initialized ${FeatureManager.features.size} features!")
        }.onSuccess { // Log successful initialization.
            logger.info("Initialized Schizoid in ${System.currentTimeMillis() - init}ms!")
        }.onFailure { exception -> // Log initialization failure and the associated exception.
            logger.error("Failed to initialize Schizoid!", exception)
        }
    }
}
