/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.opengl.shader.blur

import com.mojang.blaze3d.platform.GlConst
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import dev.lyzev.api.opengl.Render
import dev.lyzev.api.opengl.WrappedFramebuffer
import dev.lyzev.api.opengl.clear
import dev.lyzev.api.opengl.shader.Shader.Companion.drawFullScreen
import dev.lyzev.api.opengl.shader.ShaderAcrylic
import dev.lyzev.api.opengl.shader.ShaderAdd
import dev.lyzev.api.opengl.shader.ShaderMask
import dev.lyzev.api.opengl.shader.ShaderTint
import dev.lyzev.schizoid.Schizoid
import dev.lyzev.schizoid.feature.features.module.modules.render.ModuleToggleableBlur
import dev.lyzev.schizoid.feature.features.module.modules.render.ModuleToggleableBlur.mc
import net.minecraft.client.MinecraftClient
import org.joml.Vector3f
import org.lwjgl.opengl.GL13.GL_TEXTURE0
import org.lwjgl.opengl.GL13.GL_TEXTURE1

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

    private val fbos = arrayOf(WrappedFramebuffer(), WrappedFramebuffer())
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
        ShaderAdd["Tex0"] = 0
        ShaderAdd["Tex1"] = 1
        ShaderAdd["Alpha"] = alpha
        drawFullScreen()
        ShaderAdd.unbind()
    }

    /**
     * Draws the blur effect.
     */
    fun draw(
        mask: WrappedFramebuffer = BlurHelper.mask,
        clearMask: Boolean = true,
        useDefaultFbo: Boolean = false,
        dropShadowColor: Vector3f = Vector3f(0f, 0f, 0f),
        blurStrength: Int = ModuleToggleableBlur.strength,
        dropShadowStrength: Int = ModuleToggleableBlur.dropShadowStrength
    ) {
        Render.store()
        setupRenderState()

        mode.switchStrength(blurStrength)
        mode.render()

        var i = 0

        // Acrylic (Luminosity, Noise and Tint)
        if (ModuleToggleableBlur.acrylic) {
            fbos[i].beginWrite(true)
            ShaderAcrylic.bind()
            RenderSystem.activeTexture(GL_TEXTURE0)
            mode.output.beginRead()
            ShaderAcrylic["Tex0"] = 0
            ShaderAcrylic["Luminosity"] = ModuleToggleableBlur.luminosity / 100f
            ShaderAcrylic["NoiseStrength"] = 0.04f * ModuleToggleableBlur.noiseStrength / 100f
            ShaderAcrylic["NoiseScale"] = 4000f * ModuleToggleableBlur.noiseSale / 100f
            drawFullScreen()
            ShaderAcrylic.unbind()
            i++
        }

        if (ModuleToggleableBlur.RGBPuke) {
            fbos[i].beginWrite(true)
            ShaderTint.bind()
            RenderSystem.activeTexture(GL_TEXTURE0)
            if (i == 0) mode.output.beginRead()
            else fbos[i - 1].beginRead()
            ShaderTint["Tex0"] = 0
            ShaderTint["RGBPuke"] = ModuleToggleableBlur.RGBPuke
            ShaderTint.set(
                "SV",
                ModuleToggleableBlur.RGBPukeSaturation / 100f,
                ModuleToggleableBlur.RGBPukeBrightness / 100f
            )
            ShaderTint["Opacity"] = ModuleToggleableBlur.RGBPukeOpacity / 100f
            ShaderTint["Alpha"] = false
            ShaderTint["Multiplier"] = 1f
            ShaderTint["Time"] = (System.nanoTime() - ShaderTint.initTime) / 1000000000f
            ShaderTint["Yaw"] = mc.player?.yaw ?: 0f
            ShaderTint["Pitch"] = mc.player?.pitch ?: 0f
            drawFullScreen()
            ShaderTint.unbind()
            i++
        }

        Schizoid.mc.framebuffer.beginWrite(true)
        if (useDefaultFbo)
            GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, 0)
        ShaderMask.bind()
        RenderSystem.activeTexture(GL_TEXTURE1)
        mask.beginRead()
        RenderSystem.activeTexture(GL_TEXTURE0)
        if (i == 0) mode.output.beginRead()
        else fbos[i - 1].beginRead()
        ShaderMask["Tex0"] = 0
        ShaderMask["Tex1"] = 1
        ShaderMask["Invert"] = false
        drawFullScreen()
        ShaderMask.unbind()

        if (ModuleToggleableBlur.dropShadow) {
            mode.switchStrength(dropShadowStrength)
            mode.render(mask, true)

            dropShadow.clear()
            dropShadow.beginWrite(true)
            ShaderTint.bind()
            RenderSystem.activeTexture(GL_TEXTURE0)
            mode.output.beginRead()
            ShaderTint["Tex0"] = 0
            ShaderTint["Color"] = dropShadowColor
            ShaderTint["RGBPuke"] = ModuleToggleableBlur.dropShadowRGBPuke
            ShaderTint.set(
                "SV",
                ModuleToggleableBlur.dropShadowRGBPukeSaturation / 100f,
                ModuleToggleableBlur.dropShadowRGBPukeBrightness / 100f
            )
            ShaderTint["Opacity"] = 1f
            ShaderTint["Alpha"] = true
            ShaderTint["Multiplier"] = ModuleToggleableBlur.dropShadowMultiplier / 100f
            ShaderTint["Time"] = (System.nanoTime() - ShaderTint.initTime) / 1000000000f
            ShaderTint["Yaw"] = mc.player?.yaw ?: 0f
            ShaderTint["Pitch"] = mc.player?.pitch ?: 0f
            drawFullScreen()
            ShaderTint.unbind()

            Schizoid.mc.framebuffer.beginWrite(true)
            if (useDefaultFbo)
                GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, 0)
            ShaderMask.bind()
            RenderSystem.activeTexture(GL_TEXTURE1)
            mask.beginRead()
            RenderSystem.activeTexture(GL_TEXTURE0)
            dropShadow.beginRead()
            ShaderMask["Tex0"] = 0
            ShaderMask["Tex1"] = 1
            ShaderMask["Invert"] = true
            drawFullScreen()
            ShaderMask.unbind()
        }

        RenderSystem.enableCull()
        Render.restore()
        if (clearMask) mask.clear()
    }
}
