/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.opengl.shader

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import org.joml.Vector2f

/**
 * Abstract class for GLSL Sandbox Shaders.
 * @param shader The shader to be used.
 */
class GLSLSandboxShader(shader: String) : ShaderVertexFragment(shader) {

    /**
     * The time the shader was initialized.
     */
    private val initTime = System.currentTimeMillis()

    /**
     * The resolution of the shader.
     */
    private val resolution = Vector2f()

    /**
     * Method to draw the shader.
     * Disables culling, enables blending, binds the shader, sets the uniforms, draws fullscreen, unbinds the shader, and enables culling.
     */
    fun draw(setUniforms: Shader.() -> Unit = { }) {
        RenderSystem.disableCull()
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        bind()
        // Set the time since the shader was initialized.
        this["time"] = (System.currentTimeMillis() - initTime) / 1000f
        // Set the resolution to the current framebuffer's texture width and height.
        this["resolution"] = resolution.set(
            MinecraftClient.getInstance().framebuffer.textureWidth.toFloat(),
            MinecraftClient.getInstance().framebuffer.textureHeight.toFloat()
        )
        setUniforms()
        drawFullScreen()
        unbind()
        RenderSystem.enableCull()
    }
}
