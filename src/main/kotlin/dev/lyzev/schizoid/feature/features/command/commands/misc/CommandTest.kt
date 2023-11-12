/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.command.commands.misc

import dev.lyzev.schizoid.feature.features.command.Command
import dev.lyzev.schizoid.feature.features.command.argument.Arguments
import dev.lyzev.schizoid.feature.features.command.argument.arguments.ArgumentAutoCompleteBoolean
import dev.lyzev.schizoid.feature.features.command.argument.arguments.ArgumentDouble
import dev.lyzev.schizoid.feature.features.command.argument.arguments.ArgumentString
import net.kyori.adventure.text.Component

/**
 * A test command.
 */
object CommandTest : Command(
    "Test",
    "This is a test command.",
    Arguments(
        ArgumentString("Test1", "This is a test argument"),
        ArgumentDouble("Test2", isRequired = false),
        ArgumentAutoCompleteBoolean("Test3")
    ),
    "test",
    key = -1,
    category = Category.MISC
) {

    override fun invoke() {
        sendChatMessage(Component.text("This is a test command."))
        args.args.forEach { sendChatMessage(Component.text(if (it.value != null) it.value!!.toString() else it.name + " wasn't provided")) }
    }
}
