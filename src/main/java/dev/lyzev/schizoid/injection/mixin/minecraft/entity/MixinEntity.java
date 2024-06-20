/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.injection.mixin.minecraft.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.lyzev.api.events.EventIsInvisibleTo;
import dev.lyzev.api.events.EventUpdateVelocity;
import dev.lyzev.schizoid.Schizoid;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * This class provides a mixin for the Entity class in the Minecraft entity package.
 * It modifies the behavior of the isInvisibleTo method of the Entity class.
 */
@Mixin(Entity.class)
public class MixinEntity {

    /**
     * This method is a redirect for the isInvisibleTo method of the Entity class.
     * It creates and fires an EventIsInvisibleTo event before the invisibility status is checked.
     * The invisibility status is then determined by the result of the event.
     *
     * @param player The player that the Entity is being checked for invisibility against.
     * @param cir    The callback information.
     * @return The invisibility status of the Entity, as determined by the EventIsInvisibleTo event.
     */
    @Inject(method = "isInvisibleTo", at = @At("RETURN"), cancellable = true)
    private void onIsInvisibleTo(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        EventIsInvisibleTo event = new EventIsInvisibleTo(cir.getReturnValueZ());
        event.fire();
        cir.setReturnValue(event.isInvisible());
    }

    @WrapOperation(method = "updateVelocity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getYaw()F"))
    private float onUpdateVelocity(Entity instance, Operation<Float> original) {
        if (!instance.equals(Schizoid.INSTANCE.getMc().player)) {
            return original.call(instance);
        }
        EventUpdateVelocity event = new EventUpdateVelocity(original.call(instance));
        event.fire();
        return event.getYaw();
    }
}
