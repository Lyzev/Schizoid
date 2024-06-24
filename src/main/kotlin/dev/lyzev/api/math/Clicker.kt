/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.math

import dev.lyzev.api.setting.settings.OptionEnum
import dev.lyzev.api.setting.settings.option
import dev.lyzev.api.setting.settings.slider
import dev.lyzev.api.setting.settings.switch
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.modules.combat.ModuleToggleableTriggerBot.mc
import net.minecraft.util.math.MathHelper
import dev.lyzev.api.settings.Setting.Companion.eq
import kotlin.math.roundToInt

class Clicker(val container: IFeature) {

    val noiseGenerator = NoiseGenerator()
    val clicks = mutableMapOf<Long, Int>()

    val type by container.option("Type", "The type of the clicker.", Type.Cooldown, Type.entries)
    val cps by container.slider("CPS", "The clicks per second of the clicker.", 12, 1, 20, "cps", hide = ::type eq Type.Cooldown)
    val force by container.switch("Force", "Forces the clicker to click if needed.", true)

    fun tick(force: Boolean): Int {
        val force = force && this.force
        clicks.keys.filter { System.currentTimeMillis() - it > 1000 }.forEach { clicks.remove(it) }
        when (type) {
            Type.Constant -> {
                val sum = clicks.values.sum()
                if (sum >= cps && !force) {
                    return 0
                }
                clicks[System.currentTimeMillis()] = 1
                return 1
            }
            Type.Dynamic -> {
                val cpt = MathHelper.clamp(cps - clicks.values.sum(), 0, cps / 5)
                val noise = noiseGenerator.noise(System.currentTimeMillis().toDouble())
                var clicksThisTick = ((noise + 1) * cpt).roundToInt()
                if (clicksThisTick == 0 && force) {
                    clicksThisTick = 1
                }
                clicks[System.currentTimeMillis()] = clicksThisTick
                return clicksThisTick
            }
            Type.Cooldown -> {
                if ((mc.attackCooldown <= 0 && mc.player!!.getAttackCooldownProgress(1f) == 1f) || force) {
                    clicks[System.currentTimeMillis()] = 1
                    return 1
                }
                return 0
            }
        }
    }

    fun regenerate() = noiseGenerator.setSeed()

    enum class Type : OptionEnum {
        Constant,
        Dynamic,
        Cooldown;

        override val key = name
    }
}
