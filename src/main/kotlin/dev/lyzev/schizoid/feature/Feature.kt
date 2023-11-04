/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature

import dev.lyzev.api.setting.settings.keybind
import dev.lyzev.schizoid.Schizoid

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

    // The Minecraft client instance.
    val mc = Schizoid.mc

    // The keybind of the feature.
    val keybind by keybind("Key", value = key)

    /**
     * Sends a chat message to the player.
     *
     * @param message The message to send.
     */
    fun sendChatMessage(message: String) = FeatureManager.sendChatMessage(message)

    /**
     * Represents a category of features.
     */
    enum class Category {
        COMBAT, GHOST, MOVEMENT, PLAYER, RENDER, EXPLOIT, UTIL, MISC
    }
}
