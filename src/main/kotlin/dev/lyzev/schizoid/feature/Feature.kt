/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature

import dev.lyzev.api.glfw.GLFWKey
import dev.lyzev.api.imgui.render.ImGuiRenderable
import dev.lyzev.api.imgui.font.ImGuiFonts.*
import dev.lyzev.api.setting.SettingClient
import dev.lyzev.api.setting.settings.keybinds
import dev.lyzev.api.settings.SettingManager
import dev.lyzev.schizoid.Schizoid
import imgui.ImGui.*
import net.minecraft.text.Text

/**
 * Represents a feature.
 *
 * @property name The name of the feature.
 * @property desc The description of the feature.
 * @param key The keybind of the feature.
 * @property category The category of the feature.
 */
abstract class Feature(
    override val name: String,
    override val desc: String,
    keys: MutableSet<GLFWKey>,
    override val category: Category
) : IFeature {

    // The Minecraft client instance.
    val mc = Schizoid.mc

    // The keybind of the feature.
    override var keybinds by keybinds("Keybinds", "All keys used to control the feature.", keys)

    override fun sendChatMessage(message: Text) = FeatureManager.sendChatMessage(message)

    /**
     * Represents a category of features.
     */
    enum class Category : ImGuiRenderable {
        COMBAT,
        MOVEMENT,
        GHOST,
        PLAYER,
        RENDER,
        WORLD,
        UTIL,
        EXPLOIT,
        MISC;

        override fun render() {
            HELVETICA_NEUE_BOLD.begin()
            if (begin("\"$name\"")) {
                HELVETICA_NEUE.begin()
                FeatureManager[this].forEach(IFeature::render)
                HELVETICA_NEUE.end()
            }
            HELVETICA_NEUE_BOLD.end()
            end()
        }
    }
}

/**
 * Interface for a feature.
 * It extends [ImGuiRenderable] to allow rendering the feature using ImGui.
 */
interface IFeature : ImGuiRenderable {
    val name: String
    val desc: String
    var keybinds: MutableSet<GLFWKey>
    val category: Feature.Category

    /**
     * Renders the feature and its settings using ImGui.
     */
    override fun render() {
        val treeNode = treeNode(name)
        if (isItemHovered()) setTooltip(desc)
        if (treeNode) {
            @Suppress("UNCHECKED_CAST")
            for (setting in SettingManager[this::class] as List<SettingClient<*>>) {
                pushID("$name/${setting.name}")
                if (!setting.isHidden) setting.render()
                popID()
            }
            treePop()
        }
    }

    /**
     * Sends a chat message to the player.
     *
     * @param message The message to send.
     */
    fun sendChatMessage(message: Text)

    /**
     * Called when the keybind is pressed.
     */
    fun keybindReleased()
}
