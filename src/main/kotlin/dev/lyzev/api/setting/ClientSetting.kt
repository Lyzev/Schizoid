/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.setting

import com.google.gson.JsonObject
import dev.lyzev.api.settings.Setting
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
