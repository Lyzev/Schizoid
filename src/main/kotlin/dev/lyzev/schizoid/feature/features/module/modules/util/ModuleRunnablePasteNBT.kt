/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.util

import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.ModuleRunnable
import net.minecraft.item.ItemStack
import net.minecraft.nbt.StringNbtReader

object ModuleRunnablePasteNBT :
    ModuleRunnable("Paste NBT", "Pastes the NBT data to your inventory.", category = IFeature.Category.UTIL) {

    override fun invoke(): String? {
        if (!isIngame) return "You are not in a game."
        if (!mc.player!!.isInCreativeMode) return "You must be in creative mode."
        val nbt = try {
            StringNbtReader.parse(mc.keyboard.clipboard)
        } catch (_: Throwable) {
            return "Invalid data in clipboard."
        }
        mc.player!!.giveItemStack(ItemStack.fromNbtOrEmpty(mc.world!!.registryManager, nbt))
        return null
    }
}
