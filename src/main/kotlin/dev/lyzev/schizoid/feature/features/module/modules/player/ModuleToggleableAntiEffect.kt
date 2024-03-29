/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.player

import dev.lyzev.api.events.EventHasStatusEffect
import dev.lyzev.api.events.EventListener
import dev.lyzev.api.events.on
import dev.lyzev.api.setting.settings.switch
import dev.lyzev.schizoid.feature.features.module.ModuleToggleable
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.registry.Registries
import kotlin.reflect.KVisibility
import kotlin.reflect.full.staticProperties

object ModuleToggleableAntiEffect : ModuleToggleable("Anti Effect", "Makes everything brighter.", category = Category.PLAYER), EventListener {

    private val effects = mutableSetOf<StatusEffect>()

    override val shouldHandleEvents: Boolean
        get() = isEnabled && isIngame

    init {
        on<EventHasStatusEffect> { event ->
            if (mc.player == event.entity && effects.contains(event.effect))
                event.hasStatusEffect = false
        }

        StatusEffects::class.staticProperties.filter { it.visibility == KVisibility.PUBLIC }.forEach { prop ->
            val effect = prop.getter.call() as StatusEffect
            if (effect.isBeneficial)
                effects += effect
            switch(Registries.STATUS_EFFECT.getId(effect)!!.path, "Disables this effect.", !effect.isBeneficial) {
                if (it) effects += effect
                else effects -= effect
            }
        }
    }
}
