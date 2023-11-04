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
 * A specific implementation of the [Setting] class for switch settings.
 *
 * @param container The class of the settings container where this setting belongs.
 * @param name The name of the setting.
 * @param value The initial value of the boolean setting.
 * @param hide A lambda function that determines whether this setting is hidden or not.
 * @param change A lambda function that will be called when the value of the setting changes.
 */
class SwitchSetting(
    container: KClass<*>,
    name: String,
    desc: String?,
    value: Boolean,
    hide: () -> Boolean,
    change: (Boolean) -> Unit = {}
) : ClientSetting<Boolean>(container, name, desc, value, hide, change) {

    override fun render() {
    }

    override fun load(value: JsonObject) {
        this.value = value["value"].asBoolean
    }

    override fun save(value: JsonObject) = value.addProperty("value", this.value)
}

fun Feature.switch(
    name: String,
    desc: String? = null,
    value: Boolean,
    hide: () -> Boolean = { false },
    change: (Boolean) -> Unit = {}
) = SwitchSetting(this::class, name, desc, value, hide, change)
