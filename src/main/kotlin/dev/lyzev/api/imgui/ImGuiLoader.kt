/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.imgui

import dev.lyzev.api.events.*
import dev.lyzev.schizoid.Schizoid
import imgui.ImFontConfig
import imgui.ImGui.*
import imgui.flag.ImGuiCol.*
import imgui.flag.ImGuiConfigFlags.*
import imgui.gl3.ImGuiImplGl3
import imgui.glfw.ImGuiImplGlfw
import org.lwjgl.glfw.GLFW.*
import java.io.IOException
import kotlin.math.max
import kotlin.math.min

/**
 * This class is responsible for loading ImGui.
 */
object ImGuiLoader : EventListener {

    // ImGui instances
    private val imGuiGlfw = ImGuiImplGlfw()
    private val imGuiGl3 = ImGuiImplGl3()

    // Scroll positions
    var targetScrollX = 0f
    var targetScrollY = 0f

    /**
     * Renders ImGui.
     */
    private fun renderImGui() {
        imGuiGl3.renderDrawData(getDrawData())
        if (getIO().hasConfigFlags(ViewportsEnable)) {
            val backupWindowPtr = glfwGetCurrentContext()
            updatePlatformWindows()
            renderPlatformWindowsDefault()
            glfwMakeContextCurrent(backupWindowPtr)
        }
    }

    /**
     * Sets up ImGui.
     */
    private fun setup() {
        createContext()
        val io = getIO()
        io.iniFilename = Schizoid.root.path + "/imgui.ini"
        io.addConfigFlags(NavEnableKeyboard)
        io.configViewportsNoTaskBarIcon = true
        val fontAtlas = io.fonts
        val fontConfig = ImFontConfig()
        fontConfig.glyphRanges = fontAtlas.glyphRangesCyrillic
        try {
            javaClass.classLoader.getResourceAsStream("assets/${Schizoid.MOD_ID}/fonts/JetBrainsMono-Bold.ttf")?.use {
                fontAtlas.addFontFromMemoryTTF(it.readAllBytes(), 17.5f)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        fontConfig.mergeMode = true
        fontConfig.pixelSnapH = true
        fontConfig.destroy()
        style(0.65f, true)
    }

    /**
     * Styles ImGui.
     */
    @Suppress("SameParameterValue")
    private fun style(alpha: Float, darkMode: Boolean) {
        // Blue: 0.01f, 0.01f, 0.01f
        val style = getStyle()
        style.alpha = 1f
        style.frameRounding = 3f
        style.setWindowTitleAlign(0.5f, 0.5f)
        style.setColor(Text, 0f, 0f, 0f, 1f)
        style.setColor(TextDisabled, 0.60f, 0.60f, 0.60f, 1f)
        style.setColor(WindowBg, 0.94f, 0.94f, 0.94f, 0.7f)
        style.setColor(ChildBg, 0f, 0f, 0f, 0f)
        style.setColor(PopupBg, 1f, 1f, 1f, 0.94f)
        style.setColor(Border, 0f, 0f, 0f, 0f)
        style.setColor(BorderShadow, 1f, 1f, 1f, 0f)
        style.setColor(FrameBg, 1f, 1f, 1f, 0.94f)
        style.setColor(FrameBgHovered, 0.01f, 0.01f, 0.01f, 0.40f)
        style.setColor(FrameBgActive, 0.01f, 0.01f, 0.01f, 0.4f)
        style.setColor(TitleBg, 0.82f, 0.82f, 0.82f, 0.9f)
        style.setColor(TitleBgCollapsed, 1f, 1f, 1f, 0.51f)
        style.setColor(TitleBgActive, 0.76f, 0.76f, 0.76f, 0.9f)
        style.setColor(MenuBarBg, 0.86f, 0.86f, 0.86f, 1f)
        style.setColor(ScrollbarBg, 0.98f, 0.98f, 0.98f, 0.2f)
        style.setColor(ScrollbarGrab, 0.69f, 0.69f, 0.69f, 0.3f)
        style.setColor(ScrollbarGrabHovered, 0.59f, 0.59f, 0.59f, 0.3f)
        style.setColor(ScrollbarGrabActive, 0.49f, 0.49f, 0.49f, 0.3f)
        style.setColor(CheckMark, 0.01f, 0.01f, 0.01f, 1f)
        style.setColor(SliderGrab, 0.1f, 0.1f, 0.1f, 1f)
        style.setColor(SliderGrabActive, 0.01f, 0.01f, 0.01f, 1f)
        style.setColor(Button, 0.01f, 0.01f, 0.01f, 0.40f)
        style.setColor(ButtonHovered, 0.8f, 0.8f, 0.8f, 1f)
        style.setColor(ButtonActive, 0.06f, 0.53f, 0.98f, 1f)
        style.setColor(Header, 0.01f, 0.01f, 0.01f, 0.31f)
        style.setColor(HeaderHovered, 0.01f, 0.01f, 0.01f, 0.80f)
        style.setColor(HeaderActive, 0.01f, 0.01f, 0.01f, 1f)
        style.setColor(Tab, 0.39f, 0.39f, 0.39f, 1f)
        style.setColor(TabHovered, 0.01f, 0.01f, 0.01f, 0.78f)
        style.setColor(TabActive, 0.01f, 0.01f, 0.01f, 1f)
        style.setColor(ResizeGrip, 1f, 1f, 1f, 0.50f)
        style.setColor(ResizeGripHovered, 0.01f, 0.01f, 0.01f, 0.67f)
        style.setColor(ResizeGripActive, 0.01f, 0.01f, 0.01f, 0.95f)
        style.setColor(PlotLines, 0.39f, 0.39f, 0.39f, 1f)
        style.setColor(PlotLinesHovered, 1f, 0.43f, 0.35f, 1f)
        style.setColor(PlotHistogram, 0.90f, 0.70f, 0f, 1f)
        style.setColor(PlotHistogramHovered, 1f, 0.60f, 0f, 1f)
        style.setColor(TextSelectedBg, 0.01f, 0.01f, 0.01f, 0.35f)
        style.setColor(ModalWindowDimBg, 0.20f, 0.20f, 0.20f, 0.35f)
        if (darkMode) {
            for (i in 0..COUNT) {
                val col = style.getColor(i)
                val hsv = FloatArray(3)
                colorConvertRGBtoHSV(floatArrayOf(col.x, col.y, col.z), hsv)
                if (hsv[1] < 0.1f) hsv[2] = 1f - hsv[2]
                val rgb = FloatArray(3)
                colorConvertHSVtoRGB(hsv, rgb)
                if (col.w < 1f) col.w *= alpha
                style.setColor(i, rgb[0], rgb[1], rgb[2], col.w)
            }
        } else {
            for (i in 0..COUNT) {
                val col = style.getColor(i)
                if (col.w < 1f) {
                    col.x *= alpha
                    col.y *= alpha
                    col.z *= alpha
                    col.w *= alpha
                }
                style.setColor(i, col.x, col.y, col.z, col.w)
            }
        }
    }

    /**
     * Smoothly scrolls ImGui.
     */
    private fun smoothScroll() {
        // Calculate interpolation factors for scrolling
        val scrollXSpeed = if (targetScrollX > 0) {
            min(max(targetScrollX / 10f, .01f), targetScrollX)
        } else if (targetScrollX < 0) {
            max(min(targetScrollX / 10f, -.01f), targetScrollX)
        } else {
            0f
        }

        val scrollYSpeed = if (targetScrollY > 0) {
            min(max(targetScrollY / 10f, .01f), targetScrollY)
        } else if (targetScrollY < 0) {
            max(min(targetScrollY / 10f, -.01f), targetScrollY)
        } else {
            0f
        }

        targetScrollX -= scrollXSpeed
        targetScrollY -= scrollYSpeed

        // Update ImGui IO scroll positions
        getIO().mouseWheelH += scrollXSpeed
        getIO().mouseWheel += scrollYSpeed
    }

    // Indicates whether the ImGui loader should handle events.
    override val shouldHandleEvents = true

    init {

        /**
         * Sets up ImGui during the startup event.
         * @see setup
         * @param E The [GlfwInitEvent] triggered during application startup.
         */
        on<GlfwInitEvent> {
            setup()
            imGuiGlfw.init(it.handle, true)
            imGuiGl3.init()
        }

        /**
         * Renders ImGui during the render event.
         *
         * @param E The [RenderImGuiScreenEvent] triggered during application render.
         */
        on<RenderImGuiScreenEvent> {
            smoothScroll()

            imGuiGlfw.newFrame()
            newFrame()
            it.screen.renderImGui()
            render()
            RenderImGuiPreEvent.fire()
            renderImGui()
            RenderImGuiPostEvent.fire()
        }
    }
}
