/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.command

import dev.lyzev.schizoid.feature.Feature
import net.kyori.adventure.platform.fabric.FabricClientAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.event.HoverEventSource
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text

/**
 * Represents a command.
 *
 * @property name The name of the command.
 * @property desc The description of the command.
 * @property aliases The aliases of the command.
 * @param key The keybind of the command.
 * @property category The category of the command.
 */
abstract class Command(
    name: String, desc: String, val args: Arguments, vararg aliases: String, key: Int, category: Category
) : Feature(name, desc, aliases = aliases, key, category), () -> Unit {

    val usage = Component.text(aliases.first()).append(Component.text(" ")).append(args.usage)

    /**
     * The arguments of a command.
     *
     * @property args The arguments of the command.
     * @property usage The usage of the command.
     * @property size The size of the command.
     */
    class Arguments(vararg val args: Argument<*>) {

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

    /**
     * An argument for a command.
     *
     * @property name The name of the argument.
     * @property desc The description of the argument.
     * @property isRequired Whether the argument is required.
     * @property value The value of the argument.
     * @param T The type of the argument.
     */
    abstract class Argument<T>(val name: String, val desc: String? = null, val isRequired: Boolean = true) {

        var value: T? = null

        /**
         * Parses the input.
         *
         * @param input The input to parse.
         */
        abstract fun parse(input: String): Pair<Boolean, String>

        fun toComponent(): Component = Component.text(if (isRequired) "<$name>" else "[$name]").hoverEvent(HoverEventSource { HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text(desc ?: "")) })
    }

    /**
     * A string argument.
     *
     * @property name The name of the argument.
     * @property desc The description of the argument.
     * @property isRequired Whether the argument is required.
     * @property value The value of the argument.
     * @param T The type of the argument.
     */
    class StringArgument(name: String, desc: String? = null, isRequired: Boolean = true) :
        Argument<String>(name, desc, isRequired) {

        override fun parse(input: String): Pair<Boolean, String> {
            value = input
            return value!!.isNotBlank() to "Invalid string: $input"
        }
    }

    /**
     * A long argument.
     *
     * @property name The name of the argument.
     * @property desc The description of the argument.
     * @property isRequired Whether the argument is required.
     * @property value The value of the argument.
     * @param T The type of the argument.
     */
    class LongArgument(name: String, desc: String? = null, isRequired: Boolean = true) :
        Argument<Long>(name, desc, isRequired) {

        override fun parse(input: String): Pair<Boolean, String> {
            value = input.toLongOrNull()
            return (value != null) to "Invalid integer: $input"
        }
    }

    /**
     * A double argument.
     *
     * @property name The name of the argument.
     * @property desc The description of the argument.
     * @property isRequired Whether the argument is required.
     * @property value The value of the argument.
     * @param T The type of the argument.
     */
    class DoubleArgument(name: String, desc: String? = null, isRequired: Boolean = true) :
        Argument<Double>(name, desc, isRequired) {

        override fun parse(input: String): Pair<Boolean, String> {
            value = input.toDoubleOrNull()
            return (value != null) to "Invalid float: $input"
        }
    }

    /**
     * A boolean argument.
     *
     * @property name The name of the argument.
     * @property desc The description of the argument.
     * @property isRequired Whether the argument is required.
     * @property value The value of the argument.
     * @param T The type of the argument.
     */
    class BooleanArgument(name: String, desc: String? = null, isRequired: Boolean = true) :
        Argument<Boolean>(name, desc, isRequired) {

        override fun parse(input: String): Pair<Boolean, String> {
            value = input.toBooleanStrictOrNull()
            return (value != null) to "Invalid boolean: $input"
        }
    }

    /**
     * An enum argument.
     *
     * @property name The name of the argument.
     * @property desc The description of the argument.
     * @property isRequired Whether the argument is required.
     * @property value The value of the argument.
     * @param T The type of the argument.
     */
    class EnumArgument<T : Enum<T>>(
        name: String, desc: String? = null, isRequired: Boolean = true, val enum: Class<T>
    ) : Argument<T>(name, desc, isRequired) {

        override fun parse(input: String): Pair<Boolean, String> {
            value = enum.enumConstants.firstOrNull { it.name.equals(input, true) }
            return (value != null) to "Invalid enum: $input"
        }

        override fun toString(): String = super.toString() + enum.enumConstants.joinToString(" | ", " (", ")")
    }
}
