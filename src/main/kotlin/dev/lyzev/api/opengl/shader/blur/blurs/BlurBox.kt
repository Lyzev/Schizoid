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
import dev.lyzev.api.opengl.shader.ShaderBox
import dev.lyzev.api.opengl.shader.blur.Blur
import net.minecraft.client.gl.Framebuffer
import org.joml.Vector2f
import kotlin.math.ceil
import kotlin.math.sqrt
import kotlin.properties.Delegates

/**
 * Object that represents a Box blur.
 * This blur uses a two-pass approach, first horizontally and then vertically.
 */
object BlurBox : Blur {

    /**
     * Integer representing the strength of the blur.
     */
    private var strength by Delegates.notNull<Int>()

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

    override fun switchStrength(strength: Int) {
        this.strength = ceil(strength * sqrt(3.0)).toInt()
    }

    /**
     * Method to render to a framebuffer object.
     * @param targetFBO The framebuffer to render to.
     * @param sourceFBO The framebuffer to render from.
     * @param alpha Whether to use alpha blending.
     */
    private fun renderToFBO(targetFBO: Framebuffer, sourceTex: Int, alpha: Boolean) {
        targetFBO.clear()
        targetFBO.beginWrite(true)
        ShaderBox.bind()
        RenderSystem.activeTexture(GlConst.GL_TEXTURE0)
        RenderSystem.bindTexture(sourceTex)
        ShaderBox.setUniforms(
            direction,
            texelSize.set(1f / targetFBO.textureWidth, 1f / targetFBO.textureHeight),
            alpha,
            strength
        )
        Shader.drawFullScreen()
        ShaderBox.unbind()
    }

    override fun render(sourceTex: Int, alpha: Boolean) {
        // Initial iteration
        direction.set(1f, 0f)
        renderToFBO(fbos[0], sourceTex, alpha)
        // Rest of the iterations
        for (i in 1 until 4) {
            direction.set((i - 1) % 2f, i % 2f)
            renderToFBO(fbos[i % 2], fbos[(i - 1) % 2].colorAttachment, alpha)
        }
    }

    override val output: WrappedFramebuffer
        get() = fbos[1]
}
