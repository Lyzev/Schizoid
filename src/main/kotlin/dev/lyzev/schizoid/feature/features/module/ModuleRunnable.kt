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
abstract class ModuleRunnable(name: String, desc: String, vararg aliases: String, key: Int = -1, category: Category) :
    Module(name, desc, aliases = aliases, key, category) {

    /**
     * Executes the module.
     */
    abstract fun run()
}
