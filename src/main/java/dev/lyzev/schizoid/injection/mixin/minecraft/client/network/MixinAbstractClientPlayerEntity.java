/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.injection.mixin.minecraft.client.network;

import dev.lyzev.api.events.EventClientPlayerEntityIsSpectator;
import dev.lyzev.schizoid.Schizoid;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public class MixinAbstractClientPlayerEntity {

    @Inject(method = "isSpectator", at = @At("RETURN"), cancellable = true)
    private void onIsSpectator(CallbackInfoReturnable<Boolean> cir) {
        if (Schizoid.INSTANCE.getMc().player != (Object) this) {
            return;
        }
        EventClientPlayerEntityIsSpectator event = new EventClientPlayerEntityIsSpectator((ClientPlayerEntity) (Object) this, cir.getReturnValue());
        event.fire();
        cir.setReturnValue(event.isSpectator());
    }
}
