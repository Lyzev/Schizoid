/*
 * Copyright (c) 2023-2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.injection.mixin.imgui;

import dev.lyzev.api.imgui.render.ImGuiRenderer;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.glfw.ImGuiImplGlfw;
import org.spongepowered.asm.mixin.Final;
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
    @Final
    private long[] keyOwnerWindows;

    @Shadow
    @Final
    private boolean[] mouseJustPressed;

    /**
     * This method is a mixin for the scrollCallback method of the ImGuiImplGlfw class.
     * It invokes the previous user callback for the scroll event if it exists and the window ID matches the window pointer.
     * It then updates the target scroll position in the ImGuiRenderer instance.
     * The method is cancelled after the target scroll position is updated.
     *
     * @param windowId The ID of the window.
     * @param xOffset  The offset in the x direction.
     * @param yOffset  The offset in the y direction.
     * @param ci       The callback information.
     */
    @Inject(method = "scrollCallback", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFWScrollCallback;invoke(JDD)V", shift = At.Shift.AFTER, ordinal = 0), cancellable = true, remap = false)
    private void onScrollCallback(long windowId, double xOffset, double yOffset, CallbackInfo ci) {
        // Update target scroll position
        ImGuiRenderer.INSTANCE.setTargetScrollX((float) (ImGuiRenderer.INSTANCE.getTargetScrollX() + xOffset));
        ImGuiRenderer.INSTANCE.setTargetScrollY((float) (ImGuiRenderer.INSTANCE.getTargetScrollY() + yOffset));
        ci.cancel();
    }

    /**
     * This method is a mixin for the keyCallback method of the ImGuiImplGlfw class.
     * It cancels the method if the ImGui IO does not want to capture the keyboard.
     *
     * @param windowId The ID of the window.
     * @param key      The key.
     * @param scancode The scancode.
     * @param action   The action.
     * @param mods     The mods.
     * @param ci       The callback information.
     */
    @Inject(method = "keyCallback", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFWKeyCallback;invoke(JIIII)V", shift = At.Shift.AFTER, ordinal = 0), cancellable = true, remap = false)
    private void onKeyCallback(long windowId, int key, int scancode, int action, int mods, CallbackInfo ci) {
        if (!ImGui.getIO().getWantCaptureKeyboard()) {
            final ImGuiIO io = ImGui.getIO();

            if (key >= 0 && key < keyOwnerWindows.length) {
                io.setKeysDown(key, false);
                keyOwnerWindows[key] = 0;
            }

            io.setKeyCtrl(false);
            io.setKeyShift(false);
            io.setKeyAlt(false);
            io.setKeySuper(false);
            ci.cancel();
        }
    }

    /**
     * This method is a mixin for the charCallback method of the ImGuiImplGlfw class.
     * It cancels the method if the ImGui IO does not want to capture the mouse buttons.
     *
     * @param windowId The ID of the window.
     * @param button   The button.
     * @param action   The action.
     * @param mods     The mods.
     * @param ci       The callback information.
     */
    @Inject(method = "mouseButtonCallback", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFWMouseButtonCallback;invoke(JIII)V", shift = At.Shift.AFTER, ordinal = 0), cancellable = true, remap = false)
    private void onMouseButtonCallback(long windowId, int button, int action, int mods, CallbackInfo ci) {
        if (!ImGui.getIO().getWantCaptureMouse()) {
            if (button >= 0 && button < mouseJustPressed.length)
                mouseJustPressed[button] = false;
            ci.cancel();
        }
    }
}
