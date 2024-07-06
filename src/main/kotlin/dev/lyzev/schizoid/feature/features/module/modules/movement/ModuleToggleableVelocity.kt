/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.movement

import dev.lyzev.api.events.*
import dev.lyzev.api.setting.settings.*
import dev.lyzev.schizoid.Schizoid
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.ModuleToggleable
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket
import dev.lyzev.api.settings.Setting.Companion.neq
import dev.lyzev.schizoid.injection.accessor.ExplosionS2CPacketAccessor
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.util.math.MathHelper

object ModuleToggleableVelocity :
    ModuleToggleable("Velocity", "Modify/Reduce the receiving velocity.", category = IFeature.Category.MOVEMENT),
    EventListener {

    val packets by multiOption("Packets", "The packets to use.", setOf("Velocity" to true, "Explosion" to true))
    val chance by slider("Chance", "The chance to execute on the packet.", 60, 0, 100, "%%")
    val velocityThreshold by slider("Velocity Threshold", "Doesn't execute if the horizontal velocity is below this threshold.", 0.1f, 0f, 1f, 1, "blocks")
    val mode by option("Mode", "The mode to use.", Mode.Jump, Mode.entries)
    val jumpInScreen by switch("Jump In Screen", "Jumps in the screen.", true, hide = ::mode neq Mode.Jump)
    val jumpInHandledScreen by switch("Jump In Handled Screen", "Jumps in the screen.", true, hide = {
        mode != Mode.Jump || !jumpInScreen
    })
    val horizontal by slider("Horizontal", "The horizontal multiplier.", 100, -200, 200, "%%")
    val vertical by slider("Vertical", "The vertical multiplier.", 100, -200, 200, "%%")

    private var wasJumping = false
    private var shouldJump = false

    override fun onDisable() {
        shouldJump = false
        wasJumping = false
    }

    override val shouldHandleEvents
        get() = isEnabled

    init {
        on<EventPacket> { event ->
            if (event.type == EventPacket.Type.S2C) {
                if (event.packet is EntityVelocityUpdateS2CPacket || event.packet is ExplosionS2CPacket) {
                    if (chance < Schizoid.random.nextInt(100)) return@on
                    packets.forEach {
                        if (it.second) {
                            if (it.first == "Velocity" && event.packet is EntityVelocityUpdateS2CPacket) {
                                val squaredDist = MathHelper.square(event.packet.velocityX) + MathHelper.square(event.packet.velocityZ)
                                if (squaredDist < MathHelper.square(velocityThreshold)) return@forEach
                                val entity = mc.world!!.getEntityById(event.packet.id)
                                if (entity == mc.player) {
                                    mode.handle(event)
                                }
                            } else if (it.first == "Explosion" && event.packet is ExplosionS2CPacket) {
                                val squaredDist = MathHelper.square(event.packet.playerVelocityX) + MathHelper.square(event.packet.playerVelocityZ)
                                if (squaredDist < MathHelper.square(velocityThreshold)) return@forEach
                                mode.handle(event)
                            }
                        }
                    }
                }
            }
        }
        on<EventClientPlayerEntityTick> {
            if (shouldJump) {
                if ((jumpInScreen && (jumpInHandledScreen || mc.currentScreen !is HandledScreen<*>)) || mc.currentScreen == null) {
                    wasJumping = mc.options.jumpKey.isPressed
                    mc.options.jumpKey.isPressed = true
                }
                shouldJump = false
            } else if (wasJumping) {
                mc.options.jumpKey.isPressed = false
                wasJumping = false
            }
        }
        on<EventVelocity> { event ->
            if (mode != Mode.Modify) return@on
            val squaredDist = MathHelper.square(event.x) + MathHelper.square(event.z)
            if (squaredDist < MathHelper.square(velocityThreshold)) return@on
            if (chance < Schizoid.random.nextInt(100)) return@on
            packets.forEach {
                if (it.second && it.first == "Velocity") {
                    event.x *= (horizontal / 100.0)
                    event.y *= (vertical / 100.0)
                    event.z *= (horizontal / 100.0)
                }
            }
        }
    }

    enum class Mode : OptionEnum {
        Jump {
            override fun handle(event: EventPacket) {
                shouldJump = true
            }
        },
        Modify {
            override fun handle(event: EventPacket) {
                val packet = event.packet
                if (packet is ExplosionS2CPacket) {
                    val accessor = packet as ExplosionS2CPacketAccessor
                    accessor.setPlayerVelocityX(packet.playerVelocityX * (horizontal / 100f))
                    accessor.setPlayerVelocityY(packet.playerVelocityY * (vertical / 100f))
                    accessor.setPlayerVelocityZ(packet.playerVelocityZ * (horizontal / 100f))
                }
            }
        },
        Cancel {
            override fun handle(event: EventPacket) {
                event.isCancelled = true
            }
        };

        abstract fun handle(event: EventPacket)

        override val key = name
    }
}
