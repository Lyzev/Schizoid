/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.opengl.shader.blur

import dev.lyzev.schizoid.Schizoid.mc
import dev.lyzev.schizoid.util.render.WrappedFramebuffer
import net.minecraft.client.gl.Framebuffer

interface Blur {

    fun switchStrength(strength: Int)

    fun render(sourceFBO: Framebuffer = mc.framebuffer, alpha: Boolean = false)

    fun getOutput(): WrappedFramebuffer
}
