/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.injection.mixins.minecraft.client;

import dev.lyzev.api.events.EventItemUse;
import dev.lyzev.api.events.EventRenderImGui;
import dev.lyzev.api.events.EventStartup;
import dev.lyzev.api.events.EventWindowResize;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This class provides mixins for the MinecraftClient class in the Minecraft client package.
 * It modifies the behavior of the run, onResolutionChanged, render, and doItemUse methods of the MinecraftClient class.
 */
@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {

    /**
     * This method is a mixin for the run method of the MinecraftClient class.
     * It fires an EventStartup event at the start of the method.
     *
     * @param ci The callback information.
     */
    @Inject(method = "run", at = @At("HEAD"))
    private void onInit(CallbackInfo ci) {
        EventStartup.INSTANCE.fire();
    }

    /**
     * This method is a mixin for the onResolutionChanged method of the MinecraftClient class.
     * It fires an EventWindowResize event at the end of the method.
     *
     * @param ci The callback information.
     */
    @Inject(method = "onResolutionChanged", at = @At("TAIL"))
    private void onResolutionChanged(CallbackInfo ci) {
        EventWindowResize.INSTANCE.fire();
    }

    /**
     * This method is a mixin for the render method of the MinecraftClient class.
     * It fires an EventRenderImGui event before the endWrite method of the Framebuffer class is invoked.
     *
     * @param ci The callback information.
     */
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/Framebuffer;endWrite()V", shift = At.Shift.BEFORE))
    private void onRender(CallbackInfo ci) {
        EventRenderImGui.INSTANCE.fire();
    }

    /**
     * This method is a mixin for the doItemUse method of the MinecraftClient class.
     * It creates and fires an EventItemUse event before the isRiding method of the ClientPlayerEntity class is invoked.
     * The item use cooldown of the MinecraftClient instance is then replaced by the item use cooldown of the event.
     *
     * @param ci The callback information.
     */
    @Inject(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isRiding()Z", shift = At.Shift.BEFORE))
    private void onItemUse(CallbackInfo ci) {
        EventItemUse fire = new EventItemUse(((MinecraftClient) (Object) this).itemUseCooldown);
        fire.fire();
        ((MinecraftClient) (Object) this).itemUseCooldown = fire.getItemUseCooldown();
    }
}
