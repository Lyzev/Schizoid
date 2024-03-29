/*
 * Copyright (c) 2023-2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.injection.mixins.imgui;

import dev.lyzev.api.imgui.render.ImGuiRenderer;
import imgui.glfw.ImGuiImplGlfw;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This class provides a mixin for the ImGuiImplGlfw class in the imgui glfw package.
 * It modifies the behavior of the scrollCallback method of the ImGuiImplGlfw class.
 */
@Mixin(value = ImGuiImplGlfw.class, remap = false)
public class MixinImGuiImplGlfw {

    @Shadow
    private GLFWScrollCallback prevUserCallbackScroll;

    @Shadow
    private long windowPtr;

    /**
     * This method is a mixin for the scrollCallback method of the ImGuiImplGlfw class.
     * It invokes the previous user callback for the scroll event if it exists and the window ID matches the window pointer.
     * It then updates the target scroll position in the ImGuiRenderer instance.
     * The method is cancelled after the target scroll position is updated.
     *
     * @param windowId The ID of the window.
     * @param xOffset The offset in the x direction.
     * @param yOffset The offset in the y direction.
     * @param ci The callback information.
     */
    @Inject(method = "scrollCallback", at = @At("HEAD"), cancellable = true, remap = false)
    public void onScrollCallback(long windowId, double xOffset, double yOffset, CallbackInfo ci) {
        if (prevUserCallbackScroll != null && windowId == windowPtr) {
            prevUserCallbackScroll.invoke(windowId, xOffset, yOffset);
        }

        // Update target scroll position
        ImGuiRenderer.INSTANCE.setTargetScrollX((float) (ImGuiRenderer.INSTANCE.getTargetScrollX() + xOffset));
        ImGuiRenderer.INSTANCE.setTargetScrollY((float) (ImGuiRenderer.INSTANCE.getTargetScrollY() + yOffset));

        ci.cancel();
    }
}
