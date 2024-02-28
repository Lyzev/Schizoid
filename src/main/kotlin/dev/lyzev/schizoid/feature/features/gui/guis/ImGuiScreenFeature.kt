/*
 * Copyright (c) 2023-2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.gui.guis

import com.mojang.blaze3d.systems.RenderSystem
import su.mandora.tarasande.util.render.animation.EasingFunction
import su.mandora.tarasande.util.render.animation.TimeAnimator
import dev.lyzev.api.events.EventKeybindsRequest
import dev.lyzev.api.events.EventKeybindsResponse
import dev.lyzev.api.events.on
import dev.lyzev.api.glfw.GLFWKey
import dev.lyzev.api.setting.settings.keybinds
import dev.lyzev.api.setting.settings.option
import dev.lyzev.api.setting.settings.slider
import dev.lyzev.api.setting.settings.switch
import dev.lyzev.schizoid.Schizoid
import dev.lyzev.schizoid.Schizoid.mc
import dev.lyzev.schizoid.feature.Feature
import dev.lyzev.schizoid.feature.features.gui.ImGuiScreen
import net.minecraft.client.gui.DrawContext
import net.minecraft.util.Identifier
import org.lwjgl.glfw.GLFW


object ImGuiScreenFeature : ImGuiScreen("Feature Screen") {

    private val texturesMario = Array(3) {
        Identifier(Schizoid.MOD_ID, "textures/mario_$it.png")
    }
    private var isMarioRunning = false
    private val timeAnimatorMario = TimeAnimator(8000)

    private val animationMario by option(
        "Mario Animation",
        "Enables the Mario animation.",
        EasingFunction.INT_OUT_ELASTIC,
        EasingFunction.entries
    )
    private val speedMario by slider(
        "Mario Speed",
        "The speed of the Mario animation.",
        5000,
        1000,
        10000,
        "ms",
        true
    ) {
        timeAnimatorMario.animationLength = it.toLong()
    }

    private var waitingForInput = -1L
    private var isWaitingForInput = false
    private const val TIMEOUT = 5000

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)

        if (isWaitingForInput && System.currentTimeMillis() - waitingForInput > TIMEOUT) {
            EventKeybindsResponse(GLFW.GLFW_KEY_UNKNOWN).fire()
            isWaitingForInput = false
        }

        if (!isMarioRunning && timeAnimatorMario.isCompleted()) {
            timeAnimatorMario.setProgress(.0)
            return
        }
        isMarioRunning = true

        val x = -32 + ((mc.window.scaledWidth + 32) * animationMario.ease(timeAnimatorMario.getProgress())).toInt()
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
        context?.drawTexture(
            texturesMario[(System.currentTimeMillis() / 100.0 % texturesMario.size).toInt()],
            x,
            mc.window.scaledHeight - 32,
            32,
            32,
            0f,
            0f,
            400,
            400,
            400,
            400
        )
        isMarioRunning = x < mc.window.scaledWidth
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (isWaitingForInput) {
            isWaitingForInput = false
            EventKeybindsResponse(keyCode).fire()
            return true
        } else if (keybinds.contains(GLFWKey[keyCode])) {
            keybindReleased()
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (isWaitingForInput) {
            isWaitingForInput = false
            EventKeybindsResponse(button).fire()
            return true
        } else if (keybinds.contains(GLFWKey[button])) {
            keybindReleased()
            return true
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun renderImGui() = Feature.Category.entries.forEach { category ->
        category.render()
    }


    init {
        on<EventKeybindsRequest> {
            if (isWaitingForInput) EventKeybindsResponse(GLFW.GLFW_KEY_UNKNOWN).fire()
            waitingForInput = System.currentTimeMillis()
            isWaitingForInput = true
        }
    }

    override fun shouldPause(): Boolean = false

    val darkMode by switch("Dark Mode", "Enables the dark mode.", true)

    override val name: String
        get() = "Feature Screen"
    override val desc: String
        get() = "Displays all features and their respective settings."
    override var keybinds by keybinds(
        "Keybinds",
        "All keys used to control the feature.",
        mutableSetOf(GLFWKey.INSERT, GLFWKey.RIGHT_SHIFT)
    ) {
        it.removeIf { key -> key == GLFWKey.MOUSE_BUTTON_LEFT || key == GLFWKey.MOUSE_BUTTON_RIGHT || key == GLFWKey.MOUSE_BUTTON_MIDDLE }
    }
    override val category: Feature.Category
        get() = Feature.Category.RENDER
}
