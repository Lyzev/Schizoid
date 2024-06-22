/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.injection.mixin.minecraft.client.render;

import dev.lyzev.api.events.EventRenderWorld;
import dev.lyzev.api.opengl.Render;
import net.minecraft.client.render.*;
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
}
