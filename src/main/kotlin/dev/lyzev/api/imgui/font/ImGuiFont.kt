/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.imgui.font

import imgui.ImGui

/**
 * Represents a font that can be used in ImGui.
 */
interface ImGuiFont {

    /**
     * The name of the font.
     */
    val fontName: String

    /**
     * The glyph ranges for the font.
     */
    val glyphRanges: ShortArray
        get() = ImGui.getIO().fonts.glyphRangesDefault

    /**
     * The type of the font.
     */
    val type: Type

    /**
     * The size of the font.
     */
    val size: Float

    /**
     * Begins using this font.
     */
    fun begin()

    /**
     * Ends using this font.
     */
    fun end()

    /**
     * Represents the type of font.
     */
    enum class Type {
        TTF, OTF
    }
}
