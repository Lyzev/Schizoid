/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.command.commands.misc

import dev.lyzev.schizoid.feature.features.command.Command

/**
 * A test command.
 */
object TestCommand : Command("Test", "This is a test command.", "test", key = -1, category = Category.MISC) {

    override fun invoke(args: List<String>) {
        sendChatMessage("This is a test command.")
    }
}
