/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.opengl.shader.blur.blurs

import dev.lyzev.api.opengl.WrappedFramebuffer
import dev.lyzev.api.opengl.clear
import dev.lyzev.api.opengl.save
import dev.lyzev.api.opengl.shader.ShaderMovingAveragesBoxH
import dev.lyzev.api.opengl.shader.blur.Blur
import dev.lyzev.schizoid.feature.features.module.modules.render.ModuleToggleableInfo
import net.minecraft.client.gl.Framebuffer
import org.joml.Vector2f
import kotlin.math.ceil
import kotlin.math.sqrt
import kotlin.properties.Delegates

/**
 * Object that represents a Box blur.
 * This blur uses a two-pass approach, first horizontally and then vertically.
 */
object BlurMovingAveragesBox : Blur {

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

    override fun switchStrength(strength: Int) {
        this.strength = strength
    }

    /**
     * Method to render to a framebuffer object.
     * @param targetFBO The framebuffer to render to.
     * @param sourceFBO The framebuffer to render from.
     * @param alpha Whether to use alpha blending.
     */
    private fun renderToFBO(targetFBO: Framebuffer, sourceTex: Int, direction: Boolean, alpha: Boolean) {
        ShaderMovingAveragesBoxH.render(targetFBO, sourceTex, direction, alpha, strength)
    }

    override fun render(sourceTex: Int, alpha: Boolean) {
        // Initial iteration
        renderToFBO(fbos[0], sourceTex, false, alpha)
        // Rest of the iterations
        for (i in 1 until 4)
            renderToFBO(fbos[i % 2], fbos[(i + 1) % 2].colorAttachment, i % 2 == 1, alpha)
    }

    override val output: WrappedFramebuffer
        get() = fbos[1]
}
