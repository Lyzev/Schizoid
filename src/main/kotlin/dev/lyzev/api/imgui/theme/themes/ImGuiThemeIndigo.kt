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
 * Light indigo ImGui theme.
 * Using colors from https://developer.apple.com/design/human-interface-guidelines/color#macOS-system-colors
 */
object ImGuiThemeLightIndigo : ImGuiThemeBaseLight(Color(88, 86, 214), Color(138, 137, 214), Color(113, 112, 214))

/**
 * Dark indigo ImGui theme.
 * Using colors from https://developer.apple.com/design/human-interface-guidelines/color#macOS-system-colors
 */
object ImGuiThemeDarkIndigo : ImGuiThemeBaseDark(Color(94, 92, 230), Color(148, 147, 230), Color(121, 120, 230))

/**
 * Glassmorphism indigo ImGui theme.
 * Using colors from https://developer.apple.com/design/human-interface-guidelines/color#macOS-system-colors
 */
object ImGuiThemeGlassmorphismIndigo : ImGuiThemeBaseGlassmorphism(Color(88, 86, 214), Color(138, 137, 214), Color(113, 112, 214))
