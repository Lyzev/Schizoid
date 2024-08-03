/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.render

import com.mojang.blaze3d.systems.RenderSystem
import dev.lyzev.api.opengl.Render
import dev.lyzev.api.opengl.WrappedFramebuffer
import dev.lyzev.api.opengl.clear
import dev.lyzev.api.opengl.shader.Shader
import dev.lyzev.api.opengl.shader.ShaderNoAlpha
import dev.lyzev.api.setting.settings.slider
import dev.lyzev.schizoid.Schizoid
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.ModuleToggleableRenderImGuiContent
import imgui.ImGui.*
import net.fabricmc.loader.api.FabricLoader
import org.lwjgl.opengl.GL13
import kotlin.math.ceil

object ModuleToggleableRearView :
    ModuleToggleableRenderImGuiContent(
        "Rear View",
        "Shows a rear view of the player.",
        category = IFeature.Category.RENDER
    ) {

    val size by slider("Size", "The size of the rear view.", 15, 5, 100, "%%")

    val rearView = WrappedFramebuffer(useDepth = true)
    val final = WrappedFramebuffer()

    // net/minecraft/client/MinecraftClient takePanorama (Ljava/io/File;II)Lnet/minecraft/text/Text;
    private fun renderRearView(width: Int, height: Int) {
        if (mc.player == null)
            return
        val pitch = mc.player!!.pitch
        val yaw = mc.player!!.yaw
        val prevPitch = mc.player!!.prevPitch
        val prevYaw = mc.player!!.prevYaw
        mc.gameRenderer.setBlockOutlineEnabled(false)
        runCatching {
            mc.gameRenderer.isRenderingPanorama = true
            mc.worldRenderer.reloadTransparencyPostProcessor()

            mc.player!!.yaw = yaw - 180
            mc.player!!.pitch = pitch * -1
            mc.player!!.prevYaw = prevYaw - 180
            mc.player!!.prevPitch = prevPitch * -1

            rearView.clear()
            rearView.beginWrite(true)

            mc.gameRenderer.renderWorld(mc.renderTickCounter)
        }.onFailure {
            Schizoid.logger.error("Failed rendering rear view: ${it.message}")
        }
        mc.player!!.pitch = pitch
        mc.player!!.yaw = yaw
        mc.player!!.prevPitch = prevPitch
        mc.player!!.prevYaw = prevYaw
        mc.gameRenderer.setBlockOutlineEnabled(true)
        mc.gameRenderer.isRenderingPanorama = false
        mc.worldRenderer.reloadTransparencyPostProcessor()

        Render.prepare()
        final.clear()
        final.beginWrite(false)
        ShaderNoAlpha.bind()
        RenderSystem.activeTexture(GL13.GL_TEXTURE0)
        rearView.beginRead()
        ShaderNoAlpha["Tex0"] = 0
        Shader.drawFullScreen()
        ShaderNoAlpha.unbind()
        Render.post()

        mc.framebuffer.beginWrite(false)
    }

    override fun renderImGuiContent() {
        val width = ceil(mc.window.framebufferWidth * size / 100f)
        val height = ceil(mc.window.framebufferHeight * size / 100f)
        renderRearView(width.toInt(), height.toInt())
        val cursorPos = getCursorScreenPos()
        dummy(width, height)
        getWindowDrawList().addImageRounded(
            final.colorAttachment,
            cursorPos.x,
            cursorPos.y,
            cursorPos.x + width,
            cursorPos.y + height,
            0f,
            1f,
            1f,
            0f,
            -1,
            5f
        )
    }

    override val shouldHandleEvents: Boolean
        get() = isEnabled && isIngame && mc.interactionManager != null

    override val hide = FabricLoader.getInstance().getModContainer("iris").isPresent
}
