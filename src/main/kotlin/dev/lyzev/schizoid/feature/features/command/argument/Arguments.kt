/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.command.argument

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration

/**
 * The arguments of a command.
 *
 * @property args The arguments of the command.
 * @property usage The usage of the command.
 * @property size The size of the command.
 */
class Arguments(vararg val args: Argument<*>) {

    init {
        require(args.isNotEmpty()) { "Arguments cannot be empty" }
    }

    val usage = Component.join(JoinConfiguration.separator { Component.text(" ") }, args.map { it.toComponent() })
    val size = args.filter { it.isRequired }.size..args.size

    fun parse(vararg inputs: String): Pair<Boolean, String> {
        if (size.last <= 0) return true to ""
        if (inputs.size !in size) return false to "Invalid number of arguments: ${inputs.size} (expected: $size)"

        var required = 0
        var i = 0
        while (i < inputs.size && i + required < args.size) {
            val arg = args[i + required]
            val input = inputs[i]

            val (success, error) = arg.parse(input)
            if (!success) {
                if (arg.isRequired || inputs.size == args.size) return false to "${arg.name} | $error"
                else {
                    required++
                    continue
                }
            }
            i++
        }

        return !(args.any { it.isRequired && it.value == null }) to "Not all required arguments were provided"
    }

    fun reset() = args.forEach { it.value = null }
}
