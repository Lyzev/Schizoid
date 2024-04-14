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
}
