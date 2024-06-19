/*
 * Copyright (c) 2023-2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.injection.mixin.minecraft.network;

import dev.lyzev.api.events.EventPacket;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This class provides mixins for the ClientConnection class in the Minecraft network package.
 * It injects custom behavior into the sendImmediately and handlePacket methods of the ClientConnection class.
 */
@Mixin(ClientConnection.class)
public class MixinClientConnection {

    /**
     * This method is a mixin for the handlePacket method of the ClientConnection class.
     * It creates and fires an EventReceivePacket event before the packet is handled.
     * If the event is cancelled, it cancels the handling of the packet.
     *
     * @param packet   The packet to be handled.
     * @param listener The listener for the packet.
     * @param ci       The callback information.
     */
    @Inject(method = "handlePacket", at = @At("HEAD"), cancellable = true)
    private static void onHandlePacket(Packet<?> packet, PacketListener listener, CallbackInfo ci) {
        if (EventPacket.Companion.getAllowTrigger()) {
            EventPacket event = new EventPacket(packet, EventPacket.Type.S2C);
            event.fire();
            if (event.isCancelled())
                ci.cancel();
        }
    }

    /**
     * This method is a mixin for the sendImmediately method of the ClientConnection class.
     * It creates and fires an EventSendPacket event before the packet is sent.
     * If the event is cancelled, it cancels the sending of the packet.
     *
     * @param packet    The packet to be sent.
     * @param callbacks The callbacks to be used when the packet is sent.
     * @param flush     Whether to flush the packet immediately.
     * @param ci        The callback information.
     */
    @Inject(method = "sendImmediately", at = @At("HEAD"), cancellable = true)
    private void onSendImmediately(Packet<?> packet, @Nullable PacketCallbacks callbacks, boolean flush, CallbackInfo ci) {
        if (EventPacket.Companion.getAllowTrigger()) {
            EventPacket event = new EventPacket(packet, EventPacket.Type.C2S);
            event.fire();
            if (event.isCancelled())
                ci.cancel();
        }
    }
}
