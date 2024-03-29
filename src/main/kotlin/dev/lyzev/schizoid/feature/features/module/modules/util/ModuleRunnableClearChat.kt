/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.util

import dev.lyzev.schizoid.feature.features.module.ModuleRunnable

object ModuleRunnableClearChat : ModuleRunnable("Clear Chat", "Clears the chat.", category = Category.UTIL) {

    override fun invoke(): String? {
        mc.inGameHud.chatHud.clear(false)
        return null
    }
}
