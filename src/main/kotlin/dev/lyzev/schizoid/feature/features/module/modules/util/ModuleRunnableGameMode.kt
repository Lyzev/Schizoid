/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.util

import dev.lyzev.api.setting.settings.OptionEnum
import dev.lyzev.api.setting.settings.option
import dev.lyzev.schizoid.feature.features.module.ModuleRunnable

object ModuleRunnableGameMode : ModuleRunnable("Game Mode", "Allows you to change your game mode.", category = Category.UTIL) {

    val mode by option("Mode", "The game mode to set.", GameMode.CREATIVE, GameMode.entries)

    override fun invoke(): String? {
        if (!isIngame) return "You are not in a game."
        mc.interactionManager?.setGameMode(mode.type)
        return null
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
