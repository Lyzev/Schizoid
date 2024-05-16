/*
 * Copyright (c) 2023-2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.gui.guis

import com.mojang.blaze3d.systems.RenderCall
import com.mojang.blaze3d.systems.RenderSystem
import dev.lyzev.api.animation.EasingFunction
import dev.lyzev.api.animation.TimeAnimator
import dev.lyzev.api.events.*
import dev.lyzev.api.glfw.GLFWKey
import dev.lyzev.api.imgui.render.renderable.ImGuiRenderableConfigManager
import dev.lyzev.api.imgui.render.renderable.ImGuiRenderableDeveloperTool
import dev.lyzev.api.imgui.render.renderable.ImGuiRenderableSearch
import dev.lyzev.api.imgui.theme.ImGuiThemes
import dev.lyzev.api.opengl.shader.ShaderParticle
import dev.lyzev.api.setting.settings.keybinds
import dev.lyzev.api.setting.settings.option
import dev.lyzev.api.setting.settings.slider
import dev.lyzev.api.setting.settings.switch
import dev.lyzev.api.settings.Setting.Companion.neq
import dev.lyzev.schizoid.Schizoid
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.gui.ImGuiScreen
import net.minecraft.client.gui.DrawContext
import net.minecraft.util.Identifier
import org.lwjgl.glfw.GLFW

object ImGuiScreenFeature : ImGuiScreen("Feature Screen"), EventListener {

    val mode by option("Mode", "The mode of the GUI.", ImGuiThemes.Mode.SYSTEM, ImGuiThemes.Mode.entries) {
        RenderSystem.recordRenderCall(change)
    }

    val colorScheme by option("Color Scheme", "The color scheme of the GUI.", ImGuiThemes.TEAL, ImGuiThemes.entries) {
        RenderSystem.recordRenderCall(change)
    }

    private val change: RenderCall = RenderCall {
        colorScheme.applyStyle(mode)
        colorScheme.applyColors(mode)
    }

    val particles: Boolean by switch("Particles", "Enables the particle shader.\nToggle to reload.", false) {
        if (!it) return@switch
        ShaderParticle.amount = particleAmount * 1000
        ShaderParticle.reload()
    }
    val particleAmount by slider("Particle Amount", "The amount of particles.", 100, 1, 1000, "k", hide = ::particles neq true)

    private val texturesMario = Array(3) {
        Identifier(Schizoid.MOD_ID, "textures/mario_$it.png")
    }
    private var isMarioRunning = false
    private val timeAnimatorMario = TimeAnimator(8000)

    val animationMario by option(
        "Mario Animation",
        "The animation type.",
        EasingFunction.IN_OUT_ELASTIC,
        EasingFunction.entries
    )
    val speedMario by slider(
        "Mario Speed",
        "The speed of the animation.",
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

    val search = ImGuiRenderableSearch()
    val devTools = ImGuiRenderableDeveloperTool()
    val configManager = ImGuiRenderableConfigManager()

    override fun renderInGameBackground(context: DrawContext) =
        colorScheme.renderInGameBackground(context, this.width, this.height, mode)

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)

        if (particles) {
            RenderSystem.disableCull()
            RenderSystem.defaultBlendFunc()
            RenderSystem.enableBlend()
            ShaderParticle.draw()
            RenderSystem.enableCull()
        }

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
        } else if (keyCode == GLFWKey.LEFT_SHIFT.code) {
            search.open()
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
            search.close()
            keybindReleased()
            return true
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }


    override fun renderImGui() {
        if (Schizoid.DEVELOPER_MODE)
            devTools.render()
        search.render()
        configManager.render()
        IFeature.Category.entries.forEach(IFeature.Category::render)
    }

    init {
        on<EventKeybindsRequest> {
            if (isWaitingForInput) EventKeybindsResponse(GLFW.GLFW_KEY_UNKNOWN).fire()
            waitingForInput = System.currentTimeMillis()
            isWaitingForInput = true
        }
        on<EventOSThemeUpdate> {
            RenderSystem.recordRenderCall(change)
        }
    }

    override val shouldHandleEvents: Boolean
        get() = mc.currentScreen == null || mc.currentScreen == this

    override fun shouldPause(): Boolean = false

    override val name = "Feature Screen"
    override val desc = "Displays all features and their respective settings."
    override var keybinds by keybinds(
        "Keybinds",
        "All keys used to control the feature.",
        setOf(GLFWKey.INSERT, GLFWKey.RIGHT_SHIFT),
        setOf(GLFWKey.MOUSE_BUTTON_LEFT, GLFWKey.MOUSE_BUTTON_RIGHT, GLFWKey.MOUSE_BUTTON_MIDDLE)
    )

    override val category = IFeature.Category.RENDER
}
