/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.util

import dev.lyzev.api.setting.settings.OptionEnum
import dev.lyzev.api.setting.settings.option
import dev.lyzev.schizoid.feature.features.module.ModuleRunnable
import net.minecraft.item.ItemStack
import net.minecraft.nbt.StringNbtReader

object ModuleRunnablePasteNBT :
    ModuleRunnable("Paste NBT", "Pastes the NBT data to your inventory.", category = Category.UTIL) {

    override fun invoke(): String? {
        if (!isIngame) return "You are not in a game."

        if (!mc.player!!.isInCreativeMode) {
            return "You must be in creative mode."
        }

        val nbt = try {
            StringNbtReader.parse(mc.keyboard.clipboard)
        } catch (_: Throwable) {
            return "Invalid data in clipboard."
        }

        mc.player!!.giveItemStack(ItemStack.fromNbtOrEmpty(mc.world!!.registryManager, nbt))
        return null
    }

    enum class Hand(val type: net.minecraft.util.Hand) : OptionEnum {
        MAIN_HAND(net.minecraft.util.Hand.MAIN_HAND),
        OFF_HAND(net.minecraft.util.Hand.OFF_HAND);

        override val key: String
            get() = type.name
    }
}
