/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.command

import dev.lyzev.schizoid.feature.Category
import dev.lyzev.schizoid.feature.Feature

/**
 * Represents a command.
 *
 * @property name The name of the command.
 * @property desc The description of the command.
 * @property aliases The aliases of the command.
 * @param key The keybind of the command.
 * @property category The category of the command.
 */
abstract class Command(name: String, desc: String, vararg aliases: String, key: Int, category: Category) :
    Feature(name, desc, aliases = aliases, key, category), (List<String>) -> Unit
