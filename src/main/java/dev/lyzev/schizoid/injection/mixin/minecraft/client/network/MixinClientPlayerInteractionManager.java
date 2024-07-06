/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.injection.mixin.minecraft.client.network;

import dev.lyzev.api.events.EventAttackEntityPost;
import dev.lyzev.api.events.EventAttackEntityPre;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This class provides mixins for the ClientPlayerInteractionManager class in the Minecraft client network package.
 * It modifies the behavior of the attackEntity method of the ClientPlayerInteractionManager class.
 */
@Mixin(ClientPlayerInteractionManager.class)
public class MixinClientPlayerInteractionManager {

    /**
     * This method is a mixin for the attackEntity method of the ClientPlayerInteractionManager class.
     * It fires an EventAttackEntity event at the start of the method.
     *
     * @param player The player entity.
     * @param target The target entity.
     * @param ci     The callback information.
     */
    @Inject(method = "attackEntity", at = @At("HEAD"))
    private void onAttackEntityPre(PlayerEntity player, Entity target, CallbackInfo ci) {
        EventAttackEntityPre event = new EventAttackEntityPre(player, target);
        event.fire();
    }

    @Inject(method = "attackEntity", at = @At("RETURN"))
    private void onAttackEntityPost(PlayerEntity player, Entity target, CallbackInfo ci) {
        EventAttackEntityPost event = new EventAttackEntityPost(player, target);
        event.fire();
    }
}
