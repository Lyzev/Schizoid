/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.imgui.font

import dev.lyzev.schizoid.Schizoid
import imgui.ImFont
import imgui.ImGui.*

/**
 * Enum class representing different types of fonts.
 * Each enum constant is a different font.
 */
enum class ImGuiFonts(override val fontName: String, override val glyphRanges: ShortArray, override val type: ImGuiFont.Type, override val size: Float) : ImGuiFont {
    HELVETICA_NEUE("HelveticaNeue", getIO().fonts.glyphRangesDefault, ImGuiFont.Type.TTF, 17.5f),
    HELVETICA_NEUE_BOLD("HelveticaNeue-Bold", getIO().fonts.glyphRangesDefault, ImGuiFont.Type.TTF, 17.5f);

    val font: ImFont

    override fun begin() = pushFont(font)

    override fun end() = popFont()

    init {
        val fontAtlas = getIO().fonts
        val `is` = javaClass.classLoader.getResourceAsStream("assets/${Schizoid.MOD_ID}/fonts/$fontName.${type.name.lowercase()}")
        requireNotNull(`is`) { "Font file not found: $fontName.${type.name.lowercase()}" }
        font = fontAtlas.addFontFromMemoryTTF(`is`.readAllBytes(), 17.5f, glyphRanges)
    }
}
