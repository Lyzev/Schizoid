/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.setting

import dev.lyzev.api.events.EventListener
import dev.lyzev.api.events.EventShutdown
import dev.lyzev.api.events.on
import dev.lyzev.api.settings.SettingManager
import dev.lyzev.schizoid.Schizoid
import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName

/**
 * Singleton object responsible for initializing and managing settings on startup and shutdown.
 */
object SettingInitializer : EventListener {

    // Indicates whether this event listener should handle events.
    override val shouldHandleEvents = true

    val json = Json {
        prettyPrint = true
    }

    @Serializable
    data class ClientSettingJson(
        val `class`: String,
        val type: String,
        val name: String,
        val value: JsonElement
    )

    init {
        // Loads the client settings from a "settings.json" file.
        val settingsConfigFile = Schizoid.root.resolve("settings.json")
        if (settingsConfigFile.exists()) {
            json.decodeFromString<List<ClientSettingJson>>(settingsConfigFile.readText()).forEach {
                val setting = SettingManager.settings.firstOrNull { i -> i.container.jvmName == it.`class` && i::class.jvmName == it.type && i.name == it.name }
                if (setting !is SettingClient<*>) return@forEach
                setting.load(it.value)
            }
        }

        /**
         * Handles the shutdown event by saving the modified client settings into a "settings.json" file.
         *
         * @param E The [ShutdownEvent] triggered during application shutdown.
         */
        on<EventShutdown> {
            val data = SettingManager.settings.filterIsInstance<SettingClient<*>>().map { ClientSettingJson(it.container.jvmName, it::class.jvmName, it.name, it.save()) }
            Schizoid.root.resolve("settings.json").writeText(json.encodeToString(data))
        }
    }
}
