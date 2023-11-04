/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.setting

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dev.lyzev.api.events.EventListener
import dev.lyzev.api.events.ShutdownEvent
import dev.lyzev.api.events.StartupEvent
import dev.lyzev.api.events.on
import dev.lyzev.api.settings.SettingManager
import dev.lyzev.schizoid.Schizoid
import kotlin.reflect.jvm.jvmName

/**
 * Singleton object responsible for initializing and managing settings on startup and shutdown.
 */
object SettingInitializer : EventListener {

    // Indicates whether this event listener should handle events.
    override val shouldHandleEvents = true

    init {

        /**
         * Initializes the settings during the startup event. It loads settings from the "settings.json" file,
         * if it exists, and applies them to corresponding [ClientSetting] instances.
         *3
         * @param E The [StartupEvent] triggered during application startup.
         */
        on<StartupEvent> {
            val config = Schizoid.root.resolve("settings.json")
            if (!config.exists()) return@on
            val root = JsonParser.parseString(config.readText()).asJsonArray
            root.forEach {
                if (it.isJsonObject) {
                    val obj = it.asJsonObject
                    val setting = SettingManager.get(obj["class"].asString, obj["type"].asString, obj["name"].asString)
                    if (setting !is ClientSetting<*>) return@forEach
                    setting.load(obj["value"].asJsonObject)
                }
            }
        }

        /**
         * Handles the shutdown event by saving the modified client settings into a "settings.json" file.
         *
         * @param E The [ShutdownEvent] triggered during application shutdown.
         */
        on<ShutdownEvent> {
            val root = JsonArray()
            SettingManager.settings.forEach {
                if (it !is ClientSetting<*>) return@forEach
                val setting = JsonObject()
                setting.addProperty("class", it.container.jvmName)
                setting.addProperty("type", it::class.jvmName)
                setting.addProperty("name", it.name)
                setting.add("value", JsonObject().also { value ->
                    it.save(value)
                })
                root.add(setting)
            }
            Schizoid.root.resolve("settings.json").writeText(root.toString())
        }
    }
}
