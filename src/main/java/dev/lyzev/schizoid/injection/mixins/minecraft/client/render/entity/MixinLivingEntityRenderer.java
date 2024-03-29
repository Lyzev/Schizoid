/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.injection.mixins.minecraft.client.render.entity;

import dev.lyzev.api.events.EventRenderModel;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

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
     * @param instance The instance of the EntityModel class.
     * @param matrixStack The matrix stack used for transformations.
     * @param vertexConsumer The vertex consumer used for rendering.
     * @param light The light level for the render.
     * @param overlay The overlay level for the render.
     * @param red The red color component for the render.
     * @param green The green color component for the render.
     * @param blue The blue color component for the render.
     * @param alpha The alpha component for the render.
     */
    @Redirect(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V"))
    private void onRenderModel(EntityModel instance, MatrixStack matrixStack, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
        EventRenderModel event = new EventRenderModel(instance, matrixStack, vertexConsumer, light, overlay, red, green, blue, alpha);
        event.fire();
        instance.render(event.getMatrixStack(), event.getVertexConsumer(), event.getLight(), event.getOverlay(), event.getRed(), event.getGreen(), event.getBlue(), event.getAlpha());
    }
}
