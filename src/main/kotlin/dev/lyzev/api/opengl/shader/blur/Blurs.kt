/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.opengl.shader.blur

import dev.lyzev.api.opengl.shader.blur.blurs.BlurBox
import dev.lyzev.api.opengl.shader.blur.blurs.BlurDualKawase
import dev.lyzev.api.opengl.shader.blur.blurs.BlurGaussian
import dev.lyzev.api.opengl.shader.blur.blurs.BlurKawase
import dev.lyzev.api.setting.settings.OptionEnum
import dev.lyzev.schizoid.util.render.WrappedFramebuffer
import net.minecraft.client.gl.Framebuffer

enum class Blurs(override val key: String, val blur: Blur) : OptionEnum, Blur {
    DUAL_KAWASE("Dual Kawase", BlurDualKawase),
    KAWASE("Kawase", BlurKawase),
    BOX("Box", BlurBox),
    GAUSSIAN("Gaussian", BlurGaussian);

    override fun switchStrength(strength: Int) = blur.switchStrength(strength)

    override fun render(sourceFBO: Framebuffer, alpha: Boolean) = blur.render(sourceFBO, alpha)

    override fun getOutput(): WrappedFramebuffer = blur.getOutput()

    init {
        switchStrength(6)
    }
}
