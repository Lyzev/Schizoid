/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.util

import dev.lyzev.api.setting.settings.OptionEnum
import dev.lyzev.api.setting.settings.option
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.ModuleRunnable
import dev.lyzev.schizoid.feature.features.module.modules.render.ModuleToggleableNotifications

object ModuleRunnableCopyNBT :
    ModuleRunnable(
        "Copy NBT",
        "Copies the NBT data of the item in your hand to the clipboard.",
        category = IFeature.Category.UTIL
    ) {

    private val hand by option("Hand", "The hand to copy the NBT from.", Hand.MAIN_HAND, Hand.entries)

    override fun invoke() {
        if (!isIngame) {
            ModuleToggleableNotifications.error("You are not in a world.")
            return
        }
        val itemInHand = mc.player?.getStackInHand(hand.type)
        if (itemInHand == null || itemInHand.components == null || itemInHand.components!!.isEmpty) {
            ModuleToggleableNotifications.error("You are not holding an item with NBT data.")
            return
        }
        copy(itemInHand.encode(mc.world?.registryManager).asString())
        ModuleToggleableNotifications.info("The NBT data has been copied to the clipboard.")
    }

    enum class Hand(val type: net.minecraft.util.Hand) : OptionEnum {
        MAIN_HAND(net.minecraft.util.Hand.MAIN_HAND),
        OFF_HAND(net.minecraft.util.Hand.OFF_HAND);

        override val key: String
            get() = type.name
    }
}
