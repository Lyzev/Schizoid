/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.injection.mixins.minecraft.client;

import dev.lyzev.api.events.EventGetMouseSensitivity;
import dev.lyzev.api.events.EventIsCursorLocked;
import dev.lyzev.api.events.EventMouseClick;
import dev.lyzev.api.events.EventUpdateMouse;
import net.minecraft.client.Mouse;
import net.minecraft.client.option.SimpleOption;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
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
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Mouse;isCursorLocked()Z"))
    private boolean onTick(Mouse instance) {
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

    /**
     * This method is a mixin for the updateMouse method of the Mouse class.
     * It creates and fires an EventGetMouseSensitivity event when the mouse is updated.
     * This event allows for modifying the sensitivity without changing the actual value.
     * @param instance The Minecraft option instance.
     */
    @Redirect(method = "updateMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/SimpleOption;getValue()Ljava/lang/Object;", ordinal = 0))
    private Object onUpdateMouseGetSensitivity(SimpleOption<Double> instance) {
        EventGetMouseSensitivity event = new EventGetMouseSensitivity(instance.getValue());
        event.fire();
        return event.getSensitivity();
    }

}
