/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.events

import dev.lyzev.api.imgui.ImGuiScreen

/**
 * This event is triggered during the startup phase of the application.
 */
object StartupEvent : Event

/**
 * This event is triggered during the shutdown phase of the application.
 */
object ShutdownEvent : Event

/**
 * This event is triggered when GLFW is initialized.
 */
class GlfwInitEvent(val handle: Long) : Event

/**
 * This event is triggered when ImGui starts rendering a screen.
 */
class RenderImGuiScreenEvent(val screen: ImGuiScreen) : Event

/**
 * This event is triggered when the application is about to render ImGui.
 */
object RenderImGuiPreEvent : Event

/**
 * This event is triggered when the application has finished rendering ImGui.
 */
object RenderImGuiPostEvent : Event
