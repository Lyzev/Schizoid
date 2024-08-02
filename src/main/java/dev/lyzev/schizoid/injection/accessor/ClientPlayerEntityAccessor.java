/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.injection.accessor;

import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientPlayerEntity.class)
public interface ClientPlayerEntityAccessor {

    @Mutable
    @Accessor
    void setLastSneaking(boolean lastSneaking);

    @Mutable
    @Accessor
    void setLastOnGround(boolean lastOnGround);

    @Accessor
    boolean getLastSneaking();

    @Accessor
    boolean getLastOnGround();
}
