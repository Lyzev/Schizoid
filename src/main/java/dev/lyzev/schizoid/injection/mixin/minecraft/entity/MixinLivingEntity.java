/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.injection.mixin.minecraft.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.lyzev.api.events.EventGetAttributeValue;
import dev.lyzev.api.events.EventHasStatusEffect;
import dev.lyzev.schizoid.Schizoid;
import dev.lyzev.schizoid.feature.features.module.modules.movement.ModuleToggleableAirJump;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * This class provides mixins for the LivingEntity class in the Minecraft entity package.
 * It modifies the behavior of the tickMovement and hasStatusEffect methods of the LivingEntity class.
 */
@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {

    @Shadow
    protected boolean jumping;

    @Shadow
    private int jumpingCooldown;

    @Shadow
    protected abstract void jump();

    /**
     * This method is a mixin for the tickMovement method of the LivingEntity class.
     * It checks if the ModuleToggleableAirJump is enabled and if the jumping cooldown is less than or equal to the difference between 10 and the jump cooldown of the ModuleToggleableAirJump.
     * If both conditions are met, it triggers a jump and resets the jumping cooldown.
     *
     * @param ci The callback information.
     */
    @Inject(method = "tickMovement", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;jumping:Z", ordinal = 1, shift = At.Shift.BEFORE))
    private void onTickMovement(CallbackInfo ci) {
        if (ModuleToggleableAirJump.INSTANCE.isEnabled() && jumping && 10 - ModuleToggleableAirJump.INSTANCE.getJumpCooldown() >= jumpingCooldown) {
            jump();
            jumpingCooldown = 0;
        }
    }

    /**
     * This method is a mixin for the hasStatusEffect method of the LivingEntity class.
     * It creates and fires an EventHasStatusEffect event after the status effect check is performed.
     * The result of the status effect check is then replaced by the result of the event.
     *
     * @param effect The status effect to check.
     * @param cir    The callback information, which includes the return value of the status effect check.
     */
    @Inject(method = "hasStatusEffect", at = @At("RETURN"), cancellable = true)
    public void onHasStatusEffect(RegistryEntry<StatusEffect> effect, CallbackInfoReturnable<Boolean> cir) {
        EventHasStatusEffect event = new EventHasStatusEffect((LivingEntity) (Object) this, effect.value(), cir.getReturnValue());
        event.fire();
        cir.setReturnValue(event.getHasStatusEffect());
    }

    @WrapOperation(method = "getAttributeValue", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/attribute/AttributeContainer;getValue(Lnet/minecraft/registry/entry/RegistryEntry;)D", ordinal = 0))
    private double onGetAttributeValue(AttributeContainer instance, RegistryEntry<EntityAttribute> attribute, Operation<Double> original) {
        if (!this.equals(Schizoid.INSTANCE.getMc().player)) {
            return original.call(instance, attribute);
        }
        EventGetAttributeValue event = new EventGetAttributeValue(attribute.value(), original.call(instance, attribute));
        event.fire();
        return event.getValue();
    }
}
