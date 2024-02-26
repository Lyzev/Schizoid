/*
 * Copyright (c) 2023-2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.gui

import com.mojang.blaze3d.systems.RenderSystem
import dev.lyzev.api.events.*
import dev.lyzev.api.opengl.shader.ShaderAdd
import dev.lyzev.api.opengl.shader.ShaderPassThrough
import dev.lyzev.api.opengl.shader.Shader
import dev.lyzev.api.opengl.shader.blur.BlurHelper
import dev.lyzev.schizoid.Schizoid.mc
import dev.lyzev.schizoid.feature.FeatureManager
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.modules.render.ModuleToggleableBlur
import dev.lyzev.schizoid.util.render.WrappedFramebuffer
import net.kyori.adventure.text.Component
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import org.lwjgl.opengl.GL13

abstract class ImGuiScreen(title: String) : Screen(Text.of(title)), IFeature, EventListener {

//    private var initTime = System.currentTimeMillis()
//    private val FADE_TIME = 300
    private val fbo by lazy { WrappedFramebuffer() }
    private val mask by lazy { WrappedFramebuffer() }

    abstract fun renderImGui()

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        super<Screen>.render(context, mouseX, mouseY, delta)
        EventRenderImGuiScreen(this).fire()
    }

    override fun sendChatMessage(message: Component) = FeatureManager.sendChatMessage(message)

    override fun keybindReleased() {
        if (MinecraftClient.getInstance().currentScreen == this) close()
        else MinecraftClient.getInstance().setScreen(this)
    }

    override val shouldHandleEvents: Boolean
        get() = mc.currentScreen == null || mc.currentScreen == this

    init {
        on<EventPreRenderImGui> {
            if (!ModuleToggleableBlur.isEnabled) return@on
            fbo.clear()
            fbo.beginWrite(true)
        }

        on<EventPostRenderImGui> {
            if (!ModuleToggleableBlur.isEnabled) return@on
//            val deltaTime = System.currentTimeMillis() - initTime
//            val shouldFade = deltaTime < FADE_TIME
//            if (BlurModule.isEnabled) {
                fbo.endWrite()

                RenderSystem.disableCull()
//                RenderSystem.enableTexture()
                RenderSystem.defaultBlendFunc()
                RenderSystem.enableBlend()

                mask.beginWrite(true)
                ShaderAdd.bind()
                RenderSystem.activeTexture(GL13.GL_TEXTURE1)
                fbo.beginRead()
                RenderSystem.activeTexture(GL13.GL_TEXTURE0)
                mask.beginRead()
                ShaderAdd["u_s2Scene"] = 0
                ShaderAdd["u_s2Texture"] = 1
                ShaderAdd["u_bAlpha"] = false
                Shader.drawFullScreen()
                ShaderAdd.unbind()

                BlurHelper.draw(mask)

                RenderSystem.disableCull()
//                RenderSystem.enableTexture()
                RenderSystem.defaultBlendFunc()
                RenderSystem.enableBlend()

                MinecraftClient.getInstance().framebuffer.beginWrite(true)
                ShaderPassThrough.bind()
                RenderSystem.activeTexture(GL13.GL_TEXTURE0)
                fbo.beginRead()
                ShaderPassThrough["uTexture"] = 0
                ShaderPassThrough["uScale"] = 1f
                Shader.drawFullScreen()
                ShaderPassThrough.unbind()
//                if (shouldFade) {
//                    val strength = ceil((1 - (deltaTime / FADE_TIME.toFloat())) * 20).toInt()
//                    KawaseBlur.switchStrength(strength)
//                    KawaseBlur.render(fbo, true)
//
//                    MinecraftClient.getInstance().framebuffer.beginWrite(true)
//                    PassThroughShader.bind()
//                    RenderSystem.activeTexture(GL13.GL_TEXTURE0)
//                    KawaseBlur.getOutput().beginRead()
//                    PassThroughShader["uTexture"] = 0
//                    PassThroughShader["uScale"] = deltaTime / FADE_TIME.toFloat()
//                    Shader.drawFullScreen()
//                    PassThroughShader.unbind()
//                } else {
//                    mask.beginWrite(true)
//                    AddShader.bind()
//                    RenderSystem.activeTexture(GL13.GL_TEXTURE1)
//                    fbo.beginRead()
//                    RenderSystem.activeTexture(GL13.GL_TEXTURE0)
//                    mask.beginRead()
//                    AddShader["u_s2Scene"] = 0
//                    AddShader["u_s2Texture"] = 1
//                    AddShader["u_bAlpha"] = false
//                    Shader.drawFullScreen()
//                    AddShader.unbind()
//
//                    BlurHelper.draw(mask)
//
//                    RenderSystem.disableCull()
//                    RenderSystem.enableTexture()
//                    RenderSystem.defaultBlendFunc()
//                    RenderSystem.enableBlend()
//
//                    MinecraftClient.getInstance().framebuffer.beginWrite(true)
//                    PassThroughShader.bind()
//                    RenderSystem.activeTexture(GL13.GL_TEXTURE0)
//                    fbo.beginRead()
//                    PassThroughShader["uTexture"] = 0
//                    PassThroughShader["uScale"] = 1f
//                    Shader.drawFullScreen()
//                    PassThroughShader.unbind()
//                }
//            }

            RenderSystem.enableCull()
        }
    }
}
