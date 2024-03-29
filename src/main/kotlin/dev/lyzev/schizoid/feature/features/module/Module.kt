/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module

import dev.lyzev.api.events.EventListener
import dev.lyzev.api.events.EventRenderImGuiContent
import dev.lyzev.api.events.on
import dev.lyzev.api.glfw.GLFWKey
import dev.lyzev.api.imgui.font.ImGuiFonts.*
import dev.lyzev.api.setting.SettingClient
import dev.lyzev.api.setting.settings.switch
import dev.lyzev.api.settings.SettingManager
import dev.lyzev.schizoid.feature.Feature
import dev.lyzev.schizoid.feature.features.gui.guis.ImGuiScreenFeature
import imgui.ImGui.*
import imgui.flag.ImGuiWindowFlags
import imgui.type.ImBoolean

/**
 * Represents a module.
 *
 * @property name The name of the module.
 * @property desc The description of the module.
 * @param key The keybind of the module.
 * @property category The category of the module.
 */
abstract class Module(name: String, desc: String, keys: MutableSet<GLFWKey> = mutableSetOf(), category: Category) :
    Feature(name, desc, keys, category)

/**
 * Represents a module that can be run.
 *
 * @property name The name of the module.
 * @property desc The description of the module.
 * @param key The keybind of the module.
 * @property category The category of the module.
 */
abstract class ModuleRunnable(
    name: String,
    desc: String,
    keys: MutableSet<GLFWKey> = mutableSetOf(),
    category: Category
) :
    Module(name, desc, keys, category), () -> String? {

    var response: String? = null

    override fun render() {
        if (ImGuiScreenFeature.searchResult == this) {
            setWindowFocus()
            setScrollHereY()
            setNextItemOpen(true)
            ImGuiScreenFeature.searchResult = null
        }
        val treeNode = treeNode(name)
        if (isItemHovered()) setTooltip(desc)
        if (treeNode) {
            if (button("Invoke")) response = invoke()
            if (response != null) {
                sameLine()
                textColored(255, 69, 58, 255, response!!)
            }
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
        if (mc.currentScreen == null) response = invoke()
    }
}

/**
 * Represents a module that can be toggled.
 *
 * @property name The name of the module.
 * @property desc The description of the module.
 * @param key The keybind of the module.
 * @property category The category of the module.
 */
abstract class ModuleToggleable(
    name: String,
    desc: String,
    keys: MutableSet<GLFWKey> = mutableSetOf(),
    category: Category
) :
    Module(name, desc, keys, category) {

    // Indicates whether the module is enabled.
    var isEnabled by switch("Enabled", "Whether the module is enabled.", value = false) {
        if (it) onEnable()
        else onDisable()
    }

    // Indicates whether the module should be shown in the array list.
    var showInArrayList by switch(
        "Show In ArrayList",
        "Whether the module should be shown in the array list.",
        value = true
    )

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

/**
 * Represents a module that can be toggled and renders ImGui content.
 * The module will only render ImGui content when it is enabled and the player is in-game.
 * @property name The name of the module.
 * @property desc The description of the module.
 * @param key The keybind of the module.
 * @property category The category of the module.
 */
abstract class ModuleToggleableRenderImGuiContent(
    name: String,
    desc: String,
    keys: MutableSet<GLFWKey> = mutableSetOf(),
    category: Category
) : ModuleToggleable(name, desc, keys, category), EventListener {

    /**
     * Renders the ImGui content of the module.
     */
    abstract fun renderImGuiContent()

    override val shouldHandleEvents: Boolean
        get() = isEnabled && isIngame

    init {
        /**
         * This block of code is executed when the module is initialized.
         * It sets up an event listener that renders the ImGui content of the module every time the ImGui content is rendered.
         */
        on<EventRenderImGuiContent> {
            OPEN_SANS_BOLD.begin()
            if (mc.currentScreen == null) {
                if (begin("\"${name.uppercase()}\"", FLAGS or ImGuiWindowFlags.NoMove)) {
                    OPEN_SANS_REGULAR.begin()
                    renderImGuiContent()
                    OPEN_SANS_REGULAR.end()
                }
            } else {
                val open = ImBoolean(true)
                if (begin("\"${name.uppercase()}\"", open, FLAGS)) {
                    OPEN_SANS_REGULAR.begin()
                    renderImGuiContent()
                    OPEN_SANS_REGULAR.end()
                }
                if (!open.get())
                    toggle()
            }
            end()
            OPEN_SANS_BOLD.end()
        }
    }

    companion object {
        private const val FLAGS = ImGuiWindowFlags.AlwaysAutoResize or ImGuiWindowFlags.NoCollapse
    }
}
