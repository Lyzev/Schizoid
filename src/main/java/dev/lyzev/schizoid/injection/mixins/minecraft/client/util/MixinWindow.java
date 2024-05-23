/*
 * Copyright (c) 2023-2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.injection.mixins.minecraft.client.util;

import dev.lyzev.api.events.EventGlfwInit;
import dev.lyzev.api.events.EventSwapBuffers;
import net.minecraft.client.WindowEventHandler;
import net.minecraft.client.WindowSettings;
import net.minecraft.client.util.MonitorTracker;
import net.minecraft.client.util.Window;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL44;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.lwjgl.glfw.GLFW.GLFW_SAMPLES;

/**
 * This class provides a mixin for the Window class in the Minecraft client util package.
 * It modifies the behavior of the setPhase method of the Window class.
 */
@Mixin(Window.class)
public class MixinWindow {

    @Shadow
    @Final
    private long handle;

    /**
     * This method is a mixin for the setPhase method of the Window class.
     * It creates and fires an EventGlfwInit event when the phase is set to "Post startup".
     * The event is fired at the start of the method.
     *
     * @param phase The phase that is being set.
     * @param ci    The callback information.
     */
    @Inject(method = "setPhase", at = @At("HEAD"))
    private void onSetPhase(String phase, CallbackInfo ci) {
        if (phase.equals("Post startup"))
            new EventGlfwInit(handle).fire();
    }

    @Inject(method = "swapBuffers", at = @At("HEAD"))
    private void onSwapBuffers(CallbackInfo ci) {
        EventSwapBuffers.INSTANCE.fire();
    }
}
