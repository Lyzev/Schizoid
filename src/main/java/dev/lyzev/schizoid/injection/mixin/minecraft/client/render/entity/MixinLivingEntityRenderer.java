/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.injection.mixin.minecraft.client.render.entity;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import dev.lyzev.api.events.EventClientPlayerEntityRender;
import dev.lyzev.api.events.EventRenderModel;
import dev.lyzev.schizoid.Schizoid;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This class provides a mixin for the LivingEntityRenderer class in the Minecraft client render entity package.
 * It modifies the behavior of the render method of the LivingEntityRenderer class.
 */
@Mixin(LivingEntityRenderer.class)
public class MixinLivingEntityRenderer {

    /**
     * This method is a redirect for the render method of the EntityModel class.
     * It creates and fires an EventRenderModel event when the model is rendered.
     * The parameters of the render method are then replaced by the parameters of the event.
     *
     * @param instance       The instance of the EntityModel class.
     * @param matrixStack    The matrix stack used for transformations.
     * @param vertexConsumer The vertex consumer used for rendering.
     * @param light          The light level for the render.
     * @param overlay        The overlay level for the render.
     * @param argb           The color of the render.
     * @return
     */
    @WrapWithCondition(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V"))
    private boolean onRenderModel(EntityModel instance, MatrixStack matrixStack, VertexConsumer vertexConsumer, int light, int overlay, int argb) {
        EventRenderModel event = new EventRenderModel(instance, matrixStack, vertexConsumer, light, overlay, argb);
        event.fire();
        instance.render(event.getMatrixStack(), event.getVertexConsumer(), event.getLight(), event.getOverlay(), event.getArgb());
        return false;
    }

    @Unique
    private float pitch;
    @Unique
    private float prevPitch;

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"))
    private void onRenderPre(LivingEntity livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (!livingEntity.equals(Schizoid.INSTANCE.getMc().player)) {
            return;
        }
        pitch = livingEntity.getPitch();
        prevPitch = livingEntity.prevPitch;
        EventClientPlayerEntityRender event = new EventClientPlayerEntityRender(livingEntity.headYaw, livingEntity.prevHeadYaw, pitch, prevPitch);
        event.fire();
        livingEntity.setHeadYaw(event.getHeadYaw());
        livingEntity.prevHeadYaw = event.getPrevHeadYaw();
        livingEntity.setPitch(event.getPitch());
        livingEntity.prevPitch = event.getPrevPitch();
    }

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("RETURN"))
    private void onRenderPost(LivingEntity livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (!livingEntity.equals(Schizoid.INSTANCE.getMc().player)) {
            return;
        }
        livingEntity.setPitch(pitch);
        livingEntity.prevPitch = prevPitch;
    }
}
