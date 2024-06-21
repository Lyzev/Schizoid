/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.render

import dev.lyzev.api.events.*
import dev.lyzev.api.setting.settings.slider
import dev.lyzev.api.setting.settings.switch
import dev.lyzev.api.settings.Setting.Companion.neq
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.ModuleToggleable
import net.minecraft.block.Blocks

object ModuleToggleableTrueSight : ModuleToggleable(
    "True Sight",
    "Makes invisible entities and barriers visible.",
    category = IFeature.Category.RENDER
), EventListener {

    val entities by switch("Entities", "Makes invisible entities visible.", true)
    val alpha by slider("Alpha", "The alpha of the entities.", 15, 1, 100, "%%", hide = ::entities neq true)
    val barriers by switch("Barriers", "Makes barriers visible.", true)

    override val shouldHandleEvents: Boolean
        get() = isEnabled

    init {
        on<EventIsInvisibleTo> { event ->
            if (entities) {
                event.isInvisible = false
            }
        }

        on<EventRenderModel> { event ->
            val alpha = event.argb shr 24 and 0xFF
            if (entities && alpha < 0xFF) {
                event.argb = event.argb and 0xFFFFFF or (this.alpha / 100f * 255).toInt() shl 24
            }
        }

        on<EventBlockParticle> { event ->
            if (barriers) {
                event.block = Blocks.BARRIER
            }
        }
    }
}
