/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.injection.mixins.minecraft.client;

import dev.lyzev.api.events.EventKeystroke;
import net.minecraft.client.Keyboard;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This class provides a mixin for the Keyboard class in the Minecraft client package.
 * It modifies the behavior of the onKey method of the Keyboard class.
 */
@Mixin(Keyboard.class)
public class MixinKeyboard {

    /**
     * This method is a mixin for the onKey method of the Keyboard class.
     * It creates and fires an EventKeystroke event when a key is pressed.
     * The event is fired after the original method has been executed.
     *
     * @param window    The window where the key was pressed.
     * @param key       The key that was pressed.
     * @param scancode  The scancode of the key.
     * @param action    The action that was performed.
     * @param modifiers The modifier keys that were held down when the key was pressed.
     * @param ci        The callback information.
     */
    @Inject(method = "onKey", at = @At("TAIL"))
    private void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (key != GLFW.GLFW_KEY_UNKNOWN)
            new EventKeystroke(window, key, scancode, action, modifiers).fire();
    }
}
