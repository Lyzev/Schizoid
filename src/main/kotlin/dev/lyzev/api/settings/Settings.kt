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

object SettingInitializer : EventListener {

    override val shouldHandleEvents = true

    init {
        on<StartupEvent> {
            val config = Schizoid.root.resolve("settings.json")
            if (!config.exists())
                return@on
            val root = JsonParser.parseString(config.readText()).asJsonArray
            root.forEach {
                if (it.isJsonObject) {
                    val obj = it.asJsonObject
                    val setting = SettingManager.get(obj["class"].asString, obj["type"].asString, obj["name"].asString)
                    if (setting !is ClientSetting<*>)
                        return@forEach
                    setting.load(obj["value"].asJsonObject)
                }
            }
        }

        on<ShutdownEvent> {
            val root = JsonArray()
            SettingManager.settings.forEach {
                if (it !is ClientSetting<*>)
                    return@forEach
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

abstract class ClientSetting<T>(
    container: KClass<*>, name: String, value: T, hidden: () -> Boolean = { false }, onChange: (T) -> Unit = {}
) : Setting<T>(container, name, value, hidden, onChange) {

    abstract fun load(value: JsonObject)

    abstract fun save(value: JsonObject)

    abstract fun render()
}

