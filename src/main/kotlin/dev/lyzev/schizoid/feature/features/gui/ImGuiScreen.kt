/*
 * Copyright (c) 2023-2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.gui

import dev.lyzev.schizoid.feature.IFeature
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

abstract class ImGuiScreen(title: String) : Screen(Text.of(title)), IFeature {

    override fun applyBlur(delta: Float) {
        // Remove the blur effect
    }

    abstract fun renderImGui()

    override fun keybindReleased() {
        if (MinecraftClient.getInstance().currentScreen == this) close()
        else MinecraftClient.getInstance().setScreen(this)
    }
}
