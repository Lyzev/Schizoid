/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.events

import dev.lyzev.api.imgui.ImGuiScreen
import net.minecraft.network.packet.Packet

/**
 * This event is triggered during the startup phase of the application.
 */
object EventStartup : Event

/**
 * This event is triggered during the shutdown phase of the application.
 */
object EventShutdown : Event

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
