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
 * Light pink ImGui theme.
 * Using colors from https://developer.apple.com/design/human-interface-guidelines/color#macOS-system-colors
 */
object ImGuiThemeLightPink : ImGuiThemeBaseLight(Color(255, 45, 85), Color(255, 129, 153), Color(255, 87, 119))

/**
 * Dark pink ImGui theme.
 * Using colors from https://developer.apple.com/design/human-interface-guidelines/color#macOS-system-colors
 */
object ImGuiThemeDarkPink : ImGuiThemeBaseDark(Color(255, 55, 95), Color(255, 135, 159), Color(255, 95, 127))

/**
 * Glassmorphism pink ImGui theme.
 * Using colors from https://developer.apple.com/design/human-interface-guidelines/color#macOS-system-colors
 */
object ImGuiThemeGlassmorphismPink :
    ImGuiThemeBaseGlassmorphism(Color(255, 45, 85), Color(255, 129, 153), Color(255, 87, 119))
