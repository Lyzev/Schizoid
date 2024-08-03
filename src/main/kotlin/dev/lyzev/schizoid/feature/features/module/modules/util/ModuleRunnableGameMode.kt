/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.util

import dev.lyzev.api.setting.settings.OptionEnum
import dev.lyzev.api.setting.settings.option
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.ModuleRunnable
import dev.lyzev.schizoid.feature.features.module.modules.render.ModuleToggleableNotifications

object ModuleRunnableGameMode :
    ModuleRunnable("Game Mode", "Allows you to change your game mode.", category = IFeature.Category.UTIL) {

    val mode by option("Mode", "The game mode to set.", GameMode.CREATIVE, GameMode.entries)

    override fun invoke() {
        if (!isIngame) {
            ModuleToggleableNotifications.error("You are not in a world.")
            return
        }
        mc.interactionManager?.setGameMode(mode.type)
        ModuleToggleableNotifications.info("Your game mode has been set to ${mode.key}.")
    }

    enum class GameMode(val type: net.minecraft.world.GameMode) : OptionEnum {
        SURVIVAL(net.minecraft.world.GameMode.SURVIVAL),
        CREATIVE(net.minecraft.world.GameMode.CREATIVE),
        ADVENTURE(net.minecraft.world.GameMode.ADVENTURE),
        SPECTATOR(net.minecraft.world.GameMode.SPECTATOR);

        override val key: String
            get() = type.getName()
    }
}
