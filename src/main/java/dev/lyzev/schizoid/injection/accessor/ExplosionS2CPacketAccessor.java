/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.injection.accessor;

import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ExplosionS2CPacket.class)
public interface ExplosionS2CPacketAccessor {

    @Accessor
    @Mutable
    void setPlayerVelocityX(float playerVelocityX);

    @Accessor
    @Mutable
    void setPlayerVelocityY(float playerVelocityY);

    @Accessor
    @Mutable
    void setPlayerVelocityZ(float playerVelocityZ);
}
