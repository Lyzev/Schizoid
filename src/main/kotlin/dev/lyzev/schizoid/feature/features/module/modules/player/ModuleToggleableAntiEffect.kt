/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.player

import dev.lyzev.api.events.EventHasStatusEffect
import dev.lyzev.api.events.EventListener
import dev.lyzev.api.events.on
import dev.lyzev.api.setting.settings.multiOption
import dev.lyzev.schizoid.feature.features.module.ModuleToggleable
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry

object ModuleToggleableAntiEffect : ModuleToggleable("Anti Effect", "Makes everything brighter.", category = Category.PLAYER), EventListener {

    val effects = multiOption("Effects", "Effects to disable.", Registries.STATUS_EFFECT.map { it.name.string to !it.isBeneficial }.sortedBy { it.first }.toSet()) { options ->
        for (option in options) {
            if (option.second)
                remove.add(Registries.STATUS_EFFECT.first { it.name.string == option.first })
            else
                remove.remove(Registries.STATUS_EFFECT.first { it.name.string == option.first })
        }
    }

    private val remove = Registries.STATUS_EFFECT.filter { !it.isBeneficial }.toMutableSet()

    override val shouldHandleEvents: Boolean
        get() = isEnabled && isIngame

    init {
        on<EventHasStatusEffect> { event ->
            if (mc.player == event.entity && remove.contains(event.effect))
                event.hasStatusEffect = false
        }
    }
}
