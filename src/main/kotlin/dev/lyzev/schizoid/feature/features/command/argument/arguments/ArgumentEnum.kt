/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.command.argument.arguments

import dev.lyzev.schizoid.feature.features.command.argument.Argument
import dev.lyzev.schizoid.feature.features.command.argument.ArgumentAutoComplete
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.event.HoverEventSource

/**
 * An enum argument.
 *
 * @property name The name of the argument.
 * @property desc The description of the argument.
 * @property isRequired Whether the argument is required.
 * @property value The value of the argument.
 * @param T The type of the argument.
 */
open class ArgumentEnum<T : Enum<T>>(
    name: String, desc: String? = null, isRequired: Boolean = true, val enum: Class<T>
) : Argument<T>(name, desc, isRequired) {

    override fun parse(input: String): Pair<Boolean, String> {
        value = enum.enumConstants.firstOrNull { it.name.equals(input, true) }
        return (value != null) to "Invalid enum: $input"
    }

    override fun toComponent() = Component.text(if (isRequired) "<$name>" else "[$name]").hoverEvent(HoverEventSource {
        HoverEvent.showText(
            Component.text("$desc\n").append(
                Component.text(enum.enumConstants.joinToString("\n") { it.name })
            )
        )
    })
}

/**
 * An enum argument with auto-completion.
 *
 * @property name The name of the argument.
 * @property desc The description of the argument.
 * @property isRequired Whether the argument is required.
 * @property value The value of the argument.
 * @param T The type of the argument.
 */
class ArgumentAutoCompleteEnum<T : Enum<T>>(
    name: String,
    desc: String? = null,
    isRequired: Boolean = true,
    enum: Class<T>
) :
    ArgumentEnum<T>(name, desc, isRequired, enum), ArgumentAutoComplete {

    // The list of possible completions.
    private val completion = enum.enumConstants.map { it.name }

    override fun autoComplete(input: String): List<String> = completion.filter { it.startsWith(input, true) }
}
