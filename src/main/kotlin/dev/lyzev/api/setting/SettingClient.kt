/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.setting

import com.google.gson.JsonObject
import dev.lyzev.api.imgui.render.ImGuiRenderable
import dev.lyzev.api.settings.Setting
import dev.lyzev.schizoid.feature.IFeature
import imgui.ImGui
import kotlin.reflect.KClass

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
abstract class SettingClient<T>(
    container: KClass<out IFeature>, name: String, desc: String?, value: T, hidden: () -> Boolean, onChange: (T) -> Unit
) : Setting<T>(container, name, desc, value, hidden, onChange), ImGuiRenderable {

    val default = value

    open fun reset() {
        value = default
        onChange(value)
    }

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

    companion object {
        /**
         * The maximum height of the dropdown list.
         */
        private val MAX_HEIGHT
            get() = 5f * (16f + ImGui.getStyle().framePaddingY * 2f)

        /**
         * Calculate the height of the dropdown list.
         */
        fun calcHeight(size: Int): Float =
            (size.coerceAtLeast(1) * (16f + ImGui.getStyle().framePaddingY * 2f) + 1f).coerceAtMost(MAX_HEIGHT)
    }
}
