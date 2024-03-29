/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.imgui.render

import com.mojang.blaze3d.systems.RenderSystem
import dev.lyzev.api.events.*
import dev.lyzev.api.imgui.ImGuiLoader.gl3
import dev.lyzev.api.imgui.ImGuiLoader.glfw
import dev.lyzev.api.opengl.WrappedFramebuffer
import dev.lyzev.api.opengl.shader.Shader
import dev.lyzev.api.opengl.shader.ShaderPassThrough
import dev.lyzev.api.opengl.shader.blur.BlurHelper
import dev.lyzev.schizoid.Schizoid
import dev.lyzev.schizoid.feature.features.gui.ImGuiScreen
import dev.lyzev.schizoid.feature.features.module.modules.render.ModuleToggleableBlur
import imgui.ImGui.*
import imgui.flag.ImGuiConfigFlags
import net.minecraft.client.MinecraftClient
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL13
import kotlin.math.max
import kotlin.math.min

/**
 * Singleton object responsible for rendering ImGui.
 * It handles ImGui rendering during the render event and provides methods for pre and post rendering.
 */
object ImGuiRenderer : EventListener {

    /**
     * The target scroll X value.
     */
    var targetScrollX = 0f

    /**
     * The target scroll Y value.
     */
    var targetScrollY = 0f

    /**
     * The framebuffer used for rendering the blur.
     */
    private val fbo = WrappedFramebuffer()

    /**
     * Renders ImGui.
     * It uses the gl3 object to render ImGui's draw data.
     * If ImGui's IO has the ViewportsEnable flag, it updates and renders ImGui's platform windows.
     */
    private fun renderImGui() {
        gl3.renderDrawData(getDrawData())
        if (getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
            val backupWindowPtr = GLFW.glfwGetCurrentContext()
            updatePlatformWindows()
            renderPlatformWindowsDefault()
            GLFW.glfwMakeContextCurrent(backupWindowPtr)
        }
    }

    /**
     * Prepares for ImGui rendering.
     * If the ModuleToggleableBlur is enabled, it clears the framebuffer and begins writing to it.
     */
    private fun preRenderImGui() {
        if (ModuleToggleableBlur.isEnabled) {
            fbo.clear()
            fbo.beginWrite(true)
        }
    }

    /**
     * Finalizes ImGui rendering.
     * If the ModuleToggleableBlur is enabled, it ends writing to the framebuffer, applies a blur effect, and draws the framebuffer to the screen.
     */
    private fun postRenderImGui() {
        if (ModuleToggleableBlur.isEnabled) {
            fbo.endWrite()

            BlurHelper.draw(fbo, clearMask = false)

            RenderSystem.disableCull()
            RenderSystem.defaultBlendFunc()
            RenderSystem.enableBlend()

            MinecraftClient.getInstance().framebuffer.beginWrite(true)
            ShaderPassThrough.bind()
            RenderSystem.activeTexture(GL13.GL_TEXTURE0)
            fbo.beginRead()
            ShaderPassThrough["uTexture"] = 0
            ShaderPassThrough["uScale"] = 1f
            Shader.drawFullScreen()
            ShaderPassThrough.unbind()

            RenderSystem.enableCull()
        }
    }

    /**
     * Smooths the scrolling of ImGui.
     * It calculates the scroll speed for both X and Y directions and applies it to ImGui's IO.
     */
    private fun smoothScroll() {
        val scrollXSpeed = if (targetScrollX > 0) {
            min(max(targetScrollX / 10f, .01f), targetScrollX)
        } else if (targetScrollX < 0) {
            max(min(targetScrollX / 10f, -.01f), targetScrollX)
        } else {
            0f
        }

        val scrollYSpeed = if (targetScrollY > 0) {
            min(max(targetScrollY / 10f, .01f), targetScrollY)
        } else if (targetScrollY < 0) {
            max(min(targetScrollY / 10f, -.01f), targetScrollY)
        } else {
            0f
        }

        targetScrollX -= scrollXSpeed
        targetScrollY -= scrollYSpeed

        getIO().mouseWheelH += scrollXSpeed
        getIO().mouseWheel += scrollYSpeed
    }

    override val shouldHandleEvents = true

    init {
        /**
         * Listens for the EventRenderImGui event.
         * It smooths the scrolling, renders ImGui, and applies the blur effect if the ModuleToggleableBlur is enabled.
         */
        on<EventRenderImGui> {
            smoothScroll()

            glfw.newFrame()
            newFrame()
            // Call the event to render ImGui content.
            EventRenderImGuiContent.fire()
            // Render the current ImGui Screen.
            if (Schizoid.mc.currentScreen is ImGuiScreen)
                (Schizoid.mc.currentScreen as ImGuiScreen).renderImGui()
            render()

            preRenderImGui()
            renderImGui()
            postRenderImGui()
        }
    }
}
