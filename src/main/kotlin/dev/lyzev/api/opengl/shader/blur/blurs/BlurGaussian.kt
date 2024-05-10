/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.opengl.shader.blur.blurs

import com.mojang.blaze3d.platform.GlConst
import com.mojang.blaze3d.systems.RenderSystem
import dev.lyzev.api.opengl.WrappedFramebuffer
import dev.lyzev.api.opengl.clear
import dev.lyzev.api.opengl.shader.Shader
import dev.lyzev.api.opengl.shader.ShaderGaussian
import dev.lyzev.api.opengl.shader.blur.Blur
import dev.lyzev.schizoid.feature.features.module.modules.render.ModuleToggleableBlur
import net.minecraft.client.gl.Framebuffer
import org.joml.Vector2f
import org.joml.Vector3f
import kotlin.math.exp
import kotlin.properties.Delegates

/**
 * Object that represents a Gaussian blur.
 * This blur uses a two-pass approach, first horizontally and then vertically.
 */
object BlurGaussian : Blur {

    /**
     * Constant representing the square root of 2 times PI.
     */
    private const val sqrt2PI = 2.50662f

    /**
     * Float representing the size of the blur.
     */
    private var support by Delegates.notNull<Int>()

    /**
     * Array of framebuffers used for the blur process.
     * Each framebuffer is wrapped in a WrappedFramebuffer object.
     */
    private val fbos = Array(2) {
        WrappedFramebuffer(2)
    }

    /**
     * Vector representing the direction of the blur.
     */
    private val direction = Vector2f()

    /**
     * Vector representing the size of a texel.
     */
    private val texelSize = Vector2f()

    /**
     * Vector representing the Gaussian blur parameters.
     */
    private val gaussian = Vector3f()

    /**
     * This array stores precomputed values for the "Incremental Computation of the Gaussian" method by Ken Turkowski.
     * Each element in the array represents a Gaussian blur parameter for a specific sigma value, which is used to control the blur strength.
     * The parameters are precomputed for efficiency and are used in the 'switchStrength' method to set the Gaussian blur parameters based on the blur strength.
     * For a detailed understanding of the computation and its usage, refer to the [paper](https://developer.nvidia.com/gpugems/gpugems3/part-vi-gpu-computing/chapter-40-incremental-computation-gaussian).
     */
    private val cache = Array(20) {
        val sigma = it + 1
        val y = exp(-.5f/ (sigma * sigma))
        // vec3 gaussian = vec3(1 / (sqrt(2 * PI) * sigma), exp(-.5 / (sigma * sigma)), gaussian.y * gaussian.y)
        Vector3f(1 / (sqrt2PI * sigma), y, y * y)
    }

    override fun switchStrength(strength: Int) {
        gaussian.set(cache[strength - 1])
        support = strength * 3
    }

    /**
     * Method to render to a framebuffer object.
     * @param targetFBO The framebuffer to render to.
     * @param sourceFBO The framebuffer to render from.
     * @param alpha Whether to use alpha blending.
     */
    private fun renderToFBO(targetFBO: Framebuffer, sourceFBO: Framebuffer, alpha: Boolean) {
        targetFBO.clear()
        targetFBO.beginWrite(true)
        ShaderGaussian.bind()
        RenderSystem.activeTexture(GlConst.GL_TEXTURE0)
        sourceFBO.beginRead()
        ShaderGaussian.setUniforms(direction, texelSize, alpha, gaussian, support, ModuleToggleableBlur.useLinearSampling)
        Shader.drawFullScreen()
        ShaderGaussian.unbind()
    }


    override fun render(sourceFBO: Framebuffer, alpha: Boolean) {
        texelSize.set(1f / fbos[0].textureWidth, 1f / fbos[0].textureHeight)
        direction.set(1f, 0f)
        renderToFBO(fbos[0], sourceFBO, alpha)
        direction.set(0f, 1f)
        renderToFBO(fbos[1], fbos[0], alpha)
    }

    override val output: WrappedFramebuffer
        get() = fbos[1]
}
