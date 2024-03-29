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
import dev.lyzev.api.opengl.WrappedFramebuffer
import net.minecraft.client.gl.Framebuffer

/**
 * Enum class representing different types of blurs.
 * Each enum constant is associated with a specific implementation of the Blur interface.
 *
 * @property key The unique identifier for the blur type.
 * @property blur The specific implementation of the Blur interface associated with the enum constant.
 */
enum class Blurs(override val key: String, val blur: Blur) : OptionEnum, Blur {
    DUAL_KAWASE("Dual Kawase", BlurDualKawase),
    KAWASE("Kawase", BlurKawase),
    BOX("Box", BlurBox),
    GAUSSIAN("Gaussian", BlurGaussian);

    override fun switchStrength(strength: Int) = blur.switchStrength(strength)

    override fun render(sourceFBO: Framebuffer, alpha: Boolean) = blur.render(sourceFBO, alpha)

    override val output: WrappedFramebuffer
        get() = blur.output
}
