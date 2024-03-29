/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.imgui.theme

import dev.lyzev.api.imgui.theme.themes.*
import dev.lyzev.api.setting.settings.OptionEnum
import net.minecraft.client.gui.DrawContext

/**
 * Enum class representing different ImGui themes.
 * Each enum value represents a different theme and contains a key and an ImGuiTheme instance.
 * The ImGuiTheme instance is used to apply the style and colors of the theme.
 * It also provides a method to render the in-game background.
 * @param key The key of the theme.
 * @param theme The ImGuiTheme instance of the theme.
 */
enum class ImGuiThemes(override val key: String, val theme: ImGuiTheme) : ImGuiTheme, OptionEnum {
    LIGHT_ORANGE("Light Orange", ImGuiThemeLightOrange),
    DARK_ORANGE("Dark Orange", ImGuiThemeDarkOrange),
    LIGHT_RED("Light Red", ImGuiThemeLightRed),
    DARK_RED("Dark Red", ImGuiThemeDarkRed),
    LIGHT_MINT("Light Mint", ImGuiThemeLightMint),
    DARK_MINT("Dark Mint", ImGuiThemeDarkMint),
    LIGHT_GREEN("Light Green", ImGuiThemeLightGreen),
    DARK_GREEN("Dark Green", ImGuiThemeDarkGreen),
    LIGHT_PURPLE("Light Purple", ImGuiThemeLightPurple),
    DARK_PURPLE("Dark Purple", ImGuiThemeDarkPurple);

    override fun applyStyle() = theme.applyStyle()
    override fun applyColors() = theme.applyColors()

    override fun renderInGameBackground(context: DrawContext, width: Int, height: Int) = theme.renderInGameBackground(context, width, height)
}
