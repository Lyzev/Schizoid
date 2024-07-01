/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.combat

import dev.lyzev.api.events.*
import dev.lyzev.api.setting.settings.OptionEnum
import dev.lyzev.api.setting.settings.option
import dev.lyzev.api.setting.settings.slider
import dev.lyzev.schizoid.Schizoid
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.ModuleToggleable
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket

object ModuleToggleableWTap :
    ModuleToggleable("W-Tap", "Automatically W-Taps in combat.", category = IFeature.Category.COMBAT), EventListener {

    val mode by option("Mode", "The mode used to W-Tap.", Mode.STap, Mode.entries) {
        if (isEnabled) {
            onDisable()
        }
    }
    val wTapChance by slider("W Tap Chance", "The chance to w tap.", 70, 0, 100, "%%")
    val stopDelay by slider("Stop Delay", "The delay between hitting and stopping.", 1, 0, 20, "ticks")
    val startDelay by slider("Start Delay", "The delay between hitting and starting.", 2, 0, 20, "ticks")
    val randomTickOffset by slider(
        "Random Tick Offset", "Applies a random tick offset to the start/stop delay.", 2, 0, 10, "ticks"
    )

    private var wasSprinting = false
    private var wasToggledSprinting = false
    private var wasForward = false
    private var wasBack = false
    private var shouldStop = false
    private var shouldStart = false

    override fun onDisable() {
        if (shouldStart) {
            mc.options.sprintKey.isPressed = wasSprinting
            mc.options.sprintToggled.value = wasToggledSprinting
            mc.options.forwardKey.isPressed = wasForward
            mc.options.backKey.isPressed = wasBack
        }
        shouldStop = false
        shouldStart = false
    }

    override val shouldHandleEvents
        get() = isEnabled

    init {
        var stop = 0
        var start = 0
        var stopTick = stopDelay
        var startTick = startDelay
        var jump = false

        on<EventAttackEntity> {
            if (mc.player!!.isSprinting && Schizoid.random.nextDouble() <= wTapChance / 100.0) {
                stop = 0
                stopTick = stopDelay + Schizoid.random.nextInt(randomTickOffset + 1)
                shouldStop = true
            }
        }

        on<EventPacket> { event ->
            if (event.type == EventPacket.Type.S2C) {
                if (event.packet is EntityVelocityUpdateS2CPacket && mc.world!!.getEntityById(event.packet.id) == mc.player) {
                    if (Schizoid.random.nextDouble() <= wTapChance / 100.0) {
                        jump = true
                    }
                }
            }
        }

        on<EventClientPlayerEntityTick> {
            if (jump) {
                if (mc.options.jumpKey.isPressed) {
                    mc.options.jumpKey.isPressed = false
                    jump = false
                    println("Jumped!")
                } else {
                    println("Jumping!")
                    mc.options.jumpKey.isPressed = true
                }
            }
            if (shouldStop) {
                if (stop >= stopTick) {
                    if (mode != Mode.Packet) {
                        if (!shouldStart) {
                            wasSprinting = mc.options.sprintKey.isPressed
                            wasToggledSprinting = mc.options.sprintToggled.value
                            wasForward = mc.options.forwardKey.isPressed
                            wasBack = mc.options.backKey.isPressed
                            shouldStart = true
                        }
                    }
                    mode.handle()
                    start = 0
                    startTick = startDelay + Schizoid.random.nextInt(randomTickOffset)
                    shouldStop = false
                }
                stop++
            } else if (shouldStart) {
                if (start >= startTick) {
                    mc.options.sprintKey.isPressed = wasSprinting
                    mc.options.sprintToggled.value = wasToggledSprinting
                    mc.options.forwardKey.isPressed = wasForward
                    mc.options.backKey.isPressed = wasBack
                    shouldStart = false
                }
                start++
            }
        }
    }

    enum class Mode : OptionEnum {
        Packet {
            override fun handle() {
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
            override fun handle() {
                mc.options.sprintKey.isPressed = false
                mc.options.forwardKey.isPressed = false
            }
        },
        STap {
            override fun handle() {
                mc.options.sprintKey.isPressed = false
                mc.options.forwardKey.isPressed = false
                mc.options.backKey.isPressed = true
            }
        };

        abstract fun handle()

        override val key = name
    }
}
