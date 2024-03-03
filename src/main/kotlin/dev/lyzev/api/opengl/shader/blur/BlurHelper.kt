/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.opengl.shader.blur

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import dev.lyzev.api.opengl.shader.*
import dev.lyzev.api.opengl.shader.Shader.Companion.drawFullScreen
import dev.lyzev.schizoid.Schizoid
import dev.lyzev.schizoid.feature.features.module.modules.render.ModuleToggleableBlur
import dev.lyzev.schizoid.util.render.WrappedFramebuffer
import net.minecraft.client.MinecraftClient
import org.joml.Vector4f
import org.lwjgl.opengl.GL13.*

object BlurHelper {

    val blurMode: Blurs
        get() = ModuleToggleableBlur.method.value

    private var shouldBlur = false
    private val mask = WrappedFramebuffer()
    private val tmp = WrappedFramebuffer()

    private val acrylicBlur = WrappedFramebuffer()
    private val dropShadow = WrappedFramebuffer()

    @JvmStatic
    fun begin() {
        if (!ModuleToggleableBlur.isEnabled) return

        shouldBlur = true
        tmp.clear()
        tmp.beginWrite(true)
    }

    @JvmStatic
    fun addTmpToMask() {
        if (!ModuleToggleableBlur.isEnabled) return

        RenderSystem.disableCull()
        RenderSystem.defaultBlendFunc()
        RenderSystem.enableBlend()

        mask.beginWrite(true)
        ShaderAdd.bind()
        RenderSystem.activeTexture(GL_TEXTURE1)
        tmp.beginRead()
        RenderSystem.activeTexture(GL_TEXTURE0)
        mask.beginRead()
        ShaderAdd["u_s2Scene"] = 0
        ShaderAdd["u_s2Texture"] = 1
        ShaderAdd["u_bAlpha"] = false
        drawFullScreen()
        ShaderAdd.unbind()
    }

    @JvmStatic
    fun addTmpToScene() {
        if (!ModuleToggleableBlur.isEnabled) return

        RenderSystem.disableCull()
        RenderSystem.defaultBlendFunc()
        RenderSystem.enableBlend()

        MinecraftClient.getInstance().framebuffer.beginWrite(true)
        ShaderAdd.bind()
        RenderSystem.activeTexture(GL_TEXTURE1)
        tmp.beginRead()
        RenderSystem.activeTexture(GL_TEXTURE0)
        MinecraftClient.getInstance().framebuffer.beginRead()
        ShaderAdd["u_s2Scene"] = 0
        ShaderAdd["u_s2Texture"] = 1
        ShaderAdd["u_bAlpha"] = true
        drawFullScreen()
        ShaderAdd.unbind()
    }

    @JvmStatic
    fun end() {
        if (!ModuleToggleableBlur.isEnabled) return
        addTmpToMask()
        addTmpToScene()
        RenderSystem.enableCull()
    }

    fun draw(
        mask: WrappedFramebuffer = BlurHelper.mask,
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
        RenderSystem.disableCull()
        RenderSystem.defaultBlendFunc()
        RenderSystem.enableBlend()

        blurMode.switchStrength(blurStrength)
        blurMode.render()
        // Acrylic (Luminosity, Noise and Tint)
        if (ModuleToggleableBlur.acrylic) {
            acrylicBlur.beginWrite(true)
            ShaderAcrylic.bind()
            RenderSystem.activeTexture(GL_TEXTURE0)
            blurMode.getOutput().beginRead()
            ShaderAcrylic["uTexture"] = 0
            ShaderAcrylic["uLuminosity"] = ModuleToggleableBlur.luminosity / 100f
            ShaderAcrylic["uNoiseStrength"] = 0.03f * ModuleToggleableBlur.noiseStrength / 100f
            ShaderAcrylic["uNoiseScale"] = 4000f * ModuleToggleableBlur.noiseSale / 100f
            ShaderAcrylic["uOpacity"] = -1f
            ShaderAcrylic["uRGPuke"] = ModuleToggleableBlur.RGBPuke
            ShaderAcrylic["uRGPukeOpacity"] = ModuleToggleableBlur.RGBPukeOpacity / 100f
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
        else blurMode.getOutput().beginRead()
        ShaderMask["u_s2Texture"] = 0
        ShaderMask["u_s2Mask"] = 1
        ShaderMask["u_bInvert"] = false
        drawFullScreen()
        ShaderMask.unbind()

        if (ModuleToggleableBlur.dropShadow) {
            blurMode.switchStrength(dropShadowStrength)
            blurMode.render(mask, true)

            dropShadow.beginWrite(true)
            ShaderTint.bind()
            RenderSystem.activeTexture(GL_TEXTURE0)
            blurMode.getOutput().beginRead()
            ShaderTint["uTexture"] = 0
            ShaderTint["uColor"] = dropShadowColor
            ShaderTint["uOpacity"] = 1f
            ShaderTint["uRGPuke"] = ModuleToggleableBlur.dropShadowRGBPuke
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
        mask.clear()
    }
}
