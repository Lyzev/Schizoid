/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.setting.settings

import dev.lyzev.api.setting.SettingClient
import dev.lyzev.schizoid.feature.IFeature
import imgui.ImGui.*
import imgui.flag.ImGuiComboFlags
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import kotlin.reflect.KClass

/**
 * A specific implementation of the [SettingClient] class for string option settings.
 *
 * @param container The class of the settings container where this setting belongs.
 * @param name The name of the setting.
 * @param desc The description of the setting.
 * @param value The initial value of the option setting.
 * @param options The array of options for the setting.
 * @param hide A lambda function that determines whether this setting is hidden or not.
 * @param change A lambda function that will be called when the value of the setting changes.
 */
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
        setNextItemWidth(getColumnWidth())
        if (beginCombo(name, value, ImGuiComboFlags.HeightRegular)) {
            for (i in options.indices) {
                val isSelected = options[i] == value
                if (selectable(options[i], isSelected))
                    value = options[i]
                if (isSelected) setItemDefaultFocus()
            }
            endCombo()
        }
    }

    override fun load(value: JsonElement) {
        this.value = value.jsonPrimitive.content
    }

    override fun save(): JsonElement = JsonPrimitive(value)
}

/**
 * A specific implementation of the [SettingClient] class for enum option settings.
 *
 * @param container The class of the settings container where this setting belongs.
 * @param name The name of the setting.
 * @param desc The description of the setting.
 * @param value The initial value of the option setting.
 * @param options The list of options for the setting.
 * @param hide A lambda function that determines whether this setting is hidden or not.
 * @param change A lambda function that will be called when the value of the setting changes.
 */
class SettingClientOptionEnum<T : OptionEnum>(
    container: KClass<out IFeature>,
    name: String,
    desc: String?,
    value: T,
    private val options: List<T>,
    hide: () -> Boolean,
    change: (T) -> Unit
) : SettingClient<T>(container, name, desc, value, hide, change) {

    override fun render() {
        text(name)
        if (desc != null && isItemHovered()) setTooltip(desc)
        setNextItemWidth(getColumnWidth())
        if (beginCombo(name, value.key, ImGuiComboFlags.HeightRegular)) {
            for (i in options.indices) {
                val isSelected = options[i] == value
                if (selectable(options[i].key, isSelected))
                    value = options[i]
                if (isSelected) setItemDefaultFocus()
            }
            endCombo()
        }
    }

    override fun load(value: JsonElement) {
        this.value = options.firstOrNull { it.key == value.jsonPrimitive.content } ?: return
    }

    override fun save(): JsonElement = JsonPrimitive(value.key)
}

/**
 * Interface for option enums.
 */
interface OptionEnum {
    val key: String
}

/**
 * Creates a new string option setting.
 *
 * @param name The name of the setting.
 * @param desc The description of the setting.
 * @param value The initial value of the option setting.
 * @param options The array of options for the setting.
 * @param hide A lambda function that determines whether this setting is hidden or not.
 * @param change A lambda function that will be called when the value of the setting changes.
 *
 * @return The created string option setting.
 */
fun IFeature.option(
    name: String,
    desc: String? = null,
    value: String,
    options: Array<String>,
    hide: () -> Boolean = { false },
    change: (String) -> Unit = {}
) = SettingClientOptionString(this::class, name, desc, value, options, hide, change)

/**
 * Creates a new enum option setting.
 *
 * @param name The name of the setting.
 * @param desc The description of the setting.
 * @param value The initial value of the option setting.
 * @param options The list of options for the setting.
 * @param hide A lambda function that determines whether this setting is hidden or not.
 * @param change A lambda function that will be called when the value of the setting changes.
 *
 * @return The created enum option setting.
 */
fun <T : OptionEnum> IFeature.option(
    name: String,
    desc: String? = null,
    value: T,
    options: List<T>,
    hide: () -> Boolean = { false },
    change: (T) -> Unit = {}
) = SettingClientOptionEnum(this::class, name, desc, value, options, hide, change)
