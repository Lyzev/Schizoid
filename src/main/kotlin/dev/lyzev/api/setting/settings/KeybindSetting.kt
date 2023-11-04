/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.setting.settings

import com.google.gson.JsonObject
import dev.lyzev.api.setting.ClientSetting
import dev.lyzev.schizoid.feature.Feature
import kotlin.reflect.KClass

/**
 * A specific implementation of the [Setting] class for keybind settings.
 *
 * @param container The class of the settings container where this setting belongs.
 * @param name The name of the setting.
 * @param value The initial value of the integer setting.
 * @param hide A lambda function that determines whether this setting is hidden or not.
 * @param change A lambda function that will be called when the value of the setting changes.
 */
class KeybindSetting(
    container: KClass<*>, name: String, desc: String?, value: Int, hide: () -> Boolean, change: (Int) -> Unit = {}
) : ClientSetting<Int>(container, name, desc, value, hide, change) {

    override fun render() {
    }

    override fun load(value: JsonObject) {
        this.value = value["value"].asInt
    }

    override fun save(value: JsonObject) = value.addProperty("value", this.value)
}

fun Feature.keybind(
    name: String,
    desc: String? = null,
    value: Int,
    hide: () -> Boolean = { false },
    change: (Int) -> Unit = {}
) = KeybindSetting(this::class, name, desc, value, hide, change)
