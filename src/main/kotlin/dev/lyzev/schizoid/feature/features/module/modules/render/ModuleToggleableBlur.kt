/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.render

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import dev.lyzev.api.events.EventListener
import dev.lyzev.api.events.EventRenderWorld
import dev.lyzev.api.events.on
import dev.lyzev.api.opengl.Render
import dev.lyzev.api.opengl.WrappedFramebuffer
import dev.lyzev.api.opengl.clear
import dev.lyzev.api.opengl.shader.Shader
import dev.lyzev.api.opengl.shader.ShaderAcrylic
import dev.lyzev.api.opengl.shader.ShaderDepth
import dev.lyzev.api.opengl.shader.ShaderTint
import dev.lyzev.api.opengl.shader.blur.Blurs
import dev.lyzev.api.setting.settings.option
import dev.lyzev.api.setting.settings.slider
import dev.lyzev.api.setting.settings.switch
import dev.lyzev.api.settings.Setting.Companion.neq
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.ModuleToggleable
import net.minecraft.client.MinecraftClient
import net.minecraft.util.math.MathHelper
import org.lwjgl.opengl.GL13

object ModuleToggleableBlur :
    ModuleToggleable("Blur", "All settings related to blur effects.", category = IFeature.Category.RENDER),
    EventListener {

    val method = option("Method", "The method used to blur the screen.", Blurs.DUAL_KAWASE, Blurs.entries)
    val useLinearSampling by switch(
        "Linear Sampling",
        "Enables linear sampling for the Gaussian blur.",
        true,
        method::value neq Blurs.GAUSSIAN
    )
    val strength by slider("Strength", "The strength of the blur effect.", 6, 1, 20)

    val acrylic by switch("Acrylic", "Adds an acrylic effect to the blur.", true)
    val luminosity by slider(
        "Luminosity",
        "The luminosity of the acrylic effect.",
        180,
        0,
        200,
        unit = "%%",
        allowOutOfBounds = true,
        hide = ::acrylic neq true
    )
    val noiseStrength by slider(
        "Noise Strength",
        "The strength of the noise effect.",
        100,
        0,
        100,
        unit = "%%",
        hide = ::acrylic neq true
    )
    val noiseSale by slider(
        "Noise Scale",
        "The scale of the noise effect.",
        100,
        0,
        100,
        unit = "%%",
        allowOutOfBounds = true,
        hide = {
            !acrylic || noiseStrength == 0
        })
    val RGBPuke by switch("RGB Puke", "Adds an RGB puke effect to the blur.", false)
    val RGBPukeOpacity by slider(
        "RGB Puke Opacity",
        "The opacity of the RGB puke effect.",
        30,
        1,
        100,
        unit = "%%",
        hide = {
            !RGBPuke || !acrylic
        })

    val dropShadow by switch("Drop Shadow", "Adds a drop shadow to the gui.", true)
    val dropShadowStrength by slider(
        "Drop Shadow Strength",
        "The strength of the drop shadow effect.",
        3,
        1,
        20,
        hide = ::dropShadow neq true
    )
    val dropShadowMultiplier by slider(
        "Drop Shadow Multiplier",
        "The multiplier of the drop shadow effect.",
        140,
        100,
        200,
        unit = "%%",
        hide = ::dropShadow neq true
    )
    val dropShadowRGBPuke by switch(
        "Drop Shadow RGB Puke",
        "Adds an RGB puke effect to the drop shadow.",
        false,
        hide = ::dropShadow neq true
    )

    val bloom by switch("Bloom", "Adds a bloom to the gui.", true)
    val bloomDouble by switch(
        "Bloom Double",
        "Blurs the texture again for a stronger bloom effect.",
        true,
        hide = ::bloom neq true
    )
    val bloomStrength by slider("Bloom", "The strength of the bloom effect.", 3, 1, 20, hide = ::bloom neq true)
    val bloomRGBPuke by switch(
        "Bloom RGB Puke",
        "Adds an RGB puke effect to the bloom.",
        false,
        hide = ::bloom neq true
    )

    val fog by switch("Fog", "Adds a fog effect to the blur.", false)
    val fogStrength by slider("Fog Strength", "The blur strength of the fog effect.", 6, 1, 20, hide = ::fog neq true)
    val fogDistance by slider(
        "Fog Distance",
        "The distance of the fog effect.",
        50,
        0,
        100,
        unit = "%%",
        hide = ::fog neq true
    )
    val fogRGBPuke by switch("Fog RGB Puke", "Adds an RGB puke effect to the fog.", false, hide = ::fog neq true)
    val fogRGBPukeOpacity by slider(
        "Fog RGB Puke Opacity",
        "The opacity of the RGB puke effect.",
        30,
        1,
        100,
        unit = "%%",
        hide = {
            !fogRGBPuke || !fog
        })

    private val fbo = WrappedFramebuffer()

    override val shouldHandleEvents: Boolean
        get() = isEnabled && fog && !mc.gameRenderer.isRenderingPanorama

    init {
        on<EventRenderWorld> {
            Render.store()
            RenderSystem.disableCull()
            RenderSystem.disableDepthTest()
            RenderSystem.defaultBlendFunc()
            RenderSystem.enableBlend()
            RenderSystem.depthMask(false)

            if (fogRGBPuke) {
                fbo.clear()
                fbo.beginWrite(true)
                ShaderTint.bind()
                RenderSystem.activeTexture(GL13.GL_TEXTURE0)
                MinecraftClient.getInstance().framebuffer.beginRead()
                ShaderTint["Tex0"] = 0
                ShaderTint["RGBPuke"] = fogRGBPuke
                ShaderTint["Opacity"] = fogRGBPukeOpacity / 100f
                ShaderTint["Alpha"] = false
                ShaderTint["Multiplier"] = 1f
                ShaderTint["Time"] = (System.nanoTime() - ShaderAcrylic.initTime) / 1000000000f
                val yaw = MathHelper.lerpAngleDegrees(mc.tickDelta, mc.player?.yaw ?: 0f, mc.player?.prevYaw ?: 0f)
                ShaderTint["Yaw"] = yaw
                val pitch = MathHelper.lerpAngleDegrees(mc.tickDelta, mc.player?.pitch ?: 0f, mc.player?.prevPitch ?: 0f)
                ShaderTint["Pitch"] = pitch
                Shader.drawFullScreen()
                ShaderTint.unbind()
            }

            method.value.switchStrength(fogStrength)
            method.value.render(if (fogRGBPuke) fbo else mc.framebuffer, false)

            mc.framebuffer.beginWrite(true)
            ShaderDepth.bind()
            RenderSystem.activeTexture(GL13.GL_TEXTURE0)
            method.value.output.beginRead()
            ShaderDepth["Tex0"] = 0
            RenderSystem.activeTexture(GL13.GL_TEXTURE1)
            GlStateManager._bindTexture(mc.framebuffer.depthAttachment)
            ShaderDepth["Tex1"] = 1
            ShaderDepth["Near"] = .05f
            ShaderDepth["Far"] = mc.gameRenderer.farPlaneDistance
            ShaderDepth["MinThreshold"] = .004f * fogDistance / 100f
            ShaderDepth["MaxThreshold"] = .28f * fogDistance / 100f
            Shader.drawFullScreen()
            ShaderDepth.unbind()

            RenderSystem.depthMask(true)
            Render.restore()
        }
    }
}
