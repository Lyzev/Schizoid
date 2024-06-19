/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.opengl

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import org.lwjgl.opengl.GL13.*

object Render {

    var active = -1
    var texture = -1
    var texture0 = -1
    var texture1 = -1

    fun store() {
        active = GlStateManager._getInteger(GL_ACTIVE_TEXTURE)
        texture = GlStateManager._getInteger(GL_TEXTURE_BINDING_2D)
        RenderSystem.activeTexture(GL_TEXTURE0)
        texture0 = GlStateManager._getInteger(GL_TEXTURE_BINDING_2D)
        RenderSystem.activeTexture(GL_TEXTURE1)
        texture1 = GlStateManager._getInteger(GL_TEXTURE_BINDING_2D)
    }

    fun restore() {
        RenderSystem.activeTexture(GL_TEXTURE0)
        GlStateManager._bindTexture(texture0)
        RenderSystem.activeTexture(GL_TEXTURE1)
        GlStateManager._bindTexture(texture1)
        RenderSystem.activeTexture(active)
        GlStateManager._bindTexture(texture)
    }

    var cull = false
    var depth = false
    var blend = false

    fun prepare() {
        cull = GlStateManager._getInteger(GL_CULL_FACE_MODE) != GL_NONE
        depth = GlStateManager._getInteger(GL_DEPTH_FUNC) != GL_ALWAYS
        blend = GlStateManager._getInteger(GL_BLEND_SRC) != GL_ONE || GlStateManager._getInteger(GL_BLEND_DST) != GL_ZERO
        RenderSystem.disableCull()
        RenderSystem.disableDepthTest()
        RenderSystem.defaultBlendFunc()
        RenderSystem.enableBlend()
    }

    fun post() {
        if (cull) RenderSystem.enableCull()
        else RenderSystem.disableCull()
        if (depth) RenderSystem.enableDepthTest()
        else RenderSystem.disableDepthTest()
        if (blend) RenderSystem.enableBlend()
        else RenderSystem.disableBlend()
    }
}
