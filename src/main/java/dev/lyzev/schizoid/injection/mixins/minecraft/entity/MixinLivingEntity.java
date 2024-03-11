/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.injection.mixins.minecraft.entity;

import dev.lyzev.schizoid.feature.features.module.modules.movement.ModuleToggleableAirJump;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {

    @Shadow
    protected boolean jumping;

    @Shadow
    private int jumpingCooldown;

    @Shadow
    protected abstract void jump();

    @Inject(method = "tickMovement", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;jumping:Z", ordinal = 1, shift = At.Shift.BEFORE))
    private void injectTickMovement(CallbackInfo ci) {
        if (ModuleToggleableAirJump.INSTANCE.isEnabled() && jumping && 10 - ModuleToggleableAirJump.INSTANCE.getJumpCooldown() >= jumpingCooldown) {
            jump();
            jumpingCooldown = 0;
        }
    }
}
