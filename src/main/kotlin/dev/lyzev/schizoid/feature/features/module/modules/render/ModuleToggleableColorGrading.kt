/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.render

import com.mojang.blaze3d.systems.RenderSystem
import dev.lyzev.api.events.EventListener
import dev.lyzev.api.events.EventRenderWorld
import dev.lyzev.api.events.on
import dev.lyzev.api.opengl.Render
import dev.lyzev.api.opengl.shader.Shader
import dev.lyzev.api.opengl.shader.ShaderColorGrading
import dev.lyzev.api.opengl.shader.blur.BlurHelper.mode
import dev.lyzev.api.setting.settings.color
import dev.lyzev.api.setting.settings.slider
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.ModuleToggleable
import org.lwjgl.opengl.GL13.GL_TEXTURE0
import java.awt.Color

object ModuleToggleableColorGrading : ModuleToggleable("Color Grading", "Enables color grading for the world.", category = IFeature.Category.RENDER), EventListener {

    val brightness by slider("Brightness", "The brightness of the color grading effect.", -0.1f, -1f, 1f, 2)
    val contrast by slider("Contrast", "The contrast of the color grading effect.", .95f, 0f, 2f, 2)
    val exposure by slider("Exposure", "The exposure of the color grading effect.", -0.45f, -1f, 1f, 2)
    val saturation by slider("Saturation", "The saturation of the color grading effect.", 1.2f, 0f, 2f, 2)
    val temperature by slider("Temperature", "The temperature of the color grading effect.", 1000, 1000, 40000, "K")
    val lift by color("Lift", value = Color(0, 0, 0))
    val gamma by color("Gamma", value = Color(0,2,34))
    val gain by color("Gain", value = Color(11,0,255))
    val offset by color("Offset", value = Color(0,0,17))

    override val shouldHandleEvents: Boolean
        get() = isEnabled && !mc.gameRenderer.isRenderingPanorama

    init {
        on<EventRenderWorld> {
            Render.store()
            RenderSystem.disableCull()
            RenderSystem.disableDepthTest()
            RenderSystem.defaultBlendFunc()
            RenderSystem.enableBlend()
            ShaderColorGrading.bind()
            RenderSystem.activeTexture(GL_TEXTURE0)
            mc.framebuffer.beginRead()
            ShaderColorGrading["Tex0"] = 0
            ShaderColorGrading["Brightness"] = brightness
            ShaderColorGrading["Contrast"] = contrast
            ShaderColorGrading["Exposure"] = exposure
            ShaderColorGrading["Saturation"] = saturation
            ShaderColorGrading["Temperature"] = temperature.toFloat()
            ShaderColorGrading["Lift"] = lift
            ShaderColorGrading["Gamma"] = gamma
            ShaderColorGrading["Gain"] = gain
            ShaderColorGrading["Offset"] = offset
            Shader.drawFullScreen()
            ShaderColorGrading.unbind()
            Render.restore()
        }
    }
}
