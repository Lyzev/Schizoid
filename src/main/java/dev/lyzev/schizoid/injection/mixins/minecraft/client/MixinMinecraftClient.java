/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.injection.mixins.minecraft.client;

import dev.lyzev.api.events.EventItemUse;
import dev.lyzev.api.events.EventReloadShader;
import dev.lyzev.api.events.EventStartup;
import dev.lyzev.api.events.EventWindowResize;
import dev.lyzev.schizoid.Schizoid;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

/**
 * This class provides mixins for the MinecraftClient class in the Minecraft client package.
 * It modifies the behavior of the constructor, run, onResolutionChanged, and render methods of the MinecraftClient class.
 */
@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {

    @Shadow
    public int itemUseCooldown;

    /**
     * This method is a mixin for the constructor of the MinecraftClient class.
     * It calls the onInitializeClient method of the Schizoid instance.
     * @param ci The callback information.
     */
    @Inject(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;runDirectory:Ljava/io/File;", shift = At.Shift.AFTER, ordinal = 0))
    private void onInitializeClient(CallbackInfo ci) {
        Schizoid.INSTANCE.onInitializeClient();
    }

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
     * This method is a mixin for the doItemUse method of the MinecraftClient class.
     * It creates and fires an EventItemUse event before the isRiding method of the ClientPlayerEntity class is invoked.
     * The item use cooldown of the MinecraftClient instance is then replaced by the item use cooldown of the event.
     *
     * @param ci The callback information.
     */
    @Inject(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isRiding()Z", shift = At.Shift.BEFORE))
    private void onItemUse(CallbackInfo ci) {
        EventItemUse event = new EventItemUse(this.itemUseCooldown);
        event.fire();
        this.itemUseCooldown = event.getItemUseCooldown();
    }

    @Inject(method = "reloadResources()Ljava/util/concurrent/CompletableFuture;", at = @At(value = "HEAD"))
    private void onReloadResources(CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        EventReloadShader.INSTANCE.fire();
    }

}
