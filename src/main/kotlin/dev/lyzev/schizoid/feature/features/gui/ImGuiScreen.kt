/*
 * Copyright (c) 2023-2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.gui

import com.mojang.blaze3d.systems.RenderSystem
import dev.lyzev.api.events.*
import dev.lyzev.api.opengl.shader.blur.BlurHelper
import dev.lyzev.schizoid.Schizoid.mc
import dev.lyzev.schizoid.feature.FeatureManager
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.modules.render.ModuleToggleableBlur
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

abstract class ImGuiScreen(title: String) : Screen(Text.of(title)), IFeature, EventListener {

    abstract fun renderImGui()

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        super<Screen>.render(context, mouseX, mouseY, delta)
        EventRenderImGuiScreen(this).fire()
    }

    override fun sendChatMessage(message: Text) = FeatureManager.sendChatMessage(message)

    override fun keybindReleased() {
        if (MinecraftClient.getInstance().currentScreen == this) close()
        else MinecraftClient.getInstance().setScreen(this)
    }

    override val shouldHandleEvents: Boolean
        get() = mc.currentScreen == null || mc.currentScreen == this

    init {
        on<EventPreRenderImGui> {
            BlurHelper.begin()
        }

        on<EventPostRenderImGui> {
            if (!ModuleToggleableBlur.isEnabled) return@on

            BlurHelper.addTmpToMask()
            BlurHelper.draw()
            BlurHelper.addTmpToScene()
            RenderSystem.enableCull()
        }
    }
}
