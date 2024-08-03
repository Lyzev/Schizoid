/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.util

import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.ModuleRunnable
import dev.lyzev.schizoid.feature.features.module.modules.render.ModuleToggleableNotifications

object ModuleRunnableCopyIp :
    ModuleRunnable("Copy IP", "Copies the server IP to the clipboard.", category = IFeature.Category.UTIL) {

    override fun invoke() {
        if (!isMultiplayer) {
            ModuleToggleableNotifications.error("You are not connected to a server.")
            return
        }
        copy(mc.currentServerEntry!!.address)
        ModuleToggleableNotifications.info("The server IP has been copied to the clipboard.")
    }
}
