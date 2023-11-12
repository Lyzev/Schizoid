/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.command.commands.misc

import dev.lyzev.schizoid.feature.features.command.Command
import net.kyori.adventure.text.Component

/**
 * A test command.
 */
object TestCommand : Command(
    "Test",
    "This is a test command.",
    Arguments(
        StringArgument("Test1", "This is a test argument"),
        DoubleArgument("Test2", isRequired = false),
        BooleanArgument("Test3")
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
