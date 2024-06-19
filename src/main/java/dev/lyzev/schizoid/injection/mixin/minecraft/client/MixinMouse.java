/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.injection.mixin.minecraft.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.lyzev.api.events.EventIsCursorLocked;
import dev.lyzev.api.events.EventMouseClick;
import dev.lyzev.api.events.EventMouseScroll;
import dev.lyzev.api.events.EventUpdateMouse;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This class provides a mixin for the Mouse class in the Minecraft client package.
 * It modifies the behavior of the onMouseButton method of the Mouse class.
 */
@Mixin(Mouse.class)
public class MixinMouse {

    /**
     * This method is a mixin for the onMouseButton method of the Mouse class.
     * It creates and fires an EventMouseClick event when a mouse button is clicked.
     * The event is fired after the original method has been executed.
     *
     * @param window The window where the mouse button was clicked.
     * @param button The button that was clicked.
     * @param action The action that was performed.
     * @param mods   The modifier keys that were held down when the button was clicked.
     * @param ci     The callback information.
     */
    @Inject(method = "onMouseButton", at = @At("HEAD"))
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        if (button != GLFW.GLFW_KEY_UNKNOWN && window == MinecraftClient.getInstance().getWindow().getHandle())
            new EventMouseClick(window, button, action, mods).fire();
    }

    /**
     * This method is a redirect for the isCursorLocked method of the Mouse class.
     * It creates and fires an EventIsCursorLocked event when it is checked if the cursor is locked.
     * The return value of the isCursorLocked method is then replaced by the return value of the event.
     *
     * @param instance The Mouse instance.
     * @return The return value of the event.
     */
    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Mouse;isCursorLocked()Z"))
    private boolean onTick(Mouse instance, Operation<Boolean> original) {
        EventIsCursorLocked event = new EventIsCursorLocked(original.call(instance));
        event.fire();
        return event.isCursorLocked();
    }

    /**
     * This method is a mixin for the updateMouse method of the Mouse class.
     * It creates and fires an EventUpdateMouse event when the mouse is updated.
     * The event is fired at the start of the method.
     *
     * @param ci The callback information.
     */
    @Inject(method = "updateMouse", at = @At("HEAD"))
    private void onUpdateMouse2(CallbackInfo ci) {
        EventUpdateMouse.INSTANCE.fire();
    }

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void onScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (window == MinecraftClient.getInstance().getWindow().getHandle()) {
            EventMouseScroll event = new EventMouseScroll(horizontal, vertical);
            event.fire();
            if (event.isCancelled()) {
                ci.cancel();
            }
        }
    }
}
