/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.render

import dev.lyzev.api.events.*
import dev.lyzev.api.setting.settings.slider
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.ModuleToggleable
import dev.lyzev.schizoid.injection.accessor.ClientPlayerEntityAccessor
import net.minecraft.client.network.OtherClientPlayerEntity
import net.minecraft.entity.Entity
import net.minecraft.network.packet.c2s.play.UpdatePlayerAbilitiesC2SPacket
import net.minecraft.util.math.Vec3d
import net.minecraft.entity.player.PlayerAbilities

object ModuleToggleableFreeCam :
    ModuleToggleable(
        "Free Cam",
        "Allows the player to move the camera freely without moving the player character.",
        category = IFeature.Category.RENDER
    ), EventListener {

    private var fakePlayer: OtherClientPlayerEntity? = null

    /**
     * The last fly speed of the player.
     *
     * The default value is 0.05f.
     * @see [PlayerAbilities.flySpeed]
     */
    private var lastFlySpeed = 0.05f

    private var lastSneaking = false
    private var lastOnGround = false

    val flySpeed by slider("Fly Speed", "The speed at which the player can fly.", 0.05f, 0f, 0.2f, 2)

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
        lastFlySpeed = mc.player!!.abilities.flySpeed
        val accessor = mc.player as ClientPlayerEntityAccessor
        lastSneaking = accessor.getLastSneaking()
        lastOnGround = accessor.getLastOnGround()
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
                mc.player!!.input.sneaking = fakePlayer!!.isSneaking
                mc.player!!.velocity = Vec3d.ZERO
                val accessor = mc.player as ClientPlayerEntityAccessor
                accessor.setLastSneaking(lastSneaking)
                accessor.setLastOnGround(fakePlayer!!.isOnGround)
            }
        }
        if (mc.player != null) {
            mc.player!!.abilities.flying = false
            mc.player!!.abilities.allowFlying = false
            mc.player!!.abilities.flySpeed = lastFlySpeed
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
            event.player.abilities.flySpeed = flySpeed
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
            event.player.input.sneaking = fakePlayer!!.isSneaking
            val accessor = event.player as ClientPlayerEntityAccessor
            accessor.setLastSneaking(lastSneaking)
            accessor.setLastOnGround(lastOnGround)
        }

        on<EventClientPlayerEntityTickPost> { event ->
            event.player.isOnGround = isOnGround
            event.player.setPosition(pos)
            event.player.yaw = yaw
            event.player.pitch = pitch
            lastSneaking = event.player.input.sneaking
            lastOnGround = event.player.isOnGround
            event.player.isSprinting = isSprinting
            event.player.input.sneaking = isSneaking
            val accessor = event.player as ClientPlayerEntityAccessor
            accessor.setLastSneaking(isSneaking)
            accessor.setLastOnGround(isOnGround)
        }

        on<EventClientPlayerEntityIsSpectator> { event ->
            event.isSpectator = true
        }

        on<EventPacket> { event ->
            if (event.packet is UpdatePlayerAbilitiesC2SPacket) {
                event.isCancelled = true
            }
        }
    }
}
