/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.setting.settings

import com.google.gson.JsonObject
import dev.lyzev.api.setting.SettingClient
import dev.lyzev.schizoid.feature.IFeature
import imgui.ImGui.*
import imgui.type.ImInt
import kotlin.reflect.KClass

class SettingClientOptionString(
    container: KClass<out IFeature>,
    name: String,
    desc: String?,
    value: String,
    private val options: Array<String>,
    hide: () -> Boolean,
    change: (String) -> Unit
) : SettingClient<String>(container, name, desc, value, hide, change) {

    override fun render() {
        text(name)
        if (desc != null && isItemHovered()) setTooltip(desc)
        i.set(options.indexOf(value))
        if (combo(name, i, options, options.size)) value = options[i.get()]
    }

    override fun load(value: JsonObject) {
        this.value = value["selected"].asString
    }

    override fun save(value: JsonObject) = value.addProperty("selected", this.value)

    companion object {
        private val i = ImInt()
    }
}

class SettingClientOptionEnum<T : OptionEnum>(
    container: KClass<out IFeature>,
    name: String,
    desc: String?,
    value: T,
    private val options: List<T>,
    hide: () -> Boolean,
    change: (T) -> Unit
) : SettingClient<T>(container, name, desc, value, hide, change) {

    private val optionsKeys = options.map { it.key }.toTypedArray()

    override fun render() {
        text(name)
        if (desc != null && isItemHovered()) setTooltip(desc)
        i.set(options.indexOf(value))
        if (combo("", i, optionsKeys, options.size)) value = options[i.get()]
    }

    override fun load(value: JsonObject) {
        this.value = options.first { it.key == value["selected"].asString }
    }

    override fun save(value: JsonObject) = value.addProperty("selected", this.value.key)

    companion object {
        private val i = ImInt()
    }
}

interface OptionEnum {
    val key: String
}

fun IFeature.option(
    name: String,
    desc: String? = null,
    value: String,
    options: Array<String>,
    hide: () -> Boolean = { false },
    change: (String) -> Unit = {}
) = SettingClientOptionString(this::class, name, desc, value, options, hide, change)

fun <T : OptionEnum> IFeature.option(
    name: String,
    desc: String? = null,
    value: T,
    options: List<T>,
    hide: () -> Boolean = { false },
    change: (T) -> Unit = {}
) = SettingClientOptionEnum(this::class, name, desc, value, options, hide, change)
