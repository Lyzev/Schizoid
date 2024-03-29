/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.injection.mixins.minecraft.client.render;

import dev.lyzev.api.events.EventGamma;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * This class provides a mixin for the LightmapTextureManager class in the Minecraft client render package.
 * It modifies the behavior of the update method of the LightmapTextureManager class.
 */
@Mixin(LightmapTextureManager.class)
public class MixinLightmapTextureManager {

    /**
     * This method is a redirect for the getValue method of the SimpleOption class.
     * It creates and fires an EventGamma event when the gamma value is retrieved.
     * The gamma value that was retrieved is then replaced by the gamma value of the event.
     *
     * @param instance The instance of the SimpleOption class.
     * @return The gamma value of the event.
     */
    @SuppressWarnings("rawtypes")
    @Redirect(method = "update(F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/SimpleOption;getValue()Ljava/lang/Object;", ordinal = 1))
    private Object onUpdate(SimpleOption instance) {
        EventGamma event = new EventGamma((Double) instance.getValue());
        event.fire();
        return event.getGamma();
    }
}
