/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.command

/**
 * Represents a command.
 *
 * @property name The name of the command.
 * @property desc The description of the command.
 * @property alias The aliases of the command.
 * @property async Indicates whether the command should be executed asynchronously.
 */
abstract class Command(val name: String, val desc: String, vararg val alias: String, val async: Boolean = true) {

    /**
     * Executes the command.
     *
     * @param args The arguments of the command.
     */
    abstract operator fun invoke(args: List<String>)
}
