/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.imgui

import dev.lyzev.api.events.*
import dev.lyzev.api.imgui.font.ImGuiFonts
import dev.lyzev.api.imgui.render.ImGuiRenderer
import dev.lyzev.schizoid.Schizoid
import dev.lyzev.schizoid.feature.features.gui.guis.ImGuiScreenFeature
import imgui.*
import imgui.ImGui.*
import imgui.extension.implot.ImPlot
import imgui.flag.ImGuiCol.*
import imgui.flag.ImGuiConfigFlags.*
import imgui.gl3.ImGuiImplGl3
import imgui.glfw.ImGuiImplGlfw
import org.lwjgl.glfw.GLFW.*
import java.io.IOException


/**
 * This class is responsible for loading ImGui.
 */
object ImGuiLoader : EventListener {

    // ImGui instances
    val glfw = ImGuiImplGlfw()
    val gl3 = ImGuiImplGl3()

    var canRender = false

    /**
     * Sets up ImGui.
     */
    private fun initImGui() {
        createContext()
        ImPlot.createContext()
        val io = getIO()
        io.iniFilename = Schizoid.root.path + "/imgui.ini"
        io.addConfigFlags(NavEnableKeyboard)
        io.addConfigFlags(DockingEnable)
        io.addConfigFlags(NoMouseCursorChange)
        initFonts()
        ImGuiScreenFeature.colorScheme.applyStyle(ImGuiScreenFeature.mode)
        ImGuiScreenFeature.colorScheme.applyColors(ImGuiScreenFeature.mode)
    }

    /**
     * Initializes fonts.
     */
    private fun initFonts() {
        runCatching {
            for (font in ImGuiFonts.entries)
                requireNotNull(font.font) { "Failed loading font: ${font.fontName}.${font.type.name.lowercase()}" }
        }.onFailure {
            Schizoid.logger.error("Failed loading fonts: ${it.message}")
        }.onSuccess {
            Schizoid.logger.info("Successfully loaded fonts.")
        }
    }

    // Indicates whether the ImGui loader should handle events.
    override val shouldHandleEvents = true

    init {

        /**
         * Sets up ImGui during the startup event.
         * @see setup
         * @param E The [GlfwInitEvent] triggered during application startup.
         */
        on<EventGlfwInit> { event ->
            initImGui()
            glfw.init(event.handle, true)
            gl3.init()
            ImGuiRenderer // Initialize the renderer
        }
    }
}
