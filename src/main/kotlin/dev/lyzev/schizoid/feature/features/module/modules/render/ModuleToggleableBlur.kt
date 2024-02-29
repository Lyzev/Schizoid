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
import dev.lyzev.api.opengl.shader.Shader
import dev.lyzev.api.opengl.shader.ShaderAcrylic
import dev.lyzev.api.opengl.shader.ShaderDepth
import dev.lyzev.api.opengl.shader.blur.Blurs
import dev.lyzev.api.setting.settings.option
import dev.lyzev.api.setting.settings.slider
import dev.lyzev.api.setting.settings.switch
import dev.lyzev.api.settings.Setting.Companion.neq
import dev.lyzev.schizoid.feature.features.module.ModuleToggleable
import dev.lyzev.schizoid.util.render.WrappedFramebuffer
import net.minecraft.client.MinecraftClient
import org.lwjgl.opengl.GL13

object ModuleToggleableBlur :
    ModuleToggleable("Blur", "All settings related to blur effects.", category = Category.RENDER), EventListener {

    val method by option("Method", "The method used to blur the screen.", Blurs.DUAL_KAWASE, Blurs.entries)

    val strength by slider("Strength", "The strength of the blur effect.", 9, 1, 20)

    val acrylic by switch("Acrylic", "Adds an acrylic effect to the blur.", true)
    val luminosity by slider("Luminosity", "The luminosity of the acrylic effect.", 180, 0, 200, unit = "%%", allowOutOfBounds = true, hide = ::acrylic neq true)
    val noiseStrength by slider("Noise Strength", "The strength of the noise effect.", 50, 0, 100, unit = "%%", hide = ::acrylic neq true)
    val noiseSale by slider("Noise Scale", "The scale of the noise effect.", 100, 0, 100, unit = "%%", allowOutOfBounds = true, hide = {
        !acrylic || noiseStrength == 0
    })
    val RGBPuke by switch("RGB Puke", "Adds an RGB puke effect to the blur.", false)
    val RGBPukeOpacity by slider("RGB Puke Opacity", "The opacity of the RGB puke effect.", 30, 1, 100, unit = "%%", hide = {
        !RGBPuke || !acrylic
    })

    val dropShadow by switch("Drop Shadow", "Adds a drop shadow to the blur.", true)
    val dropShadowStrength by slider("Drop Shadow Strength", "The strength of the drop shadow effect.", 9, 1, 20, hide = ::dropShadow neq true)
    val dropShadowRGBPuke by switch("Drop Shadow RGB Puke", "Adds an RGB puke effect to the drop shadow.", false, hide = ::dropShadow neq true)

    val fog by switch("Fog", "Adds a fog effect to the blur.", true)
    val fogStrength by slider("Fog Strength", "The blur strength of the fog effect.", 9, 1, 20, hide = ::fog neq true)
    val fogDistance by slider("Fog Distance", "The distance of the fog effect.", 25, 0, 100, unit = "%%", hide = ::fog neq true)
    val fogRGBPuke by switch("Fog RGB Puke", "Adds an RGB puke effect to the fog.", false, hide = ::fog neq true)
    val fogRGBPukeOpacity by slider("Fog RGB Puke Opacity", "The opacity of the RGB puke effect.", 30, 1, 100, unit = "%%", hide = {
        !fogRGBPuke || !acrylic
    })

    override val shouldHandleEvents: Boolean
        get() = isEnabled && fog

    init {
        on<EventRenderWorld> {
            RenderSystem.disableCull()
            RenderSystem.defaultBlendFunc()
            RenderSystem.enableBlend()
            RenderSystem.depthMask(false)

            val fbo = WrappedFramebuffer.get()

            if (fogRGBPuke) {
                fbo.clear()
                fbo.beginWrite(true)
                ShaderAcrylic.bind()
                RenderSystem.activeTexture(GL13.GL_TEXTURE0)
                MinecraftClient.getInstance().framebuffer.beginRead()
                ShaderAcrylic["uTexture"] = 0
                ShaderAcrylic["uLuminosity"] = 1f
                ShaderAcrylic["uNoiseStrength"] = 0f
                ShaderAcrylic["uNoiseScale"] = 0f
                ShaderAcrylic["uOpacity"] = 1f
                ShaderAcrylic["uRGPuke"] = true
                ShaderAcrylic["uRGPukeOpacity"] = fogRGBPukeOpacity / 100f
                ShaderAcrylic["uTime"] = System.nanoTime() / 2000000000f
                Shader.drawFullScreen()
                ShaderAcrylic.unbind()
            }

            method.switchStrength(fogStrength)
            method.render(if (fogRGBPuke) fbo else mc.framebuffer)

            mc.framebuffer.beginWrite(true)
            ShaderDepth.bind()
            RenderSystem.activeTexture(GL13.GL_TEXTURE0)
            method.getOutput().beginRead()
            ShaderDepth["uScene"] = 0
            RenderSystem.activeTexture(GL13.GL_TEXTURE1)
            GlStateManager._bindTexture(mc.framebuffer.depthAttachment)
            ShaderDepth["uDepth"] = 1
            ShaderDepth["uNear"] = .05f
            ShaderDepth["uFar"] = mc.gameRenderer.farPlaneDistance
            ShaderDepth["uMinThreshold"] = .004f * fogDistance / 100f
            ShaderDepth["uMaxThreshold"] = .28f * fogDistance / 100f
            ShaderDepth["uTime"] = System.nanoTime() / 1000000000f
            Shader.drawFullScreen()
            ShaderDepth.unbind()

            RenderSystem.depthMask(true)
            RenderSystem.enableCull()
        }
        toggle()
    }
}
