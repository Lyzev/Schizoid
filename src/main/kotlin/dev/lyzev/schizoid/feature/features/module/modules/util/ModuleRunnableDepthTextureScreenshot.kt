/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.util

import com.mojang.blaze3d.systems.RenderSystem
import dev.lyzev.api.events.Event
import dev.lyzev.api.events.EventListener
import dev.lyzev.api.events.EventRenderWorld
import dev.lyzev.api.events.on
import dev.lyzev.api.opengl.Render
import dev.lyzev.api.opengl.WrappedFramebuffer
import dev.lyzev.api.opengl.clear
import dev.lyzev.api.opengl.save
import dev.lyzev.api.opengl.shader.Shader
import dev.lyzev.api.opengl.shader.ShaderLinearizeDepth
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.ModuleRunnable
import net.minecraft.client.render.GameRenderer
import org.lwjgl.opengl.GL13.GL_TEXTURE0

object ModuleRunnableDepthTextureScreenshot : ModuleRunnable(
    "Depth Texture Screenshot", "Takes a screenshot of the depth texture.", category = IFeature.Category.UTIL
), EventListener {

    private val fbo = WrappedFramebuffer()
    private var isTakingScreenshot = false

    override fun invoke(): String? {
        if (isTakingScreenshot) {
            return "Already taking a screenshot."
        } else
            isTakingScreenshot = true
        return null
    }

    override val shouldHandleEvents: Boolean
        get() = isTakingScreenshot

    init {
        on<EventRenderWorld>(Event.Priority.HIGHEST) {
            isTakingScreenshot = false
            Render.store()
            RenderSystem.disableCull()
            RenderSystem.defaultBlendFunc()
            RenderSystem.enableBlend()

            fbo.clear()
            fbo.beginWrite(true)

            ShaderLinearizeDepth.bind()
            RenderSystem.activeTexture(GL_TEXTURE0)
            RenderSystem.bindTexture(mc.framebuffer.depthAttachment)
            ShaderLinearizeDepth["Tex0"] = 0
            ShaderLinearizeDepth["Near"] = GameRenderer.CAMERA_DEPTH
            ShaderLinearizeDepth["Far"] = mc.gameRenderer.farPlaneDistance
            Shader.drawFullScreen()
            ShaderLinearizeDepth.unbind()

            RenderSystem.enableCull()
            Render.restore()

            fbo.save()
        }
    }
}
