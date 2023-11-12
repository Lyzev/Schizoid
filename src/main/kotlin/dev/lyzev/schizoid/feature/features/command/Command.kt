/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.command

import dev.lyzev.schizoid.feature.Feature
import dev.lyzev.schizoid.feature.features.command.argument.Arguments
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
}
