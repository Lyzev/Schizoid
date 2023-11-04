/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.mixins.imgui;

import dev.lyzev.api.imgui.ImGuiLoader;
import imgui.glfw.ImGuiImplGlfw;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This mixin is used to implement smooth scrolling in ImGui.
 */
@Mixin(ImGuiImplGlfw.class)
public class MixinImGuiImplGlfw {

    @Shadow
    private GLFWScrollCallback prevUserCallbackScroll;

    @Shadow
    private long windowPtr;

    /**
     * This method is used to implement smooth scrolling in ImGui.
     */
    @Inject(method = "scrollCallback", at = @At("HEAD"), cancellable = true, remap = false)
    public void injectScrollCallback(long windowId, double xOffset, double yOffset, CallbackInfo ci) {
        if (prevUserCallbackScroll != null && windowId == windowPtr) {
            prevUserCallbackScroll.invoke(windowId, xOffset, yOffset);
        }

        // Update target scroll position
        ImGuiLoader.INSTANCE.setTargetScrollX((float) (ImGuiLoader.INSTANCE.getTargetScrollX() + xOffset));
        ImGuiLoader.INSTANCE.setTargetScrollY((float) (ImGuiLoader.INSTANCE.getTargetScrollY() + yOffset));

        ci.cancel();
    }
}
