/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.opengl.shader.blur

import dev.lyzev.schizoid.Schizoid.mc
import dev.lyzev.api.opengl.WrappedFramebuffer
import net.minecraft.client.gl.Framebuffer

interface Blur {

    /**
     * Method to switch the strength of the blur.
     * @param strength The new strength level to set.
     */
    fun switchStrength(strength: Int)

    /**
     * Method to render the blur.
     * @param sourceFBO The framebuffer to render from.
     * @param alpha Whether to use alpha blending.
     */
    fun render(sourceFBO: Framebuffer = mc.framebuffer, alpha: Boolean = false)

    /**
     * Getter for the output framebuffer.
     * @return The output framebuffer.
     */
    val output: WrappedFramebuffer
}
