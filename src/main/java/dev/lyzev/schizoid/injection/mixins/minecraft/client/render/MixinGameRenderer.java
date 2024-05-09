/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.injection.mixins.minecraft.client.render;

import dev.lyzev.api.events.EventRenderWorld;
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

    /**
     * This method is a mixin for the renderWorld method of the GameRenderer class.
     * It creates and fires an EventRenderWorld event after the world is rendered.
     * The event is fired after the render method of the WorldRenderer class is invoked.
     *
     * @param tickDelta The time difference between the last and the current frame.
     * @param limitTime The maximum time that the method is allowed to run.
     * @param matrices The matrix stack used for transformations.
     * @param ci The callback information.
     */
    @Inject(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;render(FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V", shift = At.Shift.AFTER))
    private void onRenderWorld(float tickDelta, long limitTime, CallbackInfo ci) {
        new EventRenderWorld(tickDelta, limitTime).fire();
    }

    @Inject(method = "renderBlur", at = @At("HEAD"), cancellable = true)
    private void onRenderBlur(float delta, CallbackInfo ci) {
        ci.cancel();
    }
}
