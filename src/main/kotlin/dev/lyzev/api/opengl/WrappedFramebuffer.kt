/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.opengl

import com.mojang.blaze3d.platform.GlConst
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.platform.TextureUtil
import com.mojang.blaze3d.systems.RenderSystem
import dev.lyzev.api.events.EventListener
import dev.lyzev.api.events.EventWindowResize
import dev.lyzev.api.events.on
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.Framebuffer
import net.minecraft.client.gl.SimpleFramebuffer
import net.minecraft.client.util.ScreenshotRecorder
import kotlin.math.ceil

/**
 * A simple wrapper around the [SimpleFramebuffer] class.
 * This class is used to create a framebuffer with a size multiplier which uses [GlConst.GL_LINEAR] as the texture filter.
 *
 * @param multi The multiplier for the framebuffer size.
 * @param useDepth Whether to use a depth attachment or not.
 * @see SimpleFramebuffer
 * @see EventListener
 */
class WrappedFramebuffer(val multi: Float = 1f, useDepth: Boolean = false) : SimpleFramebuffer(
    ceil(MinecraftClient.getInstance().window.framebufferWidth * multi).toInt(),
    ceil(MinecraftClient.getInstance().window.framebufferHeight * multi).toInt(),
    useDepth,
    MinecraftClient.IS_SYSTEM_MAC
), EventListener {

    /**
     * Override initFbo method to initialize the framebuffer with [GlConst.GL_LINEAR].
     */
    override fun initFbo(width: Int, height: Int, getError: Boolean) {
        RenderSystem.assertOnRenderThreadOrInit()
        val i = RenderSystem.maxSupportedTextureSize()
        require(!(width <= 0 || width > i || height <= 0 || height > i)) { "Window " + width + "x" + height + " size out of bounds (max. size: " + i + ")" }
        viewportWidth = width
        viewportHeight = height
        textureWidth = width
        textureHeight = height
        fbo = GlStateManager.glGenFramebuffers()
        colorAttachment = TextureUtil.generateTextureId()
        if (useDepthAttachment) {
            depthAttachment = TextureUtil.generateTextureId()
            GlStateManager._bindTexture(depthAttachment)
            GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MIN_FILTER, GlConst.GL_LINEAR)
            GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MAG_FILTER, GlConst.GL_LINEAR)
            GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_COMPARE_MODE, 0)
            GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_S, GlConst.GL_CLAMP_TO_EDGE)
            GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_T, GlConst.GL_CLAMP_TO_EDGE)
            GlStateManager._texImage2D(
                GlConst.GL_TEXTURE_2D, 0, GlConst.GL_DEPTH_COMPONENT,
                this.textureWidth,
                this.textureHeight, 0, GlConst.GL_DEPTH_COMPONENT, GlConst.GL_FLOAT, null
            )
        }
        setTexFilter(GlConst.GL_LINEAR)
        GlStateManager._bindTexture(colorAttachment)
        GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_S, GlConst.GL_CLAMP_TO_EDGE)
        GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_T, GlConst.GL_CLAMP_TO_EDGE)
        GlStateManager._texImage2D(
            GlConst.GL_TEXTURE_2D,
            0,
            GlConst.GL_RGBA8,
            textureWidth,
            textureHeight,
            0,
            GlConst.GL_RGBA,
            GlConst.GL_UNSIGNED_BYTE,
            null
        )
        GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, fbo)
        GlStateManager._glFramebufferTexture2D(
            GlConst.GL_FRAMEBUFFER, GlConst.GL_COLOR_ATTACHMENT0, GlConst.GL_TEXTURE_2D, colorAttachment, 0
        )
        if (useDepthAttachment) {
            GlStateManager._glFramebufferTexture2D(
                GlConst.GL_FRAMEBUFFER, GlConst.GL_DEPTH_ATTACHMENT, GlConst.GL_TEXTURE_2D,
                depthAttachment, 0
            )
        }
        checkFramebufferStatus()
        clear()
        endRead()
    }

    override val shouldHandleEvents = true

    /**
     * Initializes the framebuffer.
     */
    init {
        setClearColor(0f, 0f, 0f, 0f)
        clear()
        /**
         * Listens for window resize events and resizes the framebuffer accordingly.
         */
        on<EventWindowResize> {
            resize(
                (MinecraftClient.getInstance().window.framebufferWidth * multi).toInt(),
                (MinecraftClient.getInstance().window.framebufferHeight * multi).toInt(),
                MinecraftClient.IS_SYSTEM_MAC
            )
        }
    }
}

fun Framebuffer.clear() = clear(MinecraftClient.IS_SYSTEM_MAC)

fun Framebuffer.save() = ScreenshotRecorder.saveScreenshot(MinecraftClient.getInstance().runDirectory, this) {}
