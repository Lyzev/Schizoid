/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.injection.mixin.minecraft.client.render;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.lyzev.api.events.EventRenderEntity;
import dev.lyzev.api.events.EventRenderWorld;
import dev.lyzev.api.events.EventShouldRenderEntity;
import dev.lyzev.api.opengl.Render;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {

    @Inject(method = "render", at = @At("RETURN"))
    private void onRender(RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f modelViewMat, Matrix4f projMat, CallbackInfo ci) {
        Render.INSTANCE.store();
        Render.INSTANCE.prepare();
        new EventRenderWorld(tickCounter, modelViewMat, projMat).fire();
        Render.INSTANCE.post();
        Render.INSTANCE.restore();
    }

    @Inject(method = "renderEntity", at = @At("RETURN"), cancellable = true)
    private void onRender(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        if (MinecraftClient.getInstance().gameRenderer.isRenderingPanorama()) return;
        EventRenderEntity event = new EventRenderEntity(entity, cameraX, cameraY, cameraZ, tickDelta, matrices, vertexConsumers);
        event.fire();
        if (event.isCancelled()) ci.cancel();
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderDispatcher;shouldRender(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/render/Frustum;DDD)Z"))
    private boolean onShouldRender(EntityRenderDispatcher instance, Entity entity, Frustum frustum, double x, double y, double z, Operation<Boolean> original) {
        EventShouldRenderEntity event = new EventShouldRenderEntity(entity, original.call(instance, entity, frustum, x, y, z));
        event.fire();
        return event.getShouldRender();
    }
}
