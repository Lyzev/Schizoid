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
 * Light green ImGui theme.
 * Using colors from https://developer.apple.com/design/human-interface-guidelines/color#macOS-system-colors
 */
object ImGuiThemeLightGreen : ImGuiThemeBaseLight(Color(52, 199, 89), Color(111, 199, 133), Color(81, 199, 111))

/**
 * Dark green ImGui theme.
 * Using colors from https://developer.apple.com/design/human-interface-guidelines/color#macOS-system-colors
 */
object ImGuiThemeDarkGreen : ImGuiThemeBaseDark(Color(48, 209, 88), Color(112, 209, 136), Color(80, 209, 112))

/**
 * Glassmorphism purple ImGui theme.
 * Using colors from https://developer.apple.com/design/human-interface-guidelines/color#macOS-system-colors
 */
object ImGuiThemeGlassmorphismGreen : ImGuiThemeBaseGlassmorphism(Color(52, 199, 89), Color(111, 199, 133), Color(81, 199, 111))
