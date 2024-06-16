/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.injection.accessor;

import net.minecraft.client.realms.RealmsAvailability;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.concurrent.CompletableFuture;

/**
 * Provides the means to access protected members of the Realms availability check.
 */
@Mixin(RealmsAvailability.class)
public interface RealmsAvailabilityAccessor {
    /**
     * Sets the Realms availability info checker.
     *
     * @param availabilityInfo Realms availability info completable future
     */
    @Accessor
    @Mutable
    static void setCurrentFuture(CompletableFuture<RealmsAvailability.Info> availabilityInfo) {}
}
