/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.player

import dev.lyzev.api.events.EventItemUse
import dev.lyzev.api.events.EventListener
import dev.lyzev.api.events.on
import dev.lyzev.api.setting.settings.slider
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.ModuleToggleable

object ModuleToggleableFastPlace :
    ModuleToggleable("Fast Place", "Modifies the player's block placement speed.", category = IFeature.Category.PLAYER),
    EventListener {

    val delay by slider("Delay", "The delay between block placements.", 0, 0, 4, "ticks", true)

    override val shouldHandleEvents by ::isEnabled

    init {
        on<EventItemUse> { it.itemUseCooldown = delay }
    }
}
