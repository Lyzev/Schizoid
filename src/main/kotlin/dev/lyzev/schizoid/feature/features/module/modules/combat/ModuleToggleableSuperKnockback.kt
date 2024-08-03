/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.combat

import dev.lyzev.api.events.EventAttackEntityPre
import dev.lyzev.api.events.EventClientPlayerEntityTickPre
import dev.lyzev.api.events.EventListener
import dev.lyzev.api.events.on
import dev.lyzev.api.setting.settings.OptionEnum
import dev.lyzev.api.setting.settings.option
import dev.lyzev.api.setting.settings.slider
import dev.lyzev.schizoid.Schizoid
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.ModuleToggleable
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket

object ModuleToggleableSuperKnockback : ModuleToggleable(
    "Super Knockback",
    "Automatically sprint resets in combat to deal more knockback.",
    category = IFeature.Category.COMBAT
), EventListener {

    val mode by option("Mode", "The mode used to W-Tap.", Mode.WTap, Mode.entries) {
        if (isEnabled) {
            onDisable()
        }
    }
    val chance by slider("Chance", "The chance to sprint reset.", 50, 0, 100, "%%")
    val hurtTimeThreshold by slider("Hurt Time Threshold", "The threshold for the hurt time.", 15, 0, 100, "%%")
    val waitForKnockback by slider("Wait For Knockback", "The time to wait for knockback.", 3, 0, 20, "ticks")
    val stopTick by slider("Stop Tick", "The tick to stop.", 3, 0, 20, "ticks")
    val randomTickOffset by slider(
        "Random Tick Offset", "Applies a random tick offset to the delays.", 2, 0, 10, "ticks"
    )

    private var hasAttacked = false
    private var lastTarget: Entity? = null

    override fun onDisable() {
        super.onDisable()
        hasAttacked = false
        lastTarget = null
    }

    override val shouldHandleEvents
        get() = isEnabled

    init {
        var waitingForKnockback = 0
        var storeState = false
        var stop = 0

        var wasForward = false
        var wasBack = false

        on<EventAttackEntityPre> { event ->
            if (mc.player!!.isSprinting && !hasAttacked && Schizoid.random.nextFloat() <= chance / 100f) {
                if (event.entity is LivingEntity && event.entity.hurtTime > (hurtTimeThreshold / 100f) * event.entity.maxHurtTime) {
                    return@on
                }
                lastTarget = event.entity
                hasAttacked = true
                waitingForKnockback = -Schizoid.random.nextInt(randomTickOffset + 1)
            }
        }

        on<EventClientPlayerEntityTickPre> { event ->
            if (hasAttacked && lastTarget != null) {
                if (waitingForKnockback <= waitForKnockback) {
                    if (
                        (event.player.velocity.length() < lastTarget!!.velocity.length() &&
                            event.player.velocity.dotProduct(lastTarget!!.velocity) < 0 &&
                            lastTarget!!.velocity.dotProduct(event.player.velocity) < 0
                            ) || mode == Mode.Packet
                    ) {
                        if (mode != Mode.Packet) {
                            if (!storeState) {
                                wasForward = mc.options.forwardKey.isPressed
                                wasBack = mc.options.backKey.isPressed
                                storeState = true
                            }
                        }
                        mode.start()
                        stop = -Schizoid.random.nextInt(randomTickOffset + 1)
                        hasAttacked = false
                        lastTarget = null
                    }
                }
                waitingForKnockback++
            } else if (storeState) {
                if (stop >= stopTick) {
                    mc.options.sprintKey.isPressed = true
                    mc.options.forwardKey.isPressed = wasForward
                    mc.options.backKey.isPressed = wasBack
                    storeState = false
                }
                stop++
            }
        }
    }

    enum class Mode : OptionEnum {
        Packet {
            override fun start() {
                mc.player!!.networkHandler.sendPacket(
                    ClientCommandC2SPacket(
                        mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING
                    )
                )
                mc.player!!.networkHandler.sendPacket(
                    ClientCommandC2SPacket(
                        mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING
                    )
                )
            }
        },
        WTap {
            override fun start() {
                mc.options.sprintKey.isPressed = false
                mc.options.forwardKey.isPressed = false
            }
        },
        STap {
            override fun start() {
                mc.options.sprintKey.isPressed = false
                mc.options.forwardKey.isPressed = false
                mc.options.backKey.isPressed = true
            }
        };

        abstract fun start()

        override val key = name
    }
}
