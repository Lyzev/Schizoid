/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid

import dev.lyzev.api.events.EventListener
import dev.lyzev.api.events.EventShutdown
import dev.lyzev.api.events.EventStartup
import dev.lyzev.api.events.on
import dev.lyzev.api.imgui.ImGuiLoader
import dev.lyzev.api.setting.SettingInitializer
import dev.lyzev.api.theme.OSTheme
import dev.lyzev.schizoid.Schizoid.CI
import dev.lyzev.schizoid.Schizoid.METADATA
import dev.lyzev.schizoid.Schizoid.MOD_AUTHORS
import dev.lyzev.schizoid.Schizoid.MOD_ID
import dev.lyzev.schizoid.Schizoid.MOD_NAME
import dev.lyzev.schizoid.Schizoid.MOD_VERSION
import dev.lyzev.schizoid.Schizoid.logger
import dev.lyzev.schizoid.Schizoid.mc
import dev.lyzev.schizoid.Schizoid.root
import dev.lyzev.schizoid.feature.FeatureManager
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.metadata.Person
import net.minecraft.client.MinecraftClient
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.util.*

/**
 * Elevate your Minecraft gameplay with this free and feature-rich client built with Fabric API and utilizing mixin-based injection techniques.
 *
 * @property MOD_ID The unique identifier of the mod.
 * @property METADATA The metadata of the mod.
 * @property MOD_NAME The name of the mod.
 * @property MOD_VERSION The version of the mod.
 * @property MOD_AUTHORS The list of authors contributing to the mod.
 * @property CI Whether the mod is running in a continuous integration environment.
 * @property root The root directory of the Schizoid mod, used for storing mod-specific data.
 * @property logger The logger for the Schizoid mod.
 * @property mc The Minecraft client instance.
 */
object Schizoid : EventListener {

    private val properties = Properties().apply {
        ClassLoader.getSystemResourceAsStream("app.properties").use {
            load(it)
        }
    }

    /**
     * The unique identifier of the mod.
     */
    val MOD_ID = properties.getProperty("MOD_ID")

    /**
     * The metadata of the mod.
     */
    val METADATA = FabricLoader.getInstance().getModContainer(MOD_ID).get().metadata

    /**
     * The name of the mod.
     */
    val MOD_NAME = METADATA.name

    /**
     * The version of the mod.
     */
    val MOD_VERSION = METADATA.version.friendlyString

    /**
     * The list of authors contributing to the mod.
     */
    val MOD_AUTHORS: MutableCollection<Person> = METADATA.authors

    /**
     * Whether the mod is running in a continuous integration environment.
     */
    val CI = properties.getProperty("CI")?.toBooleanStrict() ?: false
    val DEVELOPER_MODE = System.getProperty("fabric.development")?.toBoolean() ?: false

    /**
     * The Minecraft client instance.
     */
    val mc = MinecraftClient.getInstance()

    /**
     * The root directory of the Schizoid mod, used for storing mod-specific data.
     */
    val root = File(
        mc.runDirectory, MOD_ID
    ).apply { if (!exists()) mkdir() }

    /**
     * The logger for the Schizoid mod.
     */
    val logger: Logger = LogManager.getLogger(MOD_ID)


    /**
     * Initialize the Schizoid mod.
     */
    fun onInitializeClient() {
        ImGuiLoader // Load ImGui, because of [dev.lyzev.api.events.EventGlfwInit] event.
        on<EventStartup> {
            val init = System.currentTimeMillis()
            runCatching {
                // Initialize the Schizoid mod, log mod initialization information, initialize settings, register a shutdown hook for cleanup, and fire the startup event.
                logger.info("Initializing Schizoid v$MOD_VERSION by ${MOD_AUTHORS.joinToString(" & ") { it.name }}...")
                if (CI)
                    logger.warn("Running in a continuous integration environment!")
                if (DEVELOPER_MODE)
                    logger.warn("Running in developer mode!")
                FeatureManager
                SettingInitializer
                OSTheme.startListenForUpdatesThread()
                Runtime.getRuntime().addShutdownHook(Thread { EventShutdown.fire() })
                logger.info("Initialized ${FeatureManager.features.size} features!")
            }.onSuccess { // Log successful initialization.
                logger.info("Initialized Schizoid in ${System.currentTimeMillis() - init}ms!")
            }.onFailure { exception -> // Log initialization failure and the associated exception.
                logger.error("Failed to initialize Schizoid!", exception)
            }
        }
    }

    override val shouldHandleEvents: Boolean
        get() = true
}
