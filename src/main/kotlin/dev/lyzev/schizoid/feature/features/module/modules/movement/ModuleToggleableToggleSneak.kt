/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.movement

import dev.lyzev.api.events.EventClientPlayerEntityTick
import dev.lyzev.api.events.EventListener
import dev.lyzev.api.events.on
import dev.lyzev.api.setting.settings.switch
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.ModuleToggleable

object ModuleToggleableToggleSneak :
    ModuleToggleable("Toggle Sneak", "Automatically toggles sneak when riding a boat.", category = IFeature.Category.MOVEMENT), EventListener {

    val screen by switch("Screen", "Whether to sneak in screen.", true)

    init {
        on<EventClientPlayerEntityTick> {
            if (mc.currentScreen == null || screen)
                mc.options.sneakKey.isPressed = true
        }
    }

    override val shouldHandleEvents: Boolean
        get() = isEnabled
}
