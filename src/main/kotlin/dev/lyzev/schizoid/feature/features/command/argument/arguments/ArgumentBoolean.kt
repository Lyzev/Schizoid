/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.command.argument.arguments

import dev.lyzev.schizoid.feature.features.command.argument.Argument
import dev.lyzev.schizoid.feature.features.command.argument.ArgumentAutoComplete

/**
 * A boolean argument.
 *
 * @property name The name of the argument.
 * @property desc The description of the argument.
 * @property isRequired Whether the argument is required.
 * @property value The value of the argument.
 * @param T The type of the argument.
 */
open class ArgumentBoolean(name: String, desc: String? = null, isRequired: Boolean = true) :
    Argument<Boolean>(name, desc, isRequired) {

    override fun parse(input: String): Pair<Boolean, String> {
        value = input.toBooleanStrictOrNull()
        return (value != null) to "Invalid boolean: $input"
    }
}

/**
 * A boolean argument with auto-completion.
 *
 * @property name The name of the argument.
 * @property desc The description of the argument.
 * @property isRequired Whether the argument is required.
 * @property value The value of the argument.
 * @param T The type of the argument.
 */
class ArgumentAutoCompleteBoolean(name: String, desc: String? = null, isRequired: Boolean = true) :
    ArgumentBoolean(name, desc, isRequired), ArgumentAutoComplete {

    // The list of possible completions.
    private val completion = listOf("true", "false")

    override fun autoComplete(input: String): List<String> = completion.filter { it.startsWith(input, true) }
}
