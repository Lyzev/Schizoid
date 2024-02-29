/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.opengl.shader.blur.blurs

import com.mojang.blaze3d.platform.GlConst
import com.mojang.blaze3d.systems.RenderSystem
import dev.lyzev.api.opengl.shader.Shader
import dev.lyzev.api.opengl.shader.ShaderBox
import dev.lyzev.api.opengl.shader.blur.Blur
import dev.lyzev.schizoid.util.render.WrappedFramebuffer
import net.minecraft.client.gl.Framebuffer
import org.joml.Vector2f
import kotlin.math.ceil
import kotlin.math.sqrt
import kotlin.properties.Delegates

object BlurBox : Blur {

    private var strength by Delegates.notNull<Int>()

    private val direction = Vector2f()
    private val texelSize = Vector2f()

    override fun switchStrength(strength: Int) {
        this.strength = ceil(strength * sqrt(3.0)).toInt()
    }

    private fun renderToFBO(targetFBO: Framebuffer, sourceFBO: Framebuffer, alpha: Boolean) {
        targetFBO.beginWrite(true)
        ShaderBox.bind()
        RenderSystem.activeTexture(GlConst.GL_TEXTURE0)
        sourceFBO.beginRead()
        ShaderBox.setUniforms(direction, texelSize, alpha, strength)
        Shader.drawFullScreen()
        ShaderBox.unbind()
    }

    override fun render(sourceFBO: Framebuffer, alpha: Boolean) {
        val fbos = WrappedFramebuffer[.25f, 2]
        texelSize.set(1f / sourceFBO.textureWidth, 1f / sourceFBO.textureHeight)
        // Initial iteration
        direction.set(1f, 0f)
        renderToFBO(fbos[0], sourceFBO, alpha)
        // Rest of the iterations
        for (i in 1 until 4) {
            direction.set((i - 1) % 2f, i % 2f)
            renderToFBO(fbos[i % 2], fbos[(i - 1) % 2], alpha)
        }
    }

    override fun getOutput(): WrappedFramebuffer = WrappedFramebuffer[.25f, 2][1]
}
