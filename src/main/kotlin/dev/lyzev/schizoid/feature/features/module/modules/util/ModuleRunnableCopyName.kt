/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.util

import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.ModuleRunnable
import dev.lyzev.schizoid.feature.features.module.modules.render.ModuleToggleableNotifications

object ModuleRunnableCopyName :
    ModuleRunnable("Copy Name", "Copies your username to the clipboard.", category = IFeature.Category.UTIL) {

    override fun invoke() {
        copy(mc.session.username)
        ModuleToggleableNotifications.info("The username has been copied to the clipboard.")
    }
}
