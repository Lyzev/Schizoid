/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.imgui.theme.themes

import dev.lyzev.api.imgui.theme.ImGuiThemeBaseDark
import dev.lyzev.api.imgui.theme.ImGuiThemeBaseLight
import java.awt.Color

/**
 * Light mint ImGui theme.
 * Using colors from https://developer.apple.com/design/human-interface-guidelines/color#macOS-system-colors
 */
object ImGuiThemeLightMint : ImGuiThemeBaseLight(Color(0, 199, 190), Color(80, 199, 194), Color(40, 199, 192))

/**
 * Dark mint ImGui theme.
 * Using colors from https://developer.apple.com/design/human-interface-guidelines/color#macOS-system-colors
 */
object ImGuiThemeDarkMint : ImGuiThemeBaseDark(Color(99, 230, 226), Color(151, 230, 228), Color(125, 230, 227))
