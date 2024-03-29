/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module

import dev.lyzev.api.settings.BooleanSetting
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
abstract class Module(name: String, desc: String, key: Int = -1, category: Category) : Feature(name, desc, key, category) {

    // Indicates whether the module is enabled.
    var isEnabled by BooleanSetting(this::class, "Enabled", false) {
        if (it) onEnable()
        else onDisable()
    }

    // Indicates whether the module should be shown in the array list.
    var showInArrayList by BooleanSetting(this::class, "Show In ArrayList", true)

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
