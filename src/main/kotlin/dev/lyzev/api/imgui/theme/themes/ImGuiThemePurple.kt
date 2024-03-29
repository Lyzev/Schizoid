/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.imgui.theme.themes

import dev.lyzev.api.imgui.theme.ImGuiThemeBaseDark
import dev.lyzev.api.imgui.theme.ImGuiThemeBaseLight
import java.awt.Color

/**
 * Light purple ImGui theme.
 * Using colors from https://developer.apple.com/design/human-interface-guidelines/color#macOS-system-colors
 */
object ImGuiThemeLightPurple : ImGuiThemeBaseLight(Color(175, 82, 222), Color(194, 138, 222), Color(184, 110, 222))

/**
 * Dark purple ImGui theme.
 * Using colors from https://developer.apple.com/design/human-interface-guidelines/color#macOS-system-colors
 */
object ImGuiThemeDarkPurple : ImGuiThemeBaseDark(Color(191, 90, 242), Color(211, 151, 242), Color(201, 120, 242))
