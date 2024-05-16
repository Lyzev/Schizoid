/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.imgui.theme.themes

import dev.lyzev.api.imgui.theme.ImGuiThemeBaseDark
import dev.lyzev.api.imgui.theme.ImGuiThemeBaseGlassmorphism
import dev.lyzev.api.imgui.theme.ImGuiThemeBaseLight
import java.awt.Color

/**
 * Light teal ImGui theme.
 * Using colors from https://developer.apple.com/design/human-interface-guidelines/color#macOS-system-colors
 */
object ImGuiThemeLightTeal : ImGuiThemeBaseLight(Color(48, 176, 199), Color(108, 185, 199), Color(78, 181, 199))

/**
 * Dark teal ImGui theme.
 * Using colors from https://developer.apple.com/design/human-interface-guidelines/color#macOS-system-colors
 */
object ImGuiThemeDarkTeal : ImGuiThemeBaseDark(Color(64, 200, 224), Color(128, 210, 224), Color(96, 205, 224))

/**
 * Glassmorphism teal ImGui theme.
 * Using colors from https://developer.apple.com/design/human-interface-guidelines/color#macOS-system-colors
 */
object ImGuiThemeGlassmorphismTeal :
    ImGuiThemeBaseGlassmorphism(Color(48, 176, 199), Color(108, 185, 199), Color(78, 181, 199))
