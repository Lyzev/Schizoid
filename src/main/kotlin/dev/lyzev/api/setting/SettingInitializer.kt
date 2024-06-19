/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.setting

import dev.lyzev.api.events.EventListener
import dev.lyzev.api.events.EventSettingChange
import dev.lyzev.api.events.on
import dev.lyzev.api.settings.SettingManager
import dev.lyzev.schizoid.Schizoid
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import java.io.File
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

    var loaded =
        Schizoid.configDir.resolve("loaded.txt").let { if (it.exists() && it.isFile) it.readText() else "default" }
        set(value) {
            field = value
            Schizoid.configDir.resolve("loaded.txt").writeText(value)
        }

    val available: Set<String>
        get() = Schizoid.configDir.listFiles()
            ?.filter { it.isFile && it.name.endsWith(".json") }
            ?.map { it.name.removeSuffix(".json") }
            ?.toMutableSet()
            ?.apply {
                add("default")
                add(loaded)
            }?.toSet() ?: setOf("default", loaded)

    private var loading = false

    fun loadDefaults() {
        SettingManager.settings.filterIsInstance<SettingClient<*>>().forEach { it.reset() }
    }

    fun reload() {
        loading = true
        loadDefaults()
        val settingsConfigFile = Schizoid.configDir.resolve("$loaded.json")
        if (settingsConfigFile.canonicalPath.startsWith(Schizoid.configDir.canonicalPath) && settingsConfigFile.exists() && settingsConfigFile.isFile) {
            json.decodeFromString<List<ClientSettingJson>>(settingsConfigFile.readText()).forEach {
                val setting =
                    SettingManager.settings.firstOrNull { i -> i.container.jvmName == it.`class` && i::class.jvmName == it.type && i.name == it.name }
                if (setting !is SettingClient<*>) return@forEach
                setting.load(it.value)
            }
            loading = false
            SettingManager.settings.filterIsInstance<SettingClient<*>>().forEach { it.configOnChange() }
        }
        loading = false
    }

    fun saveCurrentConfig() {
        val settingsConfigFile = Schizoid.configDir.resolve("$loaded.json")
        if (settingsConfigFile.canonicalPath.startsWith(Schizoid.configDir.canonicalPath)) {
            if (!settingsConfigFile.parentFile.exists() || !settingsConfigFile.parentFile.isDirectory) settingsConfigFile.parentFile.mkdirs()
            val data = SettingManager.settings.filterIsInstance<SettingClient<*>>()
                .map { ClientSettingJson(it.container.jvmName, it::class.jvmName, it.name, it.save()) }
            Schizoid.configDir.resolve("$loaded.json").writeText(json.encodeToString(data))
        }
    }

    fun getConfigFile(name: String): File? {
        val settingsConfigFile = Schizoid.configDir.resolve("$name.json")
        return if (settingsConfigFile.canonicalPath.startsWith(Schizoid.configDir.canonicalPath) && settingsConfigFile.exists() && settingsConfigFile.isFile) {
            settingsConfigFile
        } else {
            null
        }
    }

    init {
        on<EventSettingChange> {
            if (loading) return@on
            saveCurrentConfig()
        }
    }
}
