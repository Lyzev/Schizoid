/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.opengl.shader.blur

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import dev.lyzev.api.opengl.WrappedFramebuffer
import dev.lyzev.api.opengl.shader.Shader.Companion.drawFullScreen
import dev.lyzev.api.opengl.shader.ShaderAcrylic
import dev.lyzev.api.opengl.shader.ShaderAdd
import dev.lyzev.api.opengl.shader.ShaderMask
import dev.lyzev.api.opengl.shader.ShaderTint
import dev.lyzev.schizoid.Schizoid
import dev.lyzev.schizoid.feature.features.module.modules.render.ModuleToggleableBlur
import net.minecraft.client.MinecraftClient
import org.joml.Vector4f
import org.lwjgl.opengl.GL13.*

/**
 * A helper object for handling blur effects.
 */
object BlurHelper {

    /**
     * Gets the current blur mode.
     */
    val mode: Blurs
        get() = ModuleToggleableBlur.method.value

    private val mask = WrappedFramebuffer()
    private val tmp = WrappedFramebuffer()

    private val acrylicBlur = WrappedFramebuffer()
    private val dropShadow = WrappedFramebuffer()

    /**
     * Begins the blur process.
     */
    @JvmStatic
    fun begin() {
        if (!ModuleToggleableBlur.isEnabled) return
        tmp.clear()
        tmp.beginWrite(true)
    }

    /**
     * Ends the blur process.
     */
    @JvmStatic
    fun end() {
        if (!ModuleToggleableBlur.isEnabled) return
        addTmpToMask()
        addTmpToScene()
        RenderSystem.enableCull()
    }

    /**
     * Adds the temporary buffer to the mask.
     */
    @JvmStatic
    fun addTmpToMask() {
        if (!ModuleToggleableBlur.isEnabled) return
        setupRenderState()
        mask.beginWrite(true)
        drawTextures(tmp, mask, false)
    }

    /**
     * Adds the temporary buffer to the scene.
     */
    @JvmStatic
    fun addTmpToScene() {
        if (!ModuleToggleableBlur.isEnabled) return
        setupRenderState()
        MinecraftClient.getInstance().framebuffer.beginWrite(true)
        drawTextures(tmp, mask, true)
    }

    /**
     * Sets up the render state.
     */
    private fun setupRenderState() {
        RenderSystem.disableCull()
        RenderSystem.defaultBlendFunc()
        RenderSystem.enableBlend()
    }

    /**
     * Draws textures to the fbo.
     */
    private fun drawTextures(texture1: WrappedFramebuffer, texture0: WrappedFramebuffer, alpha: Boolean) {
        ShaderAdd.bind()
        RenderSystem.activeTexture(GL_TEXTURE1)
        texture1.beginRead()
        RenderSystem.activeTexture(GL_TEXTURE0)
        texture0.beginRead()
        ShaderAdd["uScene"] = 0
        ShaderAdd["uTexture"] = 1
        ShaderAdd["uAlpha"] = alpha
        drawFullScreen()
        ShaderAdd.unbind()
    }

    /**
     * Draws the blur effect.
     */
    fun draw(
        mask: WrappedFramebuffer = BlurHelper.mask,
        clearMask: Boolean = true,
        opacity: Float = 1f,
        dropShadowColor: Vector4f = Vector4f(0f, 0f, 0f, 1f),
        blurStrength: Int = ModuleToggleableBlur.strength,
        dropShadowStrength: Int = ModuleToggleableBlur.dropShadowStrength
    ) {
        val active = GlStateManager._getInteger(GL_ACTIVE_TEXTURE)
        val texture = GlStateManager._getInteger(GL_TEXTURE_BINDING_2D)
        RenderSystem.activeTexture(GL_TEXTURE0)
        val texture0 = GlStateManager._getInteger(GL_TEXTURE_BINDING_2D)
        RenderSystem.activeTexture(GL_TEXTURE1)
        val texture1 = GlStateManager._getInteger(GL_TEXTURE_BINDING_2D)
        setupRenderState()

        mode.switchStrength(blurStrength)
        mode.render()
        // Acrylic (Luminosity, Noise and Tint)
        if (ModuleToggleableBlur.acrylic) {
            acrylicBlur.beginWrite(true)
            ShaderAcrylic.bind()
            RenderSystem.activeTexture(GL_TEXTURE0)
            mode.output.beginRead()
            ShaderAcrylic["uTexture"] = 0
            ShaderAcrylic["uLuminosity"] = ModuleToggleableBlur.luminosity / 100f
            ShaderAcrylic["uNoiseStrength"] = 0.03f * ModuleToggleableBlur.noiseStrength / 100f
            ShaderAcrylic["uNoiseScale"] = 4000f * ModuleToggleableBlur.noiseSale / 100f
            ShaderAcrylic["uOpacity"] = -1f
            ShaderAcrylic["uRGBPuke"] = ModuleToggleableBlur.RGBPuke
            ShaderAcrylic["uRGBPukeOpacity"] = ModuleToggleableBlur.RGBPukeOpacity / 100f
            ShaderAcrylic["uTime"] = System.nanoTime() / 1000000000f
            drawFullScreen()
            ShaderAcrylic.unbind()
        }

        Schizoid.mc.framebuffer.beginWrite(true)
        ShaderMask.bind()
        RenderSystem.activeTexture(GL_TEXTURE1)
        mask.beginRead()
        RenderSystem.activeTexture(GL_TEXTURE0)
        if (ModuleToggleableBlur.acrylic) acrylicBlur.beginRead()
        else mode.output.beginRead()
        ShaderMask["u_s2Texture"] = 0
        ShaderMask["u_s2Mask"] = 1
        ShaderMask["u_bInvert"] = false
        drawFullScreen()
        ShaderMask.unbind()

        if (ModuleToggleableBlur.dropShadow) {
            mode.switchStrength(dropShadowStrength)
            mode.render(mask, true)

            dropShadow.beginWrite(true)
            ShaderTint.bind()
            RenderSystem.activeTexture(GL_TEXTURE0)
            mode.output.beginRead()
            ShaderTint["uTexture"] = 0
            ShaderTint["uColor"] = dropShadowColor
            ShaderTint["uOpacity"] = 1f
            ShaderTint["uRGBPuke"] = ModuleToggleableBlur.dropShadowRGBPuke
            ShaderTint["uTime"] = System.nanoTime() / 1000000000f
            drawFullScreen()
            ShaderTint.unbind()

            MinecraftClient.getInstance().framebuffer.beginWrite(true)
            ShaderMask.bind()
            RenderSystem.activeTexture(GL_TEXTURE1)
            mask.beginRead()
            RenderSystem.activeTexture(GL_TEXTURE0)
            dropShadow.beginRead()
            ShaderMask["u_s2Texture"] = 0
            ShaderMask["u_s2Mask"] = 1
            ShaderMask["u_bInvert"] = true
            drawFullScreen()
            ShaderMask.unbind()
        }

        RenderSystem.enableCull()
        RenderSystem.activeTexture(GL_TEXTURE1)
        RenderSystem.bindTexture(texture1)
        RenderSystem.activeTexture(GL_TEXTURE0)
        RenderSystem.bindTexture(texture0)
        RenderSystem.activeTexture(active)
        RenderSystem.bindTexture(texture)
        if (clearMask) mask.clear()
    }
}
