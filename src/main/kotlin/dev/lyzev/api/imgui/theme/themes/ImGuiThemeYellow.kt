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
 * Light yellow ImGui theme.
 * Using colors from https://developer.apple.com/design/human-interface-guidelines/color#macOS-system-colors
 */
object ImGuiThemeLightYellow : ImGuiThemeBaseLight(Color(255, 204, 0), Color(255, 224, 102), Color(255, 214, 51))

/**
 * Dark yellow ImGui theme.
 * Using colors from https://developer.apple.com/design/human-interface-guidelines/color#macOS-system-colors
 */
object ImGuiThemeDarkYellow : ImGuiThemeBaseDark(Color(255, 214, 10), Color(255, 230, 108), Color(255, 222, 59))

/**
 * Glassmorphism yellow ImGui theme.
 * Using colors from https://developer.apple.com/design/human-interface-guidelines/color#macOS-system-colors
 */
object ImGuiThemeGlassmorphismYellow :
    ImGuiThemeBaseGlassmorphism(Color(255, 204, 0), Color(255, 224, 102), Color(255, 214, 51))
