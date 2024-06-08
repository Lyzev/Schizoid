/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.injection.mixins.minecraft.client.input;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.lyzev.api.events.EventIsMovementKeyPressed;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * This class provides a mixin for the KeyboardInput class in the Minecraft client input package.
 * It modifies the behavior of the tick method of the KeyboardInput class.
 */
@Mixin(KeyboardInput.class)
public class MixinKeyboardInput {

    /**
     * This method is a redirect for the isPressed method of the KeyBinding class.
     * It creates and fires an EventKeyboardInputPressed event when a key is pressed.
     * The return value of the isPressed method is then replaced by the return value of the event.
     *
     * @param instance The key binding that was pressed.
     * @return The return value of the event.
     */
    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;isPressed()Z"))
    private boolean onTick(KeyBinding instance, Operation<Boolean> original) {
        EventIsMovementKeyPressed event = new EventIsMovementKeyPressed(instance);
        event.fire();
        return event.isPressed();
    }
}
