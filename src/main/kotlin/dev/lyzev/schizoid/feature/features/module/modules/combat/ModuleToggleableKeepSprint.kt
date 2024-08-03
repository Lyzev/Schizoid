/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.combat

import dev.lyzev.api.events.EventAttackEntityPost
import dev.lyzev.api.events.EventAttackEntityPre
import dev.lyzev.api.events.EventListener
import dev.lyzev.api.events.on
import dev.lyzev.api.setting.settings.slider
import dev.lyzev.api.setting.settings.switch
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.ModuleToggleable
import net.minecraft.util.math.Vec3d

object ModuleToggleableKeepSprint :
    ModuleToggleable("Keep Sprint", "Keeps sprinting while attacking.", category = IFeature.Category.COMBAT),
    EventListener {

    val velocityMultiplier by slider("Velocity Multiplier", "Multiplies the player's velocity.", 100, 0, 100, "%%")
    val sprint by switch("Sprint", "Keeps sprinting while attacking.", true)

    private var velocity: Vec3d? = null
    private var isSprinting = false

    override fun onDisable() {
        super.onDisable()
        velocity = null
        isSprinting = false
    }

    override val shouldHandleEvents
        get() = isEnabled

    init {
        on<EventAttackEntityPre> {
            velocity = mc.player!!.velocity
            isSprinting = mc.player!!.isSprinting
        }
        on<EventAttackEntityPost> {
            if (isSprinting && sprint) {
                mc.player!!.isSprinting = true
            }
            if (velocity == null) return@on
            val multi = velocityMultiplier / 100
            mc.player?.velocity = velocity?.multiply(multi.toDouble(), 1.0, multi.toDouble())
        }
    }
}
