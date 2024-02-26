/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.opengl.shader.blur.blurs

import com.mojang.blaze3d.platform.GlConst
import com.mojang.blaze3d.systems.RenderSystem
import dev.lyzev.api.opengl.shader.ShaderDualKawaseDown
import dev.lyzev.api.opengl.shader.ShaderDualKawase
import dev.lyzev.api.opengl.shader.ShaderDualKawaseUp
import dev.lyzev.api.opengl.shader.Shader
import dev.lyzev.api.opengl.shader.blur.Blur
import dev.lyzev.schizoid.util.render.WrappedFramebuffer
import net.minecraft.client.gl.Framebuffer
import org.joml.Vector2f
import kotlin.math.pow

object BlurDualKawase : Blur {

    // Strength-Levels from https://github.com/jonaburg/picom/blob/a8445684fe18946604848efb73ace9457b29bf80/src/backend/backend_common.c#L372
    private val strengths = arrayOf(
        1 to 1.25f,  // LVL  1
        1 to 2.25f,  // LVL  2
        2 to 2f,     // LVL  3
        2 to 3f,     // LVL  4
        2 to 4.25f,  // LVL  5
        3 to 2.5f,   // LVL  6
        3 to 3.25f,  // LVL  7
        3 to 4.25f,  // LVL  8
        3 to 5.5f,   // LVL  9
        4 to 3.25f,  // LVL  10
        4 to 4f,     // LVL  11
        4 to 5f,     // LVL  12
        4 to 6f,     // LVL  13
        4 to 7.25f,  // LVL  14
        4 to 8.25f,  // LVL  15
        5 to 4.5f,   // LVL  16
        5 to 5.25f,  // LVL  17
        5 to 6.25f,  // LVL  18
        5 to 7.25f,  // LVL  19
        5 to 8.5f,   // LVL  20
    )

    private lateinit var strength: Pair<Int, Float>

    private val FBOs = Array(6) { WrappedFramebuffer(1f / 2f.pow(it)) }


    private val halfTexelSize = Vector2f()

    override fun switchStrength(strength: Int) {
        this.strength = strengths[strength - 1]
    }

    private fun renderToFBO(
        targetFBO: Framebuffer, sourceFBO: Framebuffer, shader: ShaderDualKawase, alpha: Boolean
    ) {
        targetFBO.beginWrite(true)
        shader.bind()
        RenderSystem.activeTexture(GlConst.GL_TEXTURE0)
        sourceFBO.beginRead()
        shader.setUniforms(
            strength.second, halfTexelSize.set(.5f / targetFBO.textureWidth, .5f / targetFBO.textureHeight), alpha
        )
        Shader.drawFullScreen()
        shader.unbind()
    }

    override fun render(sourceFBO: Framebuffer, alpha: Boolean) {
        // Initial downsample
        renderToFBO(FBOs[1], sourceFBO, ShaderDualKawaseDown, alpha)
        // Downsample
        for (i in 1 until strength.first) renderToFBO(FBOs[i + 1], FBOs[i], ShaderDualKawaseDown, alpha)
        // Upsample
        for (i in strength.first downTo 1) renderToFBO(FBOs[i - 1], FBOs[i], ShaderDualKawaseUp, alpha)
    }

    override fun getOutput(): WrappedFramebuffer = FBOs[0]
}
