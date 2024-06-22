/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.render

import dev.lyzev.api.events.EventAttackEntity
import dev.lyzev.api.events.on
import dev.lyzev.api.imgui.font.ImGuiFonts
import dev.lyzev.api.setting.settings.slider
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.ModuleToggleableRenderImGuiContent
import imgui.ImGui.*
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiWindowFlags
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.network.PlayerListEntry
import net.minecraft.client.render.entity.LivingEntityRenderer
import net.minecraft.client.texture.AbstractTexture
import net.minecraft.entity.player.PlayerEntity

object ModuleToggleableTargetHUD : ModuleToggleableRenderImGuiContent(
    "Target HUD", "Shows information about the target.", category = IFeature.Category.RENDER
) {

    val duration by slider("Duration", "The duration of the target hud to show after hit.", 2000, 500, 10000, "ms")

    var target: PlayerEntity? = null
    private var abstractTexture: AbstractTexture? = null
    private var lastHit = 0L

    override fun renderImGuiContent() {
        if (abstractTexture != null) {
            val target = if (this.target == null) mc.player!! else this.target!!
            val upsideDown = LivingEntityRenderer.shouldFlipUpsideDown(target)
            val uvMinY = 8f + (if (upsideDown) 8f else 0f)
            val uvMaxY = 8f * (if (upsideDown) -1f else 1f) + uvMinY
            val cursorScreenPos = getCursorScreenPos()
            getWindowDrawList().addImageRounded(
                abstractTexture!!.glId,
                cursorScreenPos.x,
                cursorScreenPos.y,
                cursorScreenPos.x + 50f,
                cursorScreenPos.y + 50f,
                8f / 64f,
                uvMinY / 64f,
                16f / 64f,
                uvMaxY / 64f,
                -1,
                5f
            )
            dummy(50f, 50f)
            sameLine()
            val text = """
            Target: %s
            Distance: %.1f
            """.trimIndent().format(target.displayName?.string, mc.player?.distanceTo(target))
            ImGuiFonts.OPEN_SANS_BOLD.begin()
            if (beginChild(
                    "##Info",
                    calcTextSize(text).x,
                    46f,
                    false,
                    ImGuiWindowFlags.NoScrollbar or ImGuiWindowFlags.NoBackground
                )
            ) {
                text(text)
                pushStyleColor(ImGuiCol.PlotHistogram, getColorU32(ImGuiCol.Button))
                progressBar(target.health / target.maxHealth, -1f, 3f, "##Progress")
                popStyleColor()
            }
            endChild()
            ImGuiFonts.OPEN_SANS_BOLD.end()
        }
        if (System.currentTimeMillis() - lastHit > duration) {
            this.target = null
            val skinTextures = PlayerListEntry.texturesSupplier(mc.player!!.gameProfile).get()
            abstractTexture = mc.textureManager.getTexture(skinTextures!!.texture)
        }
    }

    override val shouldDrawWindow: Boolean
        get() = shouldHandleEvents && mc.currentScreen !is HandledScreen<*> && (target != null || mc.currentScreen != null) && mc.player != null

    init {
        on<EventAttackEntity> {
            if (it.entity !is PlayerEntity) return@on
            target = it.entity
            val skinTextures = PlayerListEntry.texturesSupplier(target!!.gameProfile).get()
            abstractTexture = mc.textureManager.getTexture(skinTextures!!.texture)
            lastHit = System.currentTimeMillis()
        }
    }
}
