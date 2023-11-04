/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature

import dev.lyzev.api.settings.KeySetting

/**
 * Represents a feature.
 *
 * @property name The name of the feature.
 * @property desc The description of the feature.
 * @param key The keybind of the feature.
 * @property category The category of the feature.
 */
abstract class Feature(
    val name: String, val desc: String, vararg val aliases: String, key: Int, val category: Category
) {

    // The keybind of the feature.
    val keybind by KeySetting(this::class, "Key", key)
}
