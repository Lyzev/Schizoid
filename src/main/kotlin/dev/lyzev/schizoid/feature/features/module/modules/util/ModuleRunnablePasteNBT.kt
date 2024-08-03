/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.util

import dev.lyzev.schizoid.Schizoid
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.ModuleRunnable
import dev.lyzev.schizoid.feature.features.module.modules.render.ModuleToggleableNotifications
import net.minecraft.item.ItemStack
import net.minecraft.nbt.StringNbtReader

object ModuleRunnablePasteNBT :
    ModuleRunnable("Paste NBT", "Pastes the NBT data to your inventory.", category = IFeature.Category.UTIL) {

    override fun invoke() {
        if (!isIngame) {
            ModuleToggleableNotifications.error("You are not in a world.")
            return
        }
        if (!mc.player!!.isInCreativeMode) {
            ModuleToggleableNotifications.error("You are not in creative mode.")
            return
        }
        runCatching {
            val nbt = StringNbtReader.parse(mc.keyboard.clipboard)
            mc.player!!.giveItemStack(ItemStack.fromNbtOrEmpty(mc.world!!.registryManager, nbt))
        }.onFailure {
            Schizoid.logger.error("Failed to paste NBT.", it)
            ModuleToggleableNotifications.error("You have invalid NBT data in your clipboard.")
        }.onSuccess {
            Schizoid.logger.info("Successfully pasted NBT.")
            ModuleToggleableNotifications.info("The NBT data has been pasted to your inventory.")
        }
    }
}
