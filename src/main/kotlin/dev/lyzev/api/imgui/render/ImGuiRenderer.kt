/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.imgui.render

import com.mojang.blaze3d.platform.GlConst
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import dev.lyzev.api.events.EventListener
import dev.lyzev.api.events.EventRenderImGuiContent
import dev.lyzev.api.events.EventSwapBuffers
import dev.lyzev.api.events.on
import dev.lyzev.api.imgui.ImGuiLoader.gl3
import dev.lyzev.api.imgui.ImGuiLoader.glfw
import dev.lyzev.api.opengl.WrappedFramebuffer
import dev.lyzev.api.opengl.clear
import dev.lyzev.api.opengl.shader.Shader
import dev.lyzev.api.opengl.shader.ShaderPassThrough
import dev.lyzev.api.opengl.shader.blur.BlurHelper
import dev.lyzev.schizoid.Schizoid
import dev.lyzev.schizoid.feature.features.gui.ImGuiScreen
import dev.lyzev.schizoid.feature.features.module.modules.render.ModuleToggleableBlur
import imgui.ImGui.*
import imgui.flag.ImGuiConfigFlags
import net.minecraft.util.Formatting
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
    private val fbo = WrappedFramebuffer("ImGuiRendererFBO")
    private val mask = WrappedFramebuffer("ImGuiRendererMask")

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
            fbo.beginWrite(false)
        }
    }

    /**
     * Finalizes ImGui rendering.
     * If the ModuleToggleableBlur is enabled, it ends writing to the framebuffer, applies a blur effect, and draws the framebuffer to the screen.
     */
    private fun postRenderImGui() {
        if (ModuleToggleableBlur.isEnabled) {
            fbo.endWrite()

            RenderSystem.disableCull()
            RenderSystem.defaultBlendFunc()
            RenderSystem.enableBlend()
            mask.clear()
            mask.beginWrite(false)
            ShaderPassThrough.bind()
            RenderSystem.activeTexture(GL13.GL_TEXTURE0)
            fbo.beginRead()
            ShaderPassThrough["Tex0"] = 0
            ShaderPassThrough["Scale"] = 1f
            ShaderPassThrough["Alpha"] = false
            Shader.drawFullScreen()
            ShaderPassThrough.unbind()

            BlurHelper.draw(mask, useDefaultFbo = true)

            RenderSystem.disableCull()
            RenderSystem.defaultBlendFunc()
            RenderSystem.enableBlend()

            GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, 0)
            GlStateManager._viewport(
                0,
                0,
                Schizoid.mc.framebuffer.viewportWidth,
                Schizoid.mc.framebuffer.viewportHeight
            )
            ShaderPassThrough.bind()
            RenderSystem.activeTexture(GL13.GL_TEXTURE0)
            fbo.beginRead()
            ShaderPassThrough["Tex0"] = 0
            ShaderPassThrough["Scale"] = 1f
            ShaderPassThrough["Alpha"] = true
            Shader.drawFullScreen()
            ShaderPassThrough.unbind()

//            if (ImGuiScreenFeature.theme.theme is ImGuiThemeBase && ModuleToggleableBlur.bloom) {
//                val theme = ImGuiScreenFeature.theme.theme as ImGuiThemeBase
//                bloom.clear()
//                bloom.beginWrite(true)
//                ShaderThreshold.bind()
//                RenderSystem.activeTexture(GlConst.GL_TEXTURE0)
//                fbo.beginRead()
//                ShaderThreshold["scene"] = 0
//                ShaderThreshold["primary"] = theme.primary
//                ShaderThreshold["secondary"] = theme.secondary
//                ShaderThreshold["accent"] = theme.accent
//                Shader.drawFullScreen()
//                ShaderThreshold.unbind()
//
//                BlurHelper.mode.switchStrength(ModuleToggleableBlur.bloomStrength)
//                BlurHelper.mode.render(bloom, true)
//                if (ModuleToggleableBlur.bloomDouble)
//                    BlurHelper.mode.render(BlurHelper.mode.output, true)
//
//                bloom.clear()
//                bloom.beginWrite(true)
//                ShaderTint.bind()
//                RenderSystem.activeTexture(GlConst.GL_TEXTURE0)
//                BlurHelper.mode.output.beginRead()
//                ShaderTint["uTexture"] = 0
//                ShaderTint["uColor"] = Vector3f(theme.primary.red / 255f, theme.primary.green / 255f, theme.primary.blue / 255f)
//                ShaderTint["uOpacity"] = 1f
//                ShaderTint["uRGBPuke"] = ModuleToggleableBlur.bloomRGBPuke
//                ShaderTint["uTime"] = System.nanoTime() / 1000000000f
//                Shader.drawFullScreen()
//                ShaderTint.unbind()
//
////                Schizoid.mc.framebuffer.beginWrite(true)
////                GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, 0)
////                ShaderBlend.bind()
////                RenderSystem.activeTexture(GL13.GL_TEXTURE0)
////                Schizoid.mc.framebuffer.beginRead()
////                ShaderBlend["scene"] = 0
////                RenderSystem.activeTexture(GL13.GL_TEXTURE1)
////                bloom.beginRead()
////                ShaderBlend["textureSampler"] = 1
////                Shader.drawFullScreen()
////                ShaderBlend.unbind()
//                Schizoid.mc.framebuffer.beginWrite(true)
//                GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, 0)
//                ShaderPassThrough.bind()
//                RenderSystem.activeTexture(GlConst.GL_TEXTURE0)
//                bloom.beginRead()
//                ShaderPassThrough["uTexture"] = 0
//                ShaderPassThrough["uScale"] = 1f
//                Shader.drawFullScreen()
//                ShaderPassThrough.unbind()
//            }

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

    /**
     * Implement Minecraft Color Code support for ImGui. Use color codes from the Formatting class.
     */
    fun minecraft(text: String) {
        val splits = text.split("§")
        val start = text.startsWith("§")
        for (i in splits.indices) {
            val split = splits[i]
            if (i == 0 && !start) {
                text(split)
                continue
            }
            if (split.isEmpty()) continue
            val color = split[0]
            val code = Formatting.byCode(color)
            if (code != null && code.isColor) {
                if (i > 0) {
                    sameLine(0f, 0f)
                }
                textColored(code.colorValue!! shl 8 or 0xFF, split.substring(1))
            } else {
                if (i > 0) {
                    sameLine(0f, 0f)
                }
                text("§$split")
            }
        }
    }

    override val shouldHandleEvents = true

    init {
        /**
         * Listens for the EventRenderImGui event.
         * It smooths the scrolling, renders ImGui, and applies the blur effect if the ModuleToggleableBlur is enabled.
         */
        on<EventSwapBuffers> {
            smoothScroll()

            glfw.newFrame()
            newFrame()
            // Call the event to render ImGui content.
            EventRenderImGuiContent.fire()
            // Render the current ImGui Screen.
            if (Schizoid.mc.currentScreen is ImGuiScreen) {
                getIO().wantCaptureKeyboard = true
                (Schizoid.mc.currentScreen as ImGuiScreen).renderImGui()
            } else
                getIO().wantCaptureKeyboard = false
            getIO().wantCaptureMouse = Schizoid.mc.currentScreen != null
            render()

            preRenderImGui()
            renderImGui()
            postRenderImGui()
        }
    }
}
