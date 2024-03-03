/*
 * Copyright (c) 2023-2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.gui

import com.mojang.blaze3d.systems.RenderSystem
import dev.lyzev.api.events.*
import dev.lyzev.api.opengl.shader.Shader
import dev.lyzev.api.opengl.shader.ShaderAdd
import dev.lyzev.api.opengl.shader.ShaderPassThrough
import dev.lyzev.api.opengl.shader.blur.BlurHelper
import dev.lyzev.api.setting.settings.option
import dev.lyzev.api.setting.settings.slider
import dev.lyzev.schizoid.Schizoid.mc
import dev.lyzev.schizoid.feature.FeatureManager
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.modules.render.ModuleToggleableBlur
import dev.lyzev.schizoid.util.render.WrappedFramebuffer
import dev.lyzev.schizoid.util.render.save
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import org.joml.Vector4f
import org.lwjgl.opengl.GL13
import su.mandora.tarasande.util.render.animation.EasingFunction
import su.mandora.tarasande.util.render.animation.TimeAnimator
import kotlin.math.ceil
import kotlin.math.floor

abstract class ImGuiScreen(title: String) : Screen(Text.of(title)), IFeature, EventListener {

    private val fbo by lazy { WrappedFramebuffer() }
    private val mask by lazy { WrappedFramebuffer() }

    abstract fun renderImGui()

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        super<Screen>.render(context, mouseX, mouseY, delta)
        EventRenderImGuiScreen(this).fire()
    }

    override fun sendChatMessage(message: Text) = FeatureManager.sendChatMessage(message)

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
            fbo.endWrite()

            RenderSystem.disableCull()
            RenderSystem.defaultBlendFunc()
            RenderSystem.enableBlend()

            BlurHelper.draw(fbo, clearMask = false)

            RenderSystem.disableCull()
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
            RenderSystem.enableCull()
        }
    }
}
