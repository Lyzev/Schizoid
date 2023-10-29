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
 * @property key The keybind of the feature.
 * @property category The category of the feature.
 */
class Feature(val name: String, val desc: String, key: Int = -1, val category: Category) {

    val keybind by KeySetting(this::class, "Key", key)
}
