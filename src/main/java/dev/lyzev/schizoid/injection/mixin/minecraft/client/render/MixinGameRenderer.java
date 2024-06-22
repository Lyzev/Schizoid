/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.injection.mixin.minecraft.client.render;

import dev.lyzev.api.events.EventGetFOV;
import dev.lyzev.api.events.EventUpdateCrosshairTarget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {

    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void onGetFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> cir) {
        EventGetFOV event = new EventGetFOV(cir.getReturnValue());
        event.fire();
        cir.setReturnValue(event.getFov());
    }

    @Unique
    private float cachedYaw;
    @Unique
    private float cachedPitch;
    @Unique
    private float cachedPrevYaw;
    @Unique
    private float cachedPrevPitch;

    @Inject(method = "updateCrosshairTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/lang/String;)V", shift = At.Shift.BEFORE, ordinal = 0))
    private void onUpdateCrosshairTargetPre(float tickDelta, CallbackInfo ci) {
        Entity camera = client.getCameraEntity();
        cachedYaw = camera.getYaw();
        cachedPitch = camera.getPitch();
        cachedPrevYaw = camera.prevYaw;
        cachedPrevPitch = camera.prevPitch;
        EventUpdateCrosshairTarget event = new EventUpdateCrosshairTarget(camera);
        event.fire();
    }

    @Inject(method = "updateCrosshairTarget", at = @At("RETURN"))
    private void onUpdateCrosshairTargetPost(float tickDelta, CallbackInfo ci) {
        Entity camera = client.getCameraEntity();
        camera.setYaw(cachedYaw);
        camera.setPitch(cachedPitch);
        camera.prevYaw = cachedPrevYaw;
        camera.prevPitch = cachedPrevPitch;
    }
}
