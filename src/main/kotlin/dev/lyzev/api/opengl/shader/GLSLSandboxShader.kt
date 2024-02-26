/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.opengl.shader

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import org.joml.Vector2f

abstract class GLSLSandboxShader(shader: String) : Shader(shader) {

    private val initTime = System.currentTimeMillis()
    private val resolution = Vector2f()

    open fun setUniforms() {}

    fun draw() {
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        bind()
        this["uTime"] = (System.currentTimeMillis() - initTime) / 1000f
        this["uResolution"] = resolution.set(
            MinecraftClient.getInstance().framebuffer.textureWidth.toFloat(),
            MinecraftClient.getInstance().framebuffer.textureHeight.toFloat()
        )
        setUniforms()
        drawFullScreen()
        unbind()
        RenderSystem.enableCull()
    }
}
