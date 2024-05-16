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
 * Light brown ImGui theme.
 * Using colors from https://developer.apple.com/design/human-interface-guidelines/color#macOS-system-colors
 */
object ImGuiThemeLightBrown : ImGuiThemeBaseLight(Color(162, 132, 94), Color(162, 144, 121), Color(162, 138, 108))

/**
 * Dark brown ImGui theme.
 * Using colors from https://developer.apple.com/design/human-interface-guidelines/color#macOS-system-colors
 */
object ImGuiThemeDarkBrown : ImGuiThemeBaseDark(Color(172, 142, 104), Color(172, 154, 131), Color(172, 148, 118))

/**
 * Glassmorphism brown ImGui theme.
 * Using colors from https://developer.apple.com/design/human-interface-guidelines/color#macOS-system-colors
 */
object ImGuiThemeGlassmorphismBrown :
    ImGuiThemeBaseGlassmorphism(Color(162, 132, 94), Color(162, 144, 121), Color(162, 138, 108))
