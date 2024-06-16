/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.injection.mixin.minecraft.client.network;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.lyzev.api.events.EventClientPlayerEntityTick;
import dev.lyzev.api.events.EventIsMovementKeyPressed;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This class provides a mixin for the ClientPlayerEntity class in the Minecraft client network package.
 * It modifies the behavior of the tick method of the ClientPlayerEntity class.
 */
@Mixin(ClientPlayerEntity.class)
public class MixinClientPlayerEntity {

    /**
     * This method is a mixin for the tick method of the ClientPlayerEntity class.
     * It creates and fires an EventClientPlayerEntityTick event before the tick method of the AbstractClientPlayerEntity class is invoked.
     * The event is fired at the start of the method.
     *
     * @param ci The callback information.
     */
    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;tick()V", shift = At.Shift.BEFORE))
    private void onTick(CallbackInfo ci) {
        new EventClientPlayerEntityTick((ClientPlayerEntity) (Object) this).fire();
    }

    /**
     * This method is a redirect for the isPressed method of the KeyBinding class.
     * It creates and fires an EventIsMovementKeyPressed event when it is checked if a keybinding is pressed.
     * The return value of the isPressed method is then replaced by the return value of the event.
     * @param instance The key binding that is checked.
     * @return The return value of the event.
     */
    @WrapOperation(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;isPressed()Z"))
    private boolean onTickMovement(KeyBinding instance, Operation<Boolean> original) {
        EventIsMovementKeyPressed event = new EventIsMovementKeyPressed(instance);
        event.fire();
        return event.isPressed();
    }
}
