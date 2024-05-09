/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.util

import dev.lyzev.api.setting.settings.OptionEnum
import dev.lyzev.api.setting.settings.option
import dev.lyzev.schizoid.feature.features.module.ModuleRunnable

object ModuleRunnableCopyNBT :
    ModuleRunnable("Copy NBT", "Copies the NBT data of the item in your hand to the clipboard.", category = Category.UTIL) {

    private val hand by option("Hand", "The hand to copy the NBT from.", Hand.MAIN_HAND, Hand.entries)

    override fun invoke(): String? {
        if (!isIngame) return "You are not in a game."
        val itemInHand = mc.player?.getStackInHand(hand.type)
        if (itemInHand == null || itemInHand.components == null || itemInHand.components!!.isEmpty)
            return "There is no NBT data to copy."
        copy(itemInHand.encode(mc.world?.registryManager).asString())
        return null
    }

    enum class Hand(val type: net.minecraft.util.Hand) : OptionEnum {
        MAIN_HAND(net.minecraft.util.Hand.MAIN_HAND),
        OFF_HAND(net.minecraft.util.Hand.OFF_HAND);

        override val key: String
            get() = type.name
    }
}
