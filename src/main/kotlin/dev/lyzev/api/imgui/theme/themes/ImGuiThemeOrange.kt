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
 * Light orange ImGui theme.
 * Using colors from https://developer.apple.com/design/human-interface-guidelines/color#macOS-system-colors
 */
object ImGuiThemeLightOrange : ImGuiThemeBaseLight(Color(255, 149, 0), Color(255, 191, 102), Color(255, 170, 51))

/**
 * Dark orange ImGui theme.
 * Using colors from https://developer.apple.com/design/human-interface-guidelines/color#macOS-system-colors
 */
object ImGuiThemeDarkOrange : ImGuiThemeBaseDark(Color(255, 159, 10), Color(255, 197, 108), Color(255, 178, 59))

/**
 * Glassmorphism orange ImGui theme.
 * Using colors from https://developer.apple.com/design/human-interface-guidelines/color#macOS-system-colors
 */
object ImGuiThemeGlassmorphismOrange :
    ImGuiThemeBaseGlassmorphism(Color(255, 149, 0), Color(255, 191, 102), Color(255, 170, 51))
