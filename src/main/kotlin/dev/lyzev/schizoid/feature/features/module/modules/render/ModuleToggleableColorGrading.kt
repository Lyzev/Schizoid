/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.render

import com.mojang.blaze3d.systems.RenderSystem
import dev.lyzev.api.events.EventListener
import dev.lyzev.api.events.EventRenderWorld
import dev.lyzev.api.events.on
import dev.lyzev.api.opengl.WrappedFramebuffer
import dev.lyzev.api.opengl.clear
import dev.lyzev.api.opengl.shader.Shader
import dev.lyzev.api.opengl.shader.ShaderColorGrading
import dev.lyzev.api.opengl.shader.ShaderPassThrough
import dev.lyzev.api.setting.settings.color
import dev.lyzev.api.setting.settings.slider
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.ModuleToggleable
import org.lwjgl.opengl.GL13.GL_TEXTURE0
import java.awt.Color

object ModuleToggleableColorGrading :
    ModuleToggleable("Color Grading", "Enables color grading for the world.", category = IFeature.Category.RENDER),
    EventListener {

    private val fbo by lazy { WrappedFramebuffer() }

    val brightness by slider("Brightness", "The brightness of the color grading effect.", -50, -100, 100, "%%")
    val contrast by slider("Contrast", "The contrast of the color grading effect.", 55, 0, 200, "%%")
    val exposure by slider("Exposure", "The exposure of the color grading effect.", -0, -100, 100, "%%")
    val saturation by slider("Saturation", "The saturation of the color grading effect.", 110, 0, 200, "%%")
    val hue by slider("Hue", "The hue shift of the color grading effect.", 0, -180, 180, "°")
    val temperature by slider("Temperature", "The temperature of the color grading effect.", 1000, 1000, 40000, "K")
    val lift by color("Lift", value = Color(0, 0, 0))
    val gamma by color("Gamma", value = Color(0, 35, 0))
    val gain by color("Gain", value = Color(0, 0, 255))
    val offset by color("Offset", value = Color(0, 0, 0))

    override val shouldHandleEvents: Boolean
        get() = isEnabled && !mc.gameRenderer.isRenderingPanorama

    init {
        on<EventRenderWorld> {
            fbo.clear()
            fbo.beginWrite(false)
            ShaderColorGrading.bind()
            RenderSystem.activeTexture(GL_TEXTURE0)
            mc.framebuffer.beginRead()
            ShaderColorGrading["Tex0"] = 0
            ShaderColorGrading["Brightness"] = brightness / 100f
            ShaderColorGrading["Contrast"] = contrast / 100f
            ShaderColorGrading["Exposure"] = exposure / 100f
            ShaderColorGrading["Saturation"] = saturation / 100f
            ShaderColorGrading["Hue"] = hue
            ShaderColorGrading["Temperature"] = temperature.toFloat()
            ShaderColorGrading["Lift"] = lift
            ShaderColorGrading["Gamma"] = gamma
            ShaderColorGrading["Gain"] = gain
            ShaderColorGrading["Offset"] = offset
            Shader.drawFullScreen()
            ShaderColorGrading.unbind()

            mc.framebuffer.beginWrite(false)
            ShaderPassThrough.bind()
            RenderSystem.activeTexture(GL_TEXTURE0)
            fbo.beginRead()
            ShaderPassThrough["Tex0"] = 0
            ShaderPassThrough["Scale"] = 1f
            ShaderPassThrough["Alpha"] = true
            Shader.drawFullScreen()
            ShaderPassThrough.unbind()
        }
    }
}
