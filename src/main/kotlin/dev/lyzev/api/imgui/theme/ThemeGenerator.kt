/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.imgui.theme

import java.awt.Color

// Function to calculate colors with specified saturation
fun adjustSaturation(color: Color, saturation: Int): Color {
    // Calculate hue, saturation, and brightness components
    val hsb = Color.RGBtoHSB(color.red, color.green, color.blue, null)

    // Adjust saturation and ensure it stays within 0-100 range
    val newSaturation = 100.0.coerceAtMost(0.0.coerceAtLeast(hsb[1] * saturation / 100.0))

    // Create the new color with adjusted saturation
    return Color.getHSBColor(hsb[0], newSaturation.toFloat(), hsb[2])
}

/**
 * Main function to generate the theme classes
 * Note: This class is not used in the project, it's just a helper to generate the theme classes
 */
fun main() {
    println("Enter the light color in decimal format (e.g. 255):")
    val lightColor = Color(println("R:").let { readln().toInt(10) }, println("G:").let { readln().toInt(10) }, println("B:").let { readln().toInt(10) })
    println("Enter the dark color in decimal format (e.g. 255):")
    val darkColor = Color(println("R:").let { readln().toInt(10) }, println("G:").let { readln().toInt(10) }, println("B:").let { readln().toInt(10) })

    // Calculate colors with 60% and 80% saturation
    val light60Sat = adjustSaturation(lightColor, 60)
    val light80Sat = adjustSaturation(lightColor, 80)
    val dark60Sat = adjustSaturation(darkColor, 60)
    val dark80Sat = adjustSaturation(darkColor, 80)

    // Get the color name from the user
    println("Color Name:")
    val nameColor = readln()

    // Generate the string/class with calculated colors
    println("""
import dev.lyzev.api.imgui.theme.ImGuiThemeBaseDark
import dev.lyzev.api.imgui.theme.ImGuiThemeBaseGlassmorphism
import dev.lyzev.api.imgui.theme.ImGuiThemeBaseLight
import java.awt.Color

/**
 * Light ${nameColor.lowercase()} ImGui theme.
 * Using colors from https://developer.apple.com/design/human-interface-guidelines/color#macOS-system-colors
 */
object ImGuiThemeLight${nameColor} : ImGuiThemeBaseLight(Color(${lightColor.red}, ${lightColor.green}, ${lightColor.blue}), Color(${light60Sat.red}, ${light60Sat.green}, ${light60Sat.blue}), Color(${light80Sat.red}, ${light80Sat.green}, ${light80Sat.blue}))

/**
 * Dark ${nameColor.lowercase()} ImGui theme.
 * Using colors from https://developer.apple.com/design/human-interface-guidelines/color#macOS-system-colors
 */
object ImGuiThemeDark${nameColor} : ImGuiThemeBaseDark(Color(${darkColor.red}, ${darkColor.green}, ${darkColor.blue}), Color(${dark60Sat.red}, ${dark60Sat.green}, ${dark60Sat.blue}), Color(${dark80Sat.red}, ${dark80Sat.green}, ${dark80Sat.blue}))

/**
 * Glassmorphism ${nameColor.lowercase()} ImGui theme.
 * Using colors from https://developer.apple.com/design/human-interface-guidelines/color#macOS-system-colors
 */
object ImGuiThemeGlassmorphism${nameColor} : ImGuiThemeBaseGlassmorphism(Color(${lightColor.red}, ${lightColor.green}, ${lightColor.blue}), Color(${light60Sat.red}, ${light60Sat.green}, ${light60Sat.blue}), Color(${light80Sat.red}, ${light80Sat.green}, ${light80Sat.blue}))
""".trimIndent())
}
