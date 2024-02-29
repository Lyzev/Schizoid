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
import dev.lyzev.schizoid.util.render.WrappedFramebuffer
import net.minecraft.client.gl.Framebuffer
import org.joml.Vector2f
import kotlin.properties.Delegates

object BlurKawase : Blur {

    private var strength by Delegates.notNull<Int>()

    private val texelSize = Vector2f()

    override fun switchStrength(strength: Int) {
        this.strength = strength
    }

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
        val fbos = WrappedFramebuffer[.25f, 2]
        texelSize.set(1f / sourceFBO.textureWidth, 1f / sourceFBO.textureHeight)
        // Initial iteration
        renderToFBO(fbos[0], sourceFBO, if (strength > 1) .5f else .25f, alpha)
        // Rest of the iterations
        for (i in 2..strength) renderToFBO(fbos[(i - 1) % 2], fbos[i % 2], i * .5f, alpha)
    }

    override fun getOutput(): WrappedFramebuffer = WrappedFramebuffer[.25f, 2][(strength - 1) % 2]
}
