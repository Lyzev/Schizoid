/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.render

import dev.lyzev.api.events.*
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.ModuleToggleable
import net.minecraft.client.network.OtherClientPlayerEntity
import net.minecraft.entity.Entity
import net.minecraft.network.packet.c2s.play.UpdatePlayerAbilitiesC2SPacket
import net.minecraft.util.math.Vec3d

object ModuleToggleableFreeCam :
    ModuleToggleable(
        "Free Cam",
        "Allows the player to move the camera freely without moving the player character.",
        category = IFeature.Category.RENDER
    ), EventListener {

    private var fakePlayer: OtherClientPlayerEntity? = null

    override fun onEnable() {
        if (!isIngame) {
            toggle()
            return
        }
        fakePlayer = OtherClientPlayerEntity(mc.world!!, mc.player!!.gameProfile)
        fakePlayer!!.setPosition(mc.player!!.x, mc.player!!.y, mc.player!!.z)
        fakePlayer!!.yaw = mc.player!!.yaw
        fakePlayer!!.pitch = mc.player!!.pitch
        fakePlayer!!.isOnGround = mc.player!!.isOnGround
        fakePlayer!!.isSprinting = mc.player!!.isSprinting
        fakePlayer!!.isSneaking = mc.player!!.isSneaking
        fakePlayer!!.tick()
        mc.world!!.addEntity(fakePlayer!!)
    }

    override fun onDisable() {
        if (fakePlayer != null) {
            mc.world!!.removeEntity(fakePlayer!!.id, Entity.RemovalReason.DISCARDED)
            if (mc.player != null) {
                mc.player!!.setPosition(fakePlayer!!.x, fakePlayer!!.y, fakePlayer!!.z)
                mc.player!!.yaw = fakePlayer!!.yaw
                mc.player!!.pitch = fakePlayer!!.pitch
                mc.player!!.isOnGround = fakePlayer!!.isOnGround
                mc.player!!.isSprinting = fakePlayer!!.isSprinting
                mc.player!!.isSneaking = fakePlayer!!.isSneaking
                mc.player!!.velocity = Vec3d.ZERO
            }
        }
        if (mc.player != null) {
            mc.player!!.abilities.flying = false
            mc.player!!.abilities.allowFlying = false
        }
        fakePlayer = null
    }

    override val shouldHandleEvents: Boolean
        get() = isEnabled && isIngame

    init {
        on<EventClientPlayerEntityTickPre>(Event.Priority.LOWEST) { event ->
            if (fakePlayer == null) {
                toggle()
                return@on
            }
            event.player.abilities.flying = true
            event.player.abilities.allowFlying = true
        }

        var pos: Vec3d = Vec3d.ZERO
        var isOnGround = false
        var yaw = 0f
        var pitch = 0f
        var isSprinting = false
        var isSneaking = false

        on<EventClientPlayerEntitySendMovementPackets> { event ->
            pos = event.player.pos
            isOnGround = event.player.isOnGround
            yaw = event.player.yaw
            pitch = event.player.pitch
            isSprinting = event.player.isSprinting
            isSneaking = event.player.isSneaking
            event.player.isOnGround = fakePlayer!!.isOnGround
            event.player.setPosition(fakePlayer!!.x, fakePlayer!!.y, fakePlayer!!.z)
            event.player.yaw = fakePlayer!!.yaw
            event.player.pitch = fakePlayer!!.pitch
            event.player.isSprinting = fakePlayer!!.isSprinting
            event.player.isSneaking = fakePlayer!!.isSneaking
        }

        on<EventClientPlayerEntityTickPost> { event ->
            event.player.isOnGround = isOnGround
            event.player.setPosition(pos)
            event.player.yaw = yaw
            event.player.pitch = pitch
            event.player.isSprinting = isSprinting
            event.player.isSneaking = isSneaking
        }

        on<EventPacket> { event ->
            if (event.packet is UpdatePlayerAbilitiesC2SPacket) {
                event.isCancelled = true
            }
        }
    }
}
