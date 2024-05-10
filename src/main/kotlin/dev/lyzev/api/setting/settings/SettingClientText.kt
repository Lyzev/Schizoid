/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.setting.settings

import com.google.gson.JsonObject
import dev.lyzev.api.setting.SettingClient
import dev.lyzev.schizoid.feature.IFeature
import imgui.ImGui.*
import imgui.type.ImString
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
    hide: () -> Boolean,
    change: (String) -> Unit
) : SettingClient<String>(container, name, desc, value, hide, change) {


    override fun render() {
        text(name)
        if (desc != null && isItemHovered()) setTooltip(desc)
        v.set(value)
        if (inputText("", v))
            value = v.get()
    }

    override fun load(value: JsonObject) {
        this.value = value["text"].asString
    }

    override fun save(value: JsonObject) = value.addProperty("text", this.value)

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
    hide: () -> Boolean = { false },
    change: (String) -> Unit = {}
) = SettingClientText(this::class, name, desc, value, hide, change)
