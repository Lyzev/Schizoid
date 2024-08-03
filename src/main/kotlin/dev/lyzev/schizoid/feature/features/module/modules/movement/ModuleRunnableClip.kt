/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.movement

import dev.lyzev.api.setting.settings.slider
import dev.lyzev.api.setting.settings.switch
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.ModuleRunnable
import dev.lyzev.schizoid.feature.features.module.modules.render.ModuleToggleableNotifications

object ModuleRunnableClip :
    ModuleRunnable("Clip", "Allows you to clip through blocks.", category = IFeature.Category.MOVEMENT) {

    val verticalLook by switch("Vertical Look", "Whether to move the player's look vector vertically.", false)
    val vertical by slider("Vertical", "The vertical speed.", 5f, -10f, 10f, 1, "blocks", true)
    val horizontal by slider("Horizontal", "The horizontal speed.", 0f, -10f, 10f, 1, "blocks", true)

    override fun invoke() {
        if (mc.player == null || !isIngame) {
            ModuleToggleableNotifications.error("You are not in a world.")
        }
        val player = mc.player!!
        val lookVec = player.rotationVecClient
        if (player.hasVehicle()) player.vehicle!!.setPosition(
            player.vehicle!!.x + lookVec.x * horizontal,
            player.vehicle!!.y + (if (verticalLook) vertical * lookVec.y else vertical.toDouble()),
            player.vehicle!!.z + lookVec.z * horizontal
        )
        player.setPosition(
            player.x + lookVec.x * horizontal,
            player.y + (if (verticalLook) vertical * lookVec.y else vertical.toDouble()),
            player.z + lookVec.z * horizontal
        )
        ModuleToggleableNotifications.info("You have clipped through blocks.")
    }
}
