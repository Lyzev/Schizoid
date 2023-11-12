/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.command.argument

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.event.HoverEventSource

/**
 * An argument for a command.
 *
 * @property name The name of the argument.
 * @property desc The description of the argument.
 * @property isRequired Whether the argument is required.
 * @property value The value of the argument.
 * @param T The type of the argument.
 */
abstract class Argument<T>(val name: String, val desc: String? = null, val isRequired: Boolean) {

    var value: T? = null

    /**
     * Parses the input.
     *
     * @param input The input to parse.
     */
    abstract fun parse(input: String): Pair<Boolean, String>

    open fun toComponent() = Component.text(if (isRequired) "<$name>" else "[$name]")
        .hoverEvent(HoverEventSource { HoverEvent.showText(Component.text(desc ?: "")) })
}

interface ArgumentAutoComplete {

    /**
     * Returns a list of auto complete suggestions for the input.
     *
     * @param input The input to get suggestions for.
     * @return A list of suggestions.
     */
    fun autoComplete(input: String): List<String>
}
