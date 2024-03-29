/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.opengl.shader.blur.blurs

import com.mojang.blaze3d.platform.GlConst
import com.mojang.blaze3d.systems.RenderSystem
import dev.lyzev.api.opengl.shader.Shader
import dev.lyzev.api.opengl.shader.ShaderKawase
import dev.lyzev.api.opengl.shader.blur.Blur
import dev.lyzev.api.opengl.WrappedFramebuffer
import net.minecraft.client.gl.Framebuffer
import org.joml.Vector2f
import kotlin.properties.Delegates

/**
 * Object that represents a Gaussian blur.
 * This blur uses multiple passes to achieve a blur effect.
 */
object BlurKawase : Blur {

    /**
     * Integer representing the strength of the blur.
     */
    private var strength by Delegates.notNull<Int>()

    /**
     * Array of framebuffers used for the blur process.
     * Each framebuffer is wrapped in a WrappedFramebuffer object.
     */
    private val fbos = Array(2) {
        WrappedFramebuffer(.25f)
    }

    /**
     * Vector representing the size of a texel.
     */
    private val texelSize = Vector2f()

    override fun switchStrength(strength: Int) {
        this.strength = strength
    }

    /**
     * Method to render to a framebuffer object.
     * @param targetFBO The framebuffer to render to.
     * @param sourceFBO The framebuffer to render from.
     * @param size The size of the blur.
     * @param alpha Whether to use alpha blending.
     */
    private fun renderToFBO(targetFBO: Framebuffer, sourceFBO: Framebuffer, size: Float, alpha: Boolean) {
        targetFBO.beginWrite(true)
        ShaderKawase.bind()
        RenderSystem.activeTexture(GlConst.GL_TEXTURE0)
        sourceFBO.beginRead()
        ShaderKawase.setUniforms(texelSize, size, alpha)
        Shader.drawFullScreen()
        ShaderKawase.unbind()
    }

    override fun render(sourceFBO: Framebuffer, alpha: Boolean) {
        fbos.forEach { it.clear() }
        texelSize.set(1f / sourceFBO.textureWidth, 1f / sourceFBO.textureHeight)
        // Initial iteration
        renderToFBO(fbos[0], sourceFBO, if (strength > 1) .5f else .25f, alpha)
        // Rest of the iterations
        for (i in 2..strength) renderToFBO(fbos[(i - 1) % 2], fbos[i % 2], i * .5f, alpha)
    }

    override val output: WrappedFramebuffer
        get() = fbos[(strength - 1) % 2]
}
