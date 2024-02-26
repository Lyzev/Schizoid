/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module

import dev.lyzev.api.glfw.GLFWKey
import dev.lyzev.api.setting.SettingClient
import dev.lyzev.api.setting.settings.switch
import dev.lyzev.api.settings.SettingManager
import dev.lyzev.schizoid.feature.Feature
import imgui.ImGui.*

/**
 * Represents a module.
 *
 * @property name The name of the module.
 * @property desc The description of the module.
 * @param key The keybind of the module.
 * @property category The category of the module.
 */
abstract class Module(name: String, desc: String, vararg aliases: String, keys: MutableSet<GLFWKey> = mutableSetOf(), category: Category) :
    Feature(name, desc, aliases = aliases, keys, category)

/**
 * Represents a module that can be run.
 *
 * @property name The name of the module.
 * @property desc The description of the module.
 * @property aliases The aliases of the module.
 * @param key The keybind of the module.
 * @property category The category of the module.
 */
abstract class ModuleRunnable(name: String, desc: String, vararg aliases: String, keys: MutableSet<GLFWKey> = mutableSetOf(), category: Category) :
    Module(name, desc, aliases = aliases, keys, category), () -> Unit {

    override fun render() {
        val treeNode = treeNode(name)
        if (isItemHovered()) setTooltip(desc)
        if (treeNode) {
            if (button("Invoke")) invoke()

            @Suppress("UNCHECKED_CAST")
            for (setting in SettingManager[this::class] as List<SettingClient<*>>) {
                pushID("$name/${setting.name}")
                if (!setting.isHidden) setting.render()
                popID()
            }
            treePop()
        }
    }

    override fun keybindReleased() {
        if (mc.currentScreen == null) invoke()
    }
}

/**
 * Represents a module that can be toggled.
 *
 * @property name The name of the module.
 * @property desc The description of the module.
 * @property aliases The aliases of the module.
 * @param key The keybind of the module.
 * @property category The category of the module.
 */
abstract class ModuleToggleable(name: String, desc: String, vararg aliases: String, keys: MutableSet<GLFWKey> = mutableSetOf(), category: Category) :
    Module(name, desc, aliases = aliases, keys, category) {

    // Indicates whether the module is enabled.
    var isEnabled by switch("Enabled", "Whether the module is enabled.", value = false) {
        if (it) onEnable()
        else onDisable()
    }

    // Indicates whether the module should be shown in the array list.
    var showInArrayList by switch("Show In ArrayList", "Whether the module should be shown in the array list.", value = true)

    /**
     * Toggles the module.
     *
     * @see isEnabled
     */
    protected fun toggle() {
        isEnabled = !isEnabled
    }

    /**
     * Called when the module is enabled.
     */
    protected open fun onEnable() {}

    /**
     * Called when the module is disabled.
     */
    protected open fun onDisable() {}

    override fun keybindReleased() {
        if (mc.currentScreen == null) toggle()
    }
}
