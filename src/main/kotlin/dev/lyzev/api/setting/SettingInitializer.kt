/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.setting

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dev.lyzev.api.events.EventListener
import dev.lyzev.api.events.EventShutdown
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
        // Loads the client settings from a "settings.json" file.
        val settingsConfigFile = Schizoid.root.resolve("settings.json")
        if (settingsConfigFile.exists()) {
            val root = JsonParser.parseString(settingsConfigFile.readText()).asJsonArray
            root.forEach {
                if (it.isJsonObject) {
                    val obj = it.asJsonObject
                    val setting = SettingManager.get(obj["class"].asString, obj["type"].asString, obj["name"].asString)
                    if (setting !is SettingClient<*>) return@forEach
                    setting.load(obj["value"].asJsonObject)
                }
            }
        }

        /**
         * Handles the shutdown event by saving the modified client settings into a "settings.json" file.
         *
         * @param E The [ShutdownEvent] triggered during application shutdown.
         */
        on<EventShutdown> {
            val root = JsonArray()
            SettingManager.settings.forEach {
                if (it !is SettingClient<*>) return@forEach
                val setting = JsonObject()
                setting.addProperty("class", it.container.jvmName)
                setting.addProperty("type", it::class.jvmName)
                setting.addProperty("name", it.name)
                setting.add("value", JsonObject().also { value ->
                    it.save(value)
                })
                root.add(setting)
            }
            val gson = GsonBuilder().setPrettyPrinting().create()
            Schizoid.root.resolve("settings.json").writeText(gson.toJson(root))
        }
    }
}
