/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.events

import dev.lyzev.schizoid.Schizoid
import dev.lyzev.schizoid.feature.features.gui.ImGuiScreen
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.network.packet.Packet

/**
 * This event is triggered during the startup phase of the application.
 */
object EventStartup : Event

/**
 * This event is triggered during the shutdown phase of the application.
 */
object EventShutdown : Event {

    override fun fire() {
        Schizoid.logger.info("Shutting down the client...")
        super.fire()
    }
}

/**
 * This event is triggered when GLFW is initialized.
 */
class EventGlfwInit(val handle: Long) : Event

/**
 * This event is triggered when ImGui starts rendering a screen.
 */
class EventRenderImGuiScreen(val screen: ImGuiScreen) : Event

/**
 * This event is triggered when the application is about to render ImGui.
 */
object EventPreRenderImGui : Event

/**
 * This event is triggered when the application has finished rendering ImGui.
 */
object EventPostRenderImGui : Event

class EventSendPacket(val packet: Packet<*>) : CancellableEvent()
class EventReceivePacket(val packet: Packet<*>) : CancellableEvent()

class EventKeystroke(val window: Long, val key: Int, val scancode: Int, val action: Int, val modifiers: Int) : Event

class EventMouseClick(val window: Long, val button: Int, val action: Int, val mods: Int) : Event

object EventKeybindsRequest : Event
class EventKeybindsResponse(val key: Int) : Event

object EventWindowResize : Event

class EventClientPlayerEntityTick(val player: ClientPlayerEntity) : Event

class EventRenderWorld(val tickDelta: Float, val limitTime: Long, val matrices: MatrixStack) : Event
