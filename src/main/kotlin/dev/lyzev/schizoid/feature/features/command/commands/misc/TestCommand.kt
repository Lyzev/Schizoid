/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.command.commands.misc

import dev.lyzev.schizoid.feature.features.command.Command

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
        sendChatMessage("This is a test command.")
        args.args.forEach { sendChatMessage(if (it.value != null) it.value!!.toString() else it.name + " wasn't provided") }
    }
}
