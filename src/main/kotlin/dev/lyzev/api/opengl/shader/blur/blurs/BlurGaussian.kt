/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.opengl.shader.blur.blurs

import com.mojang.blaze3d.platform.GlConst
import com.mojang.blaze3d.systems.RenderSystem
import dev.lyzev.api.opengl.shader.ShaderGaussian
import dev.lyzev.api.opengl.shader.Shader
import dev.lyzev.api.opengl.shader.blur.Blur
import dev.lyzev.schizoid.util.render.WrappedFramebuffer
import net.minecraft.client.gl.Framebuffer
import org.joml.Vector2f
import org.joml.Vector3f
import kotlin.math.exp
import kotlin.properties.Delegates

object BlurGaussian : Blur {

    private val sqrt2PI = 2.50662f

    private var size by Delegates.notNull<Float>()

    private val FBOs = Array(2) {
        WrappedFramebuffer(.25f)
    }

    private val direction = Vector2f()
    private val texelSize = Vector2f()
    private val gaussian = Vector3f()

    override fun switchStrength(strength: Int) {
        // vec3 gaussian = vec3(1.0 / (sqrt(2.0 * PI) * sigma), exp(-0.5 * delta * delta / (sigma * sigma)), gaussian.y * gaussian.y)
        val delta = 1f
        val gaussianY = exp(-.5f * delta * delta / (strength * strength))
        gaussian.set(1 / (sqrt2PI * strength), gaussianY, gaussianY * gaussianY)
        this.size = strength * 3f
    }

    private fun renderToFBO(targetFBO: Framebuffer, sourceFBO: Framebuffer, alpha: Boolean) {
        targetFBO.beginWrite(true)
        ShaderGaussian.bind()
        RenderSystem.activeTexture(GlConst.GL_TEXTURE0)
        sourceFBO.beginRead()
        ShaderGaussian.setUniforms(direction, texelSize, alpha, gaussian, size, 1f)
        Shader.drawFullScreen()
        ShaderGaussian.unbind()
    }


    override fun render(sourceFBO: Framebuffer, alpha: Boolean) {
        texelSize.set(1f / FBOs[0].textureWidth, 1f / FBOs[0].textureHeight)
        direction.set(1f, 0f)
        renderToFBO(FBOs[0], sourceFBO, alpha)
        direction.set(0f, 1f)
        renderToFBO(FBOs[1], FBOs[0], alpha)
    }

    override fun getOutput(): WrappedFramebuffer = FBOs[1]
}
