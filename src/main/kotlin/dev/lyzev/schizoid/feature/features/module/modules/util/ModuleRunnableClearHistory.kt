/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.util

import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.ModuleRunnable
import dev.lyzev.schizoid.feature.features.module.modules.render.ModuleToggleableNotifications

object ModuleRunnableClearHistory :
    ModuleRunnable("Clear History", "Clears the chat history.", category = IFeature.Category.UTIL) {

    override fun invoke() {
        mc.inGameHud.chatHud.messageHistory.clear()
        ModuleToggleableNotifications.info("The message history has been cleared.")
    }
}
