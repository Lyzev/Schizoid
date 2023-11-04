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
 * @property alias The aliases of the command.
 * @property async Indicates whether the command should be executed asynchronously.
 */
abstract class Command(name: String, desc: String, vararg aliases: String, key: Int, category: Category) :
    Feature(name, desc, aliases = aliases, key, category) {

    /**
     * Executes the command.
     *
     * @param args The arguments of the command.
     */
    abstract operator fun invoke(args: List<String>)
}
