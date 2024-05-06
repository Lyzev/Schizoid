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
 * Light cyan ImGui theme.
 * Using colors from https://developer.apple.com/design/human-interface-guidelines/color#macOS-system-colors
 */
object ImGuiThemeLightCyan : ImGuiThemeBaseLight(Color(50, 173, 230), Color(122, 196, 230), Color(86, 184, 230))

/**
 * Dark cyan ImGui theme.
 * Using colors from https://developer.apple.com/design/human-interface-guidelines/color#macOS-system-colors
 */
object ImGuiThemeDarkCyan : ImGuiThemeBaseDark(Color(100, 210, 255), Color(162, 228, 255), Color(131, 219, 255))

/**
 * Glassmorphism cyan ImGui theme.
 * Using colors from https://developer.apple.com/design/human-interface-guidelines/color#macOS-system-colors
 */
object ImGuiThemeGlassmorphismCyan : ImGuiThemeBaseGlassmorphism(Color(50, 173, 230), Color(122, 196, 230), Color(86, 184, 230))
