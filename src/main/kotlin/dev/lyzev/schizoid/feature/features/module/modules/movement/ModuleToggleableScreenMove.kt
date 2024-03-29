/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.movement

import dev.lyzev.api.events.*
import dev.lyzev.api.glfw.GLFWKey
import dev.lyzev.api.setting.settings.slider
import dev.lyzev.api.setting.settings.switch
import dev.lyzev.api.settings.Setting.Companion.neq
import dev.lyzev.schizoid.feature.features.module.ModuleToggleable
import net.minecraft.client.gui.screen.ChatScreen
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.item.ItemGroups

object ModuleToggleableScreenMove : ModuleToggleable("Screen Move", "Allows you to move while in GUI screens.", category = Category.MOVEMENT), EventListener {

    val mouse by switch("Mouse", "Pass through the mouse.", false)
    val weight by slider("Weight", "The weight of the mouse movement.", 20, 1, 100, "%%", hide = ::mouse neq true)
    val keyboard by switch("Keyboard", "Pass through the keyboard.", true)

    override val shouldHandleEvents: Boolean
        get() = isEnabled && isIngame && mc.currentScreen != null && mc.currentScreen !is HandledScreen<*> && !mc.currentScreen!!.shouldPause()

    init {
        val movementKeys = listOf(
            mc.options.forwardKey,
            mc.options.backKey,
            mc.options.rightKey,
            mc.options.leftKey,
            mc.options.jumpKey,
            mc.options.sneakKey,
            mc.options.sprintKey
        ).map { switch(it.defaultKey.translationKey, "Pass through the keybinding.", true, ::keyboard neq true) }

        on<EventIsMovementKeyPressed> { event ->
            if (!keyboard || mc.currentScreen is ChatScreen || (mc.currentScreen is CreativeInventoryScreen && CreativeInventoryScreen.selectedTab == ItemGroups.SEARCH))
                return@on
            val key = event.keyBinding.defaultKey.translationKey
            movementKeys.find { it.name == key }?.let {
                if (it.value)
                    event.isPressed = GLFWKey.isPressed(event.keyBinding.boundKey.code)
            }
        }

        on<EventIsCursorLocked> { event ->
            if (!mouse)
                return@on
            event.isCursorLocked = true
        }

        on<EventUpdateMouse> {
            if (!mouse)
                return@on
            val mouse = mc.mouse
            val weight = weight / 100f
            mouse.cursorDeltaX *= weight
            mouse.cursorDeltaY *= weight
        }
    }
}
