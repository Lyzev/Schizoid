/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.injection.mixins.minecraft.client;

import dev.lyzev.api.events.EventKeystroke;
import dev.lyzev.api.events.EventMouseClick;
import net.minecraft.client.Mouse;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MixinMouse {

    @Inject(method = "onMouseButton", at = @At("TAIL"))
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        if (button != GLFW.GLFW_KEY_UNKNOWN)
            new EventMouseClick(window, button, action, mods).fire();
    }
}
