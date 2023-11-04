/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module

import dev.lyzev.api.setting.settings.switch
import dev.lyzev.schizoid.feature.Category
import dev.lyzev.schizoid.feature.Feature

/**
 * Represents a module.
 *
 * @property name The name of the module.
 * @property desc The description of the module.
 * @param key The keybind of the module.
 * @property category The category of the module.
 */
abstract class Module(name: String, desc: String, vararg aliases: String, key: Int = -1, category: Category) :
    Feature(name, desc, aliases = aliases, key, category)

/**
 * Represents a module that can be run.
 *
 * @property name The name of the module.
 * @property desc The description of the module.
 * @property aliases The aliases of the module.
 * @param key The keybind of the module.
 * @property category The category of the module.
 */
abstract class ModuleRunnable(name: String, desc: String, vararg aliases: String, key: Int = -1, category: Category) :
    Module(name, desc, aliases = aliases, key, category), () -> Unit

/**
 * Represents a module that can be toggled.
 *
 * @property name The name of the module.
 * @property desc The description of the module.
 * @property aliases The aliases of the module.
 * @param key The keybind of the module.
 * @property category The category of the module.
 */
abstract class ModuleToggleable(name: String, desc: String, vararg aliases: String, key: Int = -1, category: Category) :
    Module(name, desc, aliases = aliases, key, category) {

    // Indicates whether the module is enabled.
    var isEnabled by switch("Enabled", value = false) {
        if (it) onEnable()
        else onDisable()
    }

    // Indicates whether the module should be shown in the array list.
    var showInArrayList by switch("Show In ArrayList", value = true)

    /**
     * Toggles the module.
     *
     * @see isEnabled
     */
    fun toggle() {
        isEnabled = !isEnabled
    }

    /**
     * Called when the module is enabled.
     */
    protected open fun onEnable() {}

    /**
     * Called when the module is disabled.
     */
    protected open fun onDisable() {}
}