/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.settings

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dev.lyzev.api.events.EventListener
import dev.lyzev.api.events.ShutdownEvent
import dev.lyzev.api.events.StartupEvent
import dev.lyzev.api.events.on
import dev.lyzev.schizoid.Schizoid
import kotlin.reflect.KClass
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

/**
 * Abstract class representing a client setting.
 *
 * @param T The type of value this setting holds.
 * @property container The class of the settings container.
 * @property name The name of the setting.
 * @property value The default value of the setting.
 * @property hidden A lambda function indicating if the setting should be hidden from the user interface.
 * @property onChange A lambda function to be called when the setting value changes.
 */
abstract class ClientSetting<T>(
    container: KClass<*>, name: String, desc: String?, value: T, hidden: () -> Boolean, onChange: (T) -> Unit
) : Setting<T>(container, name, desc, value, hidden, onChange) {

    /**
     * Render the setting for the user interface.
     */
    abstract fun render()

    /**
     * Load the setting value from a JSON object.
     * @param value The JSON object containing the setting value.
     */
    abstract fun load(value: JsonObject)

    /**
     * Save the setting value to a JSON object.
     * @param value The JSON object to store the setting value.
     */
    abstract fun save(value: JsonObject)
}

/**
 * A specific implementation of the [Setting] class for boolean settings.
 *
 * @param container The class of the settings container where this setting belongs.
 * @param name The name of the setting.
 * @param value The initial value of the boolean setting.
 * @param hide A lambda function that determines whether this setting is hidden or not.
 * @param change A lambda function that will be called when the value of the setting changes.
 */
class BooleanSetting(
    container: KClass<*>, name: String, desc: String? = null, value: Boolean, hide: () -> Boolean = { false }, change: (Boolean) -> Unit = {}
) : ClientSetting<Boolean>(container, name, desc, value, hide, change) {

    override fun render() {
    }

    override fun load(value: JsonObject) {
        this.value = value["value"].asBoolean
    }

    override fun save(value: JsonObject) = value.addProperty("value", this.value)
}

/**
 * A specific implementation of the [Setting] class for integer settings.
 *
 * @param container The class of the settings container where this setting belongs.
 * @param name The name of the setting.
 * @param value The initial value of the integer setting.
 * @param hide A lambda function that determines whether this setting is hidden or not.
 * @param change A lambda function that will be called when the value of the setting changes.
 */
class KeySetting(
    container: KClass<*>, name: String, desc: String? = null, value: Int, hide: () -> Boolean = { false }, change: (Int) -> Unit = {}
) : ClientSetting<Int>(container, name, desc, value, hide, change) {

    override fun render() {
    }

    override fun load(value: JsonObject) {
        this.value = value["value"].asInt
    }

    override fun save(value: JsonObject) = value.addProperty("value", this.value)
}

