/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.util

import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.ModuleRunnable
import dev.lyzev.schizoid.feature.features.module.modules.render.ModuleToggleableNotifications

object ModuleRunnableClearChat : ModuleRunnable("Clear Chat", "Clears the chat.", category = IFeature.Category.UTIL) {

    override fun invoke() {
        mc.inGameHud.chatHud.clear(false)
        ModuleToggleableNotifications.info("The chat has been cleared.")
    }
}
