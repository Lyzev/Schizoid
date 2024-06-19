/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.gui

import com.mojang.blaze3d.systems.RenderCall
import com.mojang.blaze3d.systems.RenderSystem
import dev.lyzev.api.events.EventListener
import dev.lyzev.api.events.EventOSThemeUpdate
import dev.lyzev.api.events.on
import dev.lyzev.api.glfw.GLFWKey
import dev.lyzev.api.imgui.theme.ImGuiThemes
import dev.lyzev.api.opengl.shader.ShaderGameOfLife
import dev.lyzev.api.opengl.shader.ShaderParticle
import dev.lyzev.api.setting.settings.option
import dev.lyzev.api.setting.settings.slider
import dev.lyzev.api.setting.settings.text
import dev.lyzev.api.settings.Setting.Companion.neq
import dev.lyzev.schizoid.feature.IFeature

object FeatureImGui : IFeature, EventListener {

    val mode by option("Mode", "The mode of the GUI.", ImGuiThemes.Mode.DARK, ImGuiThemes.Mode.entries) {
        RenderSystem.recordRenderCall(change)
    }

    val colorScheme by option(
        "Color Scheme",
        "The color scheme of the GUI.",
        ImGuiThemes.ORANGE,
        ImGuiThemes.entries
    ) {
        RenderSystem.recordRenderCall(change)
    }

    private val change: RenderCall = RenderCall {
        colorScheme.applyStyle(mode)
        colorScheme.applyColors(mode)
    }

    val background by option(
        "Background",
        "The background of the GUI.",
        "None",
        arrayOf("None", "Particle", "Game of Life")
    ) {
        if (it == "Particle") {
            ShaderParticle.init()
        } else {
            ShaderParticle.delete()
        }
        if (it == "Game of Life") {
            ShaderGameOfLife.init()
        } else {
            ShaderGameOfLife.delete()
        }
    }

    val particleAmount by slider(
        "Particle Amount",
        "The amount of particles.",
        100,
        1,
        999,
        "k",
        onlyUpdateOnRelease = true,
        hide = FeatureImGui::background neq "Particle"
    ) {
        ShaderParticle.amount = it * 1_000
        ShaderParticle.reload()
    }

    val gameOfLifeTps by slider(
        "Game of Life TPS",
        "The ticks per second of the game of life.",
        10,
        1,
        40,
        "tps",
        hide = FeatureImGui::background neq "Game of Life"
    ) {
        ShaderGameOfLife.deltaTime = 1000 / it
    }
    val gameOfLifeSize by slider(
        "Game of Life Size",
        "The size of the game of life.",
        3,
        1,
        5,
        onlyUpdateOnRelease = true,
        hide = FeatureImGui::background neq "Game of Life"
    ) {
        ShaderGameOfLife.size = it
        ShaderGameOfLife.reload()
    }
    val gameOfLifeRulestring by text(
        "Game of Life Rulestring",
        "The rulestring of the game of life.",
        "B3/S236",
        true,
        Regex("B[0-8]+/S[0-8]+"),
        hide = FeatureImGui::background neq "Game of Life"
    ) {
        val rulestring = it.uppercase()
        ShaderGameOfLife.b = rulestring.substringAfter("B").substringBefore("/")
        ShaderGameOfLife.s = rulestring.substringAfter("S")
        ShaderGameOfLife.reload()
    }

    override val name = "ImGui"
    override val desc = "Edit the ImGui theme and background."
    override var keybinds = setOf<GLFWKey>()
    override val category = IFeature.Category.RENDER
    override fun keybindReleased() {}
    override val shouldHandleEvents = true

    init {
        on<EventOSThemeUpdate> {
            RenderSystem.recordRenderCall(change)
        }
    }
}
