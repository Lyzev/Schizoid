/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.render

import com.mojang.blaze3d.systems.RenderSystem
import dev.lyzev.api.opengl.WrappedFramebuffer
import dev.lyzev.api.opengl.clear
import dev.lyzev.api.opengl.shader.Shader
import dev.lyzev.api.opengl.shader.ShaderFlip
import dev.lyzev.api.setting.settings.slider
import dev.lyzev.schizoid.Schizoid
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.ModuleToggleableRenderImGuiContent
import imgui.ImGui.*
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.util.Util
import org.lwjgl.opengl.GL13
import kotlin.math.ceil

object ModuleToggleableRearView :
    ModuleToggleableRenderImGuiContent(
        "Rear View",
        "Shows a rear view of the player.",
        category = IFeature.Category.RENDER
    ) {

    val size by slider("Size", "The size of the rear view.", 15, 5, 100, "%%")

    private val rearView = WrappedFramebuffer(useDepth = true)
    private val flippedRearView = WrappedFramebuffer()

    // net/minecraft/client/MinecraftClient takePanorama (Ljava/io/File;II)Lnet/minecraft/text/Text;
    private fun renderRearView(width: Int, height: Int) {
        val framebufferWidth = mc.window.framebufferWidth
        val framebufferHeight = mc.window.framebufferHeight
        if (mc.player == null)
            return
        val pitch = mc.player!!.pitch
        val yaw = mc.player!!.yaw
        val prevPitch = mc.player!!.prevPitch
        val prevYaw = mc.player!!.prevYaw
        mc.gameRenderer.setBlockOutlineEnabled(false)
        try {
            mc.gameRenderer.isRenderingPanorama = true
            mc.window.framebufferWidth = width
            mc.window.framebufferHeight = height
            mc.worldRenderer.reloadTransparencyPostProcessor()

            mc.player!!.yaw = yaw - 180
            mc.player!!.pitch = pitch * -1
            mc.player!!.prevYaw = prevYaw - 180
            mc.player!!.prevPitch = prevPitch * -1

            if (rearView.viewportWidth != width || rearView.viewportHeight != height)
                rearView.resize(width, height, MinecraftClient.IS_SYSTEM_MAC)
            if (flippedRearView.viewportWidth != width || flippedRearView.viewportHeight != height)
                flippedRearView.resize(width, height, MinecraftClient.IS_SYSTEM_MAC)

            rearView.clear()
            rearView.beginWrite(true)
            mc.gameRenderer.renderWorld(
                if (mc.paused) mc.pausedTickDelta else mc.renderTickCounter.tickDelta,
                Util.getMeasuringTimeNano()
            )

            RenderSystem.disableCull()
            RenderSystem.defaultBlendFunc()
            RenderSystem.enableBlend()

            flippedRearView.clear()
            flippedRearView.beginWrite(true)
            ShaderFlip.bind()
            RenderSystem.activeTexture(GL13.GL_TEXTURE0)
            rearView.beginRead()
            ShaderFlip["scene"] = 0
            Shader.drawFullScreen()
            ShaderFlip.unbind()

            RenderSystem.enableCull()
        } catch (e: Exception) {
            Schizoid.logger.error(e)
        } finally {
            mc.player!!.pitch = pitch
            mc.player!!.yaw = yaw
            mc.player!!.prevPitch = prevPitch
            mc.player!!.prevYaw = prevYaw
            mc.gameRenderer.setBlockOutlineEnabled(true)
            mc.gameRenderer.isRenderingPanorama = false
            mc.window.framebufferWidth = framebufferWidth
            mc.window.framebufferHeight = framebufferHeight
            mc.worldRenderer.reloadTransparencyPostProcessor()
            mc.framebuffer.beginWrite(true)
        }
    }

    override fun renderImGuiContent() {
        val width = ceil(mc.window.framebufferWidth * size / 100f)
        val height = ceil(mc.window.framebufferHeight * size / 100f)
        renderRearView(width.toInt(), height.toInt())
        val cursorPos = getCursorScreenPos()
        dummy(width, height)
        getWindowDrawList().addImageRounded(flippedRearView.colorAttachment, cursorPos.x, cursorPos.y, cursorPos.x + width, cursorPos.y + height, 0f, 0f, 1f, 1f, -1, 5f)
    }

    override val shouldHandleEvents: Boolean
        get() = isEnabled && isIngame && mc.interactionManager != null

    override val hide = FabricLoader.getInstance().getModContainer("iris").isPresent
}
