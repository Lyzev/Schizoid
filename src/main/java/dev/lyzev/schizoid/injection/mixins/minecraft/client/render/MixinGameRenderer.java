/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.injection.mixins.minecraft.client.render;

import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This class provides a mixin for the GameRenderer class in the Minecraft client render package.
 * It modifies the behavior of the renderWorld method of the GameRenderer class.
 */
@Mixin(GameRenderer.class)
public class MixinGameRenderer {

    @Inject(method = "renderBlur", at = @At("HEAD"), cancellable = true)
    private void onRenderBlur(float delta, CallbackInfo ci) {
        ci.cancel();
    }
}
