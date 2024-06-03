/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.setting.settings

import dev.lyzev.api.setting.SettingClient
import dev.lyzev.schizoid.feature.IFeature
import imgui.ImGui.*
import imgui.flag.ImGuiInputTextFlags
import imgui.type.ImString
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import kotlin.reflect.KClass

/**
 * A class representing a text setting in the client.
 * This class extends the SettingClient class with a String type.
 * It provides methods to render the setting, load and save its value.
 *
 * @property container The class of the feature that this setting belongs to.
 * @property name The name of the setting.
 * @property desc The description of the setting.
 * @property value The value of the setting.
 * @property hide A function that determines whether the setting should be hidden.
 * @property change A function that is called when the setting's value changes.
 */
class SettingClientText(
    container: KClass<out IFeature>,
    name: String,
    desc: String?,
    value: String,
    private val enterReturnsTrue: Boolean,
    private val regex: Regex?,
    hide: () -> Boolean,
    change: (String) -> Unit
) : SettingClient<String>(container, name, desc, value, hide, change) {


    override fun render() {
        text(name)
        if (desc != null && isItemHovered()) setTooltip(desc)
        v.set(value)
        if (inputText("", v, if (enterReturnsTrue) ImGuiInputTextFlags.EnterReturnsTrue else ImGuiInputTextFlags.None)) {
            if (regex != null && !regex.matches(v.get()))
                return
            value = v.get()
        }
        if (enterReturnsTrue && isItemHovered()) setTooltip("Press Enter to apply." + if (regex != null) "\nMust match regex: $regex" else "")
    }

    override fun load(value: JsonElement) {
        this.value = value.jsonPrimitive.content
    }

    override fun save(): JsonElement = JsonPrimitive(this.value)

    companion object {
        /**
         * The ImString instance used to store the value of the text setting.
         */
        private val v = ImString("", 1024)
    }
}

/**
 * Extension function for the IFeature interface.
 * Creates a new SettingClientText instance with the provided parameters.
 *
 * @param name The name of the setting.
 * @param desc The description of the setting.
 * @param value The value of the setting.
 * @param hide A function that determines whether the setting should be hidden.
 * @param change A function that is called when the setting's value changes.
 *
 * @return The created SettingClientText instance.
 */
fun IFeature.text(
    name: String,
    desc: String? = null,
    value: String,
    enterReturnsTrue: Boolean = false,
    regex: Regex? = null,
    hide: () -> Boolean = { false },
    change: (String) -> Unit = {}
) = SettingClientText(this::class, name, desc, value, enterReturnsTrue, regex, hide, change)
