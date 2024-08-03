/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.movement

import dev.lyzev.api.events.EventClientPlayerEntityTickPre
import dev.lyzev.api.events.EventListener
import dev.lyzev.api.events.on
import dev.lyzev.api.setting.settings.switch
import dev.lyzev.api.settings.Setting.Companion.neq
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.ModuleToggleable
import net.minecraft.client.gui.screen.ingame.HandledScreen

object ModuleToggleableToggleWalk :
    ModuleToggleable(
        "Toggle Walk",
        "Automatically toggles sneak when riding a boat.",
        category = IFeature.Category.MOVEMENT
    ), EventListener {

    val screen by switch("Screen", "Whether to walk in screen.", true)
    val handledScreen by switch(
        "Handled Screen",
        "Whether to walk in a handled screen.",
        false,
        hide = ::screen neq true
    )
    val autoJump by switch("Auto Jump", "Whether to auto jump.", true)

    private var wasAutoJump = false

    override fun onEnable() {
        super.onEnable()
        wasAutoJump = mc.options.autoJump.value
    }

    override fun onDisable() {
        super.onDisable()
        mc.options.autoJump.value = wasAutoJump
    }

    init {
        on<EventClientPlayerEntityTickPre> {
            mc.options.forwardKey.isPressed = true
            mc.options.autoJump.value = autoJump
        }
    }

    override val shouldHandleEvents
        get() = isEnabled && (mc.currentScreen == null || ((screen && mc.currentScreen !is HandledScreen<*>) || handledScreen))
}
