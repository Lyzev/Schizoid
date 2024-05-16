/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.imgui.font

import dev.lyzev.api.imgui.font.icon.FontAwesomeIcons
import dev.lyzev.schizoid.Schizoid
import imgui.ImFont
import imgui.ImGui.*

/**
 * Enum class representing different types of fonts.
 * Each enum constant is a different font.
 */
enum class ImGuiFonts(override val fontName: String, override val type: ImGuiFont.Type, override val size: Float) : ImGuiFont {
    OPEN_SANS_REGULAR("OpenSans-Regular", ImGuiFont.Type.TTF, 17.5f),
    OPEN_SANS_BOLD("OpenSans-Bold", ImGuiFont.Type.TTF, 17.5f),
    OPEN_SANS_BOLD_MEDIUM("OpenSans-Bold", ImGuiFont.Type.TTF, 25f),
    OPEN_SANS_BOLD_BIG("OpenSans-Bold", ImGuiFont.Type.TTF, 30f),
    LEAGUE_SPARTAN_EXTRA_BOLD("LeagueSpartan-ExtraBold", ImGuiFont.Type.TTF, 40f),
    FONT_AWESOME_REGULAR("fa-regular-400", ImGuiFont.Type.TTF, 10f) {
        override val glyphRanges: ShortArray
            get() = FontAwesomeIcons._IconRange
    },
    FONT_AWESOME_SOLID("fa-solid-900", ImGuiFont.Type.TTF, 10f) {
        override val glyphRanges: ShortArray
            get() = FontAwesomeIcons._IconRange
    },
    FONT_AWESOME_SOLID_BIG("fa-solid-900", ImGuiFont.Type.TTF, 50f) {
        override val glyphRanges: ShortArray
            get() = FontAwesomeIcons._IconRange
    };

    val font: ImFont

    override fun begin() = pushFont(font)

    override fun end() = popFont()

    init {
        val fontAtlas = getIO().fonts
        val `is` = ClassLoader.getSystemResourceAsStream("assets/${Schizoid.MOD_ID}/fonts/$fontName.${type.name.lowercase()}")
        requireNotNull(`is`) { "Font file not found: $fontName.${type.name.lowercase()}" }
        font = fontAtlas.addFontFromMemoryTTF(`is`.readAllBytes(), size, glyphRanges)
    }
}
