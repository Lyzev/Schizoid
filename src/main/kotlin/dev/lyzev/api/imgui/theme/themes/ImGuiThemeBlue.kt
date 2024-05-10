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
 * Light blue ImGui theme.
 * Using colors from https://developer.apple.com/design/human-interface-guidelines/color#macOS-system-colors
 */
object ImGuiThemeLightBlue : ImGuiThemeBaseLight(Color(0, 122, 255), Color(102, 175, 255), Color(51, 149, 255))

/**
 * Dark blue ImGui theme.
 * Using colors from https://developer.apple.com/design/human-interface-guidelines/color#macOS-system-colors
 */
object ImGuiThemeDarkBlue : ImGuiThemeBaseDark(Color(10, 132, 255), Color(108, 181, 255), Color(59, 157, 255))

/**
 * Glassmorphism blue ImGui theme.
 * Using colors from https://developer.apple.com/design/human-interface-guidelines/color#macOS-system-colors
 */
object ImGuiThemeGlassmorphismBlue : ImGuiThemeBaseGlassmorphism(Color(0, 122, 255), Color(102, 175, 255), Color(51, 149, 255))
