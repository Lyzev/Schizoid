/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.injection.mixins.minecraft.client;

import dev.lyzev.api.events.EventIsCursorLocked;
import dev.lyzev.api.events.EventMouseClick;
import dev.lyzev.api.events.EventUpdateMouse;
import net.minecraft.client.Mouse;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This class provides a mixin for the Mouse class in the Minecraft client package.
 * It modifies the behavior of the onMouseButton method of the Mouse class.
 */
@Mixin(Mouse.class)
public class MixinMouse {

    @Shadow
    private double cursorDeltaX;

    @Shadow
    private double cursorDeltaY;

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
    @Inject(method = "onMouseButton", at = @At("TAIL"))
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        if (button != GLFW.GLFW_KEY_UNKNOWN)
            new EventMouseClick(window, button, action, mods).fire();
    }

    /**
     * This method is a redirect for the isCursorLocked method of the Mouse class.
     * It creates and fires an EventIsCursorLocked event when it is checked if the cursor is locked.
     * The return value of the isCursorLocked method is then replaced by the return value of the event.
     * @param instance The Mouse instance.
     * @return The return value of the event.
     */
    @Redirect(method = "onCursorPos", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Mouse;isCursorLocked()Z"))
    private boolean onCursorPos(Mouse instance) {
        EventIsCursorLocked event = new EventIsCursorLocked(instance.isCursorLocked());
        event.fire();
        return event.isCursorLocked();
    }

    /**
     * This method is a redirect for the isCursorLocked method of the Mouse class.
     * It creates and fires an EventIsCursorLocked event when it is checked if the cursor is locked.
     * The return value of the isCursorLocked method is then replaced by the return value of the event.
     * @param instance The Mouse instance.
     * @return The return value of the event.
     */
    @Redirect(method = "updateMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Mouse;isCursorLocked()Z"))
    private boolean onUpdateMouse(Mouse instance) {
        EventIsCursorLocked event = new EventIsCursorLocked(instance.isCursorLocked());
        event.fire();
        return event.isCursorLocked();
    }

    /**
     * This method is a mixin for the updateMouse method of the Mouse class.
     * It creates and fires an EventUpdateMouse event when the mouse is updated.
     * The event is fired at the start of the method.
     * @param ci The callback information.
     */
    @Inject(method = "updateMouse", at = @At("HEAD"))
    private void onUpdateMouse2(CallbackInfo ci) {
        EventUpdateMouse.INSTANCE.fire();
    }
}
