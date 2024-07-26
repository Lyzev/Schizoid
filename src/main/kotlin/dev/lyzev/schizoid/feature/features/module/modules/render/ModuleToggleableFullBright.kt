/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.render

import dev.lyzev.api.events.EventClientPlayerEntityTickPre
import dev.lyzev.api.events.EventGamma
import dev.lyzev.api.events.EventListener
import dev.lyzev.api.events.on
import dev.lyzev.api.setting.settings.OptionEnum
import dev.lyzev.api.setting.settings.option
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.ModuleToggleable
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects

object ModuleToggleableFullBright :
    ModuleToggleable("Full Bright", "Makes everything brighter.", category = IFeature.Category.RENDER), EventListener {

    val mode by option("Mode", "The mode of the full bright.", Mode.GAMMA, Mode.entries) {
        if (isEnabled && it == Mode.GAMMA) {
            mc.player?.removeStatusEffect(StatusEffects.NIGHT_VISION)
        }
    }

    override fun onDisable() {
        if (mode == Mode.NIGHT_VISION)
            mc.player?.removeStatusEffect(StatusEffects.NIGHT_VISION)
    }

    override val shouldHandleEvents: Boolean
        get() = isEnabled && isIngame

    init {
        on<EventClientPlayerEntityTickPre> {
            if (mode == Mode.NIGHT_VISION)
                mc.player?.addStatusEffect(StatusEffectInstance(StatusEffects.NIGHT_VISION, Int.MAX_VALUE))
        }

        on<EventGamma> {
            if (mode == Mode.GAMMA) {
                it.gamma = 100f
            }
        }
    }

    enum class Mode(override val key: String) : OptionEnum {
        GAMMA("Gamma"),
        NIGHT_VISION("Night Vision");
    }
}
