/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.imgui

import dev.lyzev.api.events.*
import dev.lyzev.api.imgui.font.ImGuiFonts
import dev.lyzev.schizoid.Schizoid
import dev.lyzev.schizoid.feature.features.gui.guis.ImGuiScreenFeature
import imgui.*
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
    private fun initImGui() {
        createContext()
        val io = getIO()
        io.iniFilename = Schizoid.root.path + "/imgui.ini"
        io.addConfigFlags(NavEnableKeyboard)
        io.addConfigFlags(DockingEnable)
//        io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable);    // Enable Multi-Viewport / Platform Windows
//        io.configViewportsNoTaskBarIcon = true
        initFonts()
        initStyle(true)
    }

    /**
     * Initializes fonts.
     */
    private fun initFonts() {
        try {
            for (font in ImGuiFonts.entries)
                requireNotNull(font.font) { "Failed loading font: ${font.fontName}.${font.type.name.lowercase()}" }
        } catch (e: IOException) {
            Schizoid.logger.error("Failed to load fonts", e)
        }
    }

    /**
     * Styles ImGui.
     */
    private fun initStyle(darkMode: Boolean) {
        val style = getStyle()
        style.alpha = 1f
        style.antiAliasedFill = true
        style.antiAliasedLines = true
        style.antiAliasedLinesUseTex = false

        style.setWindowPadding(15f, 15f)
        style.windowRounding = 5f
        style.setFramePadding(5f, 5f)
        style.frameRounding = 4f
        style.setItemSpacing(12f, 8f)
        style.setItemInnerSpacing(8f, 6f)
        style.indentSpacing = 25f
        style.scrollbarSize = 12f
        style.scrollbarRounding = 9f
        style.grabMinSize = 5f
        style.grabRounding = 3f
        style.tabRounding = 4f
        style.childRounding = 4f

        style.setWindowTitleAlign(0.5f, 0.5f)

        if (darkMode) {
            style.setColor(Text, 1.0f, 1.0f, 1.0f, 1.0f)
            style.setColor(TextDisabled, 0.4f, 0.4f, 0.4f, 1.0f)
            style.setColor(WindowBg, 0.06f, 0.06f, 0.06f, 0.455f)
            style.setColor(ChildBg, 1.0f, 1.0f, 1.0f, 0.0f)
            style.setColor(PopupBg, 0.0f, 0.0f, 0.0f, 0.611f)
            style.setColor(Border, 1.0f, 1.0f, 1.0f, 0.0f)
            style.setColor(BorderShadow, 0.0f, 0.0f, 0.0f, 0.0f)
            style.setColor(FrameBg, 0.0f, 0.0f, 0.0f, 0.611f)
            style.setColor(FrameBgHovered, 0.99f, 0.99f, 0.99f, 0.26f)
            style.setColor(FrameBgActive, 0.99f, 0.99f, 0.99f, 0.26f)
            style.setColor(TitleBg, 0.18f, 0.18f, 0.18f, 0.585f)
            style.setColor(TitleBgActive, 0.24f, 0.24f, 0.24f, 0.585f)
            style.setColor(TitleBgCollapsed, 0.0f, 0.0f, 0.0f, 0.3315f)
            style.setColor(MenuBarBg, 0.14f, 0.14f, 0.14f, 1.0f)
            style.setColor(ScrollbarBg, 0.02f, 0.02f, 0.02f, 0.1f)
            style.setColor(ScrollbarGrab, 0.51f, 0.51f, 0.51f, 0.195f)
            style.setColor(ScrollbarGrabHovered, 0.41f, 0.41f, 0.41f, 0.195f)
            style.setColor(ScrollbarGrabActive, 0.31f, 0.31f, 0.31f, 0.195f)
            style.setColor(CheckMark, 1f, 0.3647f, 0f, 1f)
            style.setColor(SliderGrab, 1f, 0.3647f, 0f, 1.0f)
            style.setColor(SliderGrabActive, 0.75f, 0.29f, 0f, 1f)
            style.setColor(Button, 1f, 0.3647f, 0f, 1f)
            style.setColor(ButtonHovered, 0.85f, 0.31f, 0f, 1f)
            style.setColor(ButtonActive, 0.75f, 0.29f, 0f, 1f)
            style.setColor(Header, 0.99f, 0.99f, 0.99f, 0.2015f)
            style.setColor(HeaderHovered, 0.99f, 0.99f, 0.99f, 0.52f)
            style.setColor(HeaderActive, 0.99f, 0.99f, 0.99f, 1.0f)
            style.setColor(Separator, 0.43f, 0.43f, 0.5f, 0.325f)
            style.setColor(SeparatorHovered, 0.1f, 0.4f, 0.75f, 0.5f)
            style.setColor(SeparatorActive, 1f, 0.3647f, 0f, 1.0f)
            style.setColor(ResizeGrip, 0.51f, 0.51f, 0.51f, 0.195f)
            style.setColor(ResizeGripHovered, 0.41f, 0.41f, 0.41f, 0.195f)
            style.setColor(ResizeGripActive, 0.31f, 0.31f, 0.31f, 0.195f)
            style.setColor(Tab, 0.3f, 0.3f, 0.3f, 0.25f)
            style.setColor(TabHovered, 0.5f, 0.5f, 0.5f, 0.25f)
            style.setColor(TabActive, 0.5f, 0.5f, 0.5f, 0.25f)
            style.setColor(TabUnfocused, 0.3f, 0.3f, 0.3f, 0.25f)
            style.setColor(TabUnfocusedActive, 0.5f, 0.5f, 0.5f, 0.25f)
            style.setColor(DockingPreview, 1f, 0.3647f, 0f, 0.455f)
            style.setColor(DockingEmptyBg, 0.02f, 0.02f, 0.02f, 0.1f)
            style.setColor(PlotLines, 0.61f, 0.61f, 0.61f, 1.0f)
            style.setColor(PlotLinesHovered, 1.0f, 0.43f, 0.35000002f, 1.0f)
            style.setColor(PlotHistogram, 0.9f, 0.70000005f, 0.0f, 1.0f)
            style.setColor(PlotHistogramHovered, 1.0f, 0.59999996f, 0.0f, 1.0f)
            style.setColor(TableHeaderBg, 0.76f, 0.76f, 0.8f, 1.0f)
            style.setColor(TableBorderStrong, 0.31f, 0.31000003f, 0.35f, 1.0f)
            style.setColor(TableBorderLight, 0.69f, 0.69f, 0.75f, 1.0f)
            style.setColor(TableRowBg, 1.0f, 1.0f, 1.0f, 0.0f)
            style.setColor(TableRowBgAlt, 0.0f, 0.0f, 0.0f, 0.038999997f)
            style.setColor(TextSelectedBg, 0.8f, 0.8f, 0.8f, 0.25f)
            style.setColor(DragDropTarget, 1.0f, 1.0f, 0.0f, 0.585f)
            style.setColor(NavHighlight, 1f, 0.3647f, 0f, 1.0f)
            style.setColor(NavWindowingHighlight, 0.0f, 0.0f, 0.0f, 0.455f)
            style.setColor(NavWindowingDimBg, 0.2f, 0.2f, 0.2f, 0.13f)
            style.setColor(ModalWindowDimBg, 0.8f, 0.8f, 0.8f, 0.22749999f)
        } else {
            val accentColor = intArrayOf(255, 93, 0)
            val accentLightened = intArrayOf(255, 157, 93)
            val accentDarkened = intArrayOf(170, 62, 0)

            style.setColor(Text, 255, 255, 255, 255)
            style.setColor(TextDisabled, 153, 153, 153, 255)
            style.setColor(WindowBg, 238, 238, 238, 230)
            style.setColor(ChildBg, 0, 0, 0, 0)
            style.setColor(PopupBg, 255, 255, 255, 255)
            style.setColor(Border, 204, 204, 204, 255)
            style.setColor(BorderShadow, 0, 0, 0, 0)
            style.setColor(FrameBg, 255, 255, 255, 255)
            style.setColor(FrameBgHovered, 255, 255, 255, 255)
            style.setColor(FrameBgActive, 255, 255, 255, 255)
            style.setColor(TitleBg, 57, 57, 57, 255)
            style.setColor(TitleBgActive, 74, 74, 74, 255)
            style.setColor(TitleBgCollapsed, 57, 57, 57, 255)
            style.setColor(MenuBarBg, 74, 74, 74, 255)
            style.setColor(ScrollbarBg, 74, 74, 74, 255)
            style.setColor(ScrollbarGrab, 99, 99, 99, 255)
            style.setColor(ScrollbarGrabHovered, 120, 120, 120, 255)
            style.setColor(ScrollbarGrabActive, 150, 150, 150, 255)
            style.setColor(CheckMark, 255, 255, 255, 255)
            style.setColor(SliderGrab, accentLightened[0], accentLightened[1], accentLightened[2], 255)
            style.setColor(SliderGrabActive, accentDarkened[0], accentDarkened[1], accentDarkened[2], 255)
            style.setColor(Button, accentColor[0], accentColor[1], accentColor[2], 255)
            style.setColor(ButtonHovered, accentLightened[0], accentLightened[1], accentLightened[2], 255)
            style.setColor(ButtonActive, accentDarkened[0], accentDarkened[1], accentDarkened[2], 255)
            style.setColor(Header, 57, 57, 57, 255)
            style.setColor(HeaderHovered, 99, 99, 99, 255)
            style.setColor(HeaderActive, 120, 120, 120, 255)
            style.setColor(Separator, 110, 110, 128, 255)
            style.setColor(SeparatorHovered, 190, 190, 190, 255)
            style.setColor(SeparatorActive, 255, 255, 255, 255)
            style.setColor(ResizeGrip, 255, 255, 255, 64)
            style.setColor(ResizeGripHovered, 255, 255, 255, 171)
            style.setColor(ResizeGripActive, 255, 255, 255, 171)
            style.setColor(Tab, 239, 239, 239, 255)
            style.setColor(TabHovered, 204, 204, 204, 255)
            style.setColor(TabActive, 255, 93, 0, 255)
            style.setColor(TabUnfocused, 239, 239, 239, 255)
            style.setColor(TabUnfocusedActive, 204, 204, 204, 255)
            style.setColor(DockingPreview, accentColor[0], accentColor[1], accentColor[2], 255)
            style.setColor(DockingEmptyBg, 255, 255, 255, 255)
            style.setColor(PlotLines, 0, 0, 0, 255)
            style.setColor(PlotLinesHovered, 255, 110, 89, 255)
            style.setColor(PlotHistogram, 100, 149, 237, 255)
            style.setColor(PlotHistogramHovered, 255, 179, 71, 255)
            style.setColor(TableHeaderBg, 74, 74, 74, 255)
            style.setColor(TableBorderStrong, 0, 0, 0, 255)
            style.setColor(TableBorderLight, 189, 189, 189, 255)
            style.setColor(TableRowBg, 0, 0, 0, 0)
            style.setColor(TableRowBgAlt, 255, 255, 255, 51)
            style.setColor(TextSelectedBg, 255, 93, 0, 179)
            style.setColor(DragDropTarget, 255, 255, 0, 230)
            style.setColor(NavHighlight, 255, 93, 0, 255)
            style.setColor(NavWindowingHighlight, 255, 255, 255, 179)
            style.setColor(NavWindowingDimBg, 204, 204, 204, 153)
            style.setColor(ModalWindowDimBg, 204, 204, 204, 153)
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
        on<EventGlfwInit> {
            initImGui()
            imGuiGlfw.init(it.handle, true)
            imGuiGl3.init()
        }

        var tmpDarkMode = true

        /**
         * Renders ImGui during the render event.
         *
         * @param E The [RenderImGuiScreenEvent] triggered during application render.
         */
        on<EventRenderImGuiScreen> {
            smoothScroll()

            if (tmpDarkMode != ImGuiScreenFeature.darkMode) {
                tmpDarkMode = ImGuiScreenFeature.darkMode
                initStyle(tmpDarkMode)
            }

            imGuiGlfw.newFrame()
            newFrame()
            it.screen.renderImGui()
            render()
            EventPreRenderImGui.fire()
            renderImGui()
            EventPostRenderImGui.fire()
        }
    }
}
