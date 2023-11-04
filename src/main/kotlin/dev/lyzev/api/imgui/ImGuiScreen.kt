/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.imgui

import dev.lyzev.api.events.EventRenderImGuiScreen
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

abstract class ImGuiScreen(title: String) : Screen(Text.of(title)) {

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        EventRenderImGuiScreen(this).fire()
    }

    abstract fun renderImGui()
}
