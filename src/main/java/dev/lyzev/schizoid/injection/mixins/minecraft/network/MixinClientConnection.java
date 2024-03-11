/*
 * Copyright (c) 2023-2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.injection.mixins.minecraft.network;

import dev.lyzev.api.events.EventReceivePacket;
import dev.lyzev.api.events.EventSendPacket;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class MixinClientConnection {

    @Inject(method = "sendImmediately", at = @At("HEAD"), cancellable = true)
    private void onSendImmediately(Packet<?> packet, @Nullable PacketCallbacks callbacks, boolean flush, CallbackInfo ci) {
        EventSendPacket event = new EventSendPacket(packet);
        event.fire();
        if (event.isCancelled())
            ci.cancel();
    }

    @Inject(method = "handlePacket", at = @At("HEAD"), cancellable = true)
    private static void onHandlePacket(Packet<?> packet, PacketListener listener, CallbackInfo ci) {
        EventReceivePacket event = new EventReceivePacket(packet);
        event.fire();
        if (event.isCancelled())
            ci.cancel();
    }
}
