/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.imgui.theme.themes

import dev.lyzev.api.imgui.theme.ImGuiThemeBaseDark
import dev.lyzev.api.imgui.theme.ImGuiThemeBaseLight
import java.awt.Color

/**
 * Light red ImGui theme.
 * Using colors from https://developer.apple.com/design/human-interface-guidelines/color#macOS-system-colors
 */
object ImGuiThemeLightRed : ImGuiThemeBaseLight(Color(255, 59, 48), Color(255, 137, 131), Color(255, 98, 89))

/**
 * Dark red ImGui theme.
 * Using colors from https://developer.apple.com/design/human-interface-guidelines/color#macOS-system-colors
 */
object ImGuiThemeDarkRed : ImGuiThemeBaseDark(Color(255, 69, 58), Color(255, 143, 137), Color(255, 106, 97))
