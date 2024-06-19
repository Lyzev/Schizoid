/*
 * Copyright (c) 2023-2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.gui

import com.mojang.blaze3d.systems.RenderSystem
import dev.lyzev.api.glfw.GLFWKey
import dev.lyzev.api.opengl.shader.GLSLSandboxShader
import dev.lyzev.api.opengl.shader.ShaderGameOfLife
import dev.lyzev.api.opengl.shader.ShaderParticle
import dev.lyzev.api.setting.settings.keybinds
import dev.lyzev.schizoid.feature.IFeature
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

abstract class ImGuiScreen(title: String, override val desc: String, keybinds: Set<GLFWKey> = setOf()) : Screen(Text.of(title)), IFeature {

    override fun onDisplayed() {
        ShaderGameOfLife.queueGenPixels = true
    }

    override fun renderBackground(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        if (!isIngame) {
            backgroundShader.draw {
                this["primary"] = FeatureImGui.colorScheme[FeatureImGui.mode].primary
                this["secondary"] = FeatureImGui.colorScheme[FeatureImGui.mode].secondary
                this["accent"] = FeatureImGui.colorScheme[FeatureImGui.mode].accent
            }
        } else {
            FeatureImGui.colorScheme.renderInGameBackground(context, this.width, this.height, FeatureImGui.mode)
        }
        if (FeatureImGui.background != "None") {
            RenderSystem.disableCull()
            RenderSystem.defaultBlendFunc()
            RenderSystem.enableBlend()
            when (FeatureImGui.background) {
                "Particle" -> ShaderParticle.draw()
                "Game of Life" -> ShaderGameOfLife.draw()
            }
            RenderSystem.enableCull()
        }
    }

    override fun applyBlur(delta: Float) {
        // Remove the blur effect
    }

    abstract fun renderImGui()

    override fun keybindReleased() {
        if (MinecraftClient.getInstance().currentScreen == this) close()
        else MinecraftClient.getInstance().setScreen(this)
    }

    override val name = title
    override var keybinds by keybinds(
        "Keybinds",
        "All keys used to control the feature.",
        keybinds,
        setOf(GLFWKey.MOUSE_BUTTON_LEFT, GLFWKey.MOUSE_BUTTON_RIGHT, GLFWKey.MOUSE_BUTTON_MIDDLE)
    )
    override val category = IFeature.Category.RENDER

    companion object {
        private val backgroundShader = GLSLSandboxShader("BackgroundNoise")
    }
}
