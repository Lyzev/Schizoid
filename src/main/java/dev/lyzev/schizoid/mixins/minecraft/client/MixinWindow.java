/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.mixins.minecraft.client;

import dev.lyzev.api.events.EventGlfwInit;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.lwjgl.glfw.GLFW.glfwWindowHint;

/**
 * This mixin is used to allow legacy OpenGL to be used and initialize ImGui.
 */
@Mixin(Window.class)
public class MixinWindow {

    @Shadow
    @Final
    private long handle;

    /**
     * This method is used to allow legacy OpenGL to be used.
     */
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwWindowHint(II)V", remap = false))
    public void downgradeGL(int hint, int value) {
        if (hint == 139267 && value == 2) {
            glfwWindowHint(hint, 1);
            return;
        }
        if (hint == 139272 && value == 204801)
            return;
        if (hint == 139270 && value == 1)
            return;
        glfwWindowHint(hint, value);
    }

    /**
     * This method is used to initialize ImGui.
     */
    @Inject(method = "setPhase", at = @At("HEAD"))
    private void onSetPhase(String phase, CallbackInfo ci) {
        if (phase.equals("Post startup"))
            new EventGlfwInit(handle).fire();
    }
}
