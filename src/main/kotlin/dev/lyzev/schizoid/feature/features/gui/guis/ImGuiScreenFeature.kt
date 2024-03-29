/*
 * Copyright (c) 2023-2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.gui.guis

import com.mojang.blaze3d.systems.RenderSystem
import dev.lyzev.api.events.EventKeybindsRequest
import dev.lyzev.api.events.EventKeybindsResponse
import dev.lyzev.api.events.EventListener
import dev.lyzev.api.events.on
import dev.lyzev.api.glfw.GLFWKey
import dev.lyzev.api.imgui.font.ImGuiFonts.OPEN_SANS_BOLD
import dev.lyzev.api.imgui.font.ImGuiFonts.OPEN_SANS_REGULAR
import dev.lyzev.api.imgui.theme.ImGuiThemes
import dev.lyzev.api.setting.settings.keybinds
import dev.lyzev.api.setting.settings.option
import dev.lyzev.api.setting.settings.slider
import dev.lyzev.schizoid.Schizoid
import dev.lyzev.schizoid.feature.Feature
import dev.lyzev.schizoid.feature.FeatureManager
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.gui.ImGuiScreen
import imgui.ImGui.*
import imgui.flag.ImGuiComboFlags
import imgui.flag.ImGuiCond
import imgui.flag.ImGuiFocusedFlags
import imgui.flag.ImGuiInputTextFlags
import imgui.flag.ImGuiWindowFlags
import imgui.type.ImBoolean
import imgui.type.ImString
import me.xdrop.fuzzywuzzy.FuzzySearch
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.util.InputUtil
import net.minecraft.util.Identifier
import org.lwjgl.glfw.GLFW
import su.mandora.tarasande.util.render.animation.EasingFunction
import su.mandora.tarasande.util.render.animation.TimeAnimator


object ImGuiScreenFeature : ImGuiScreen("Feature Screen"), EventListener {

    val theme by option("Theme", "The theme of the GUI.", ImGuiThemes.DARK_ORANGE, ImGuiThemes.entries) {
        RenderSystem.recordRenderCall { it.apply() }
    }

    private val texturesMario = Array(3) {
        Identifier(Schizoid.MOD_ID, "textures/mario_$it.png")
    }
    private var isMarioRunning = false
    private val timeAnimatorMario = TimeAnimator(8000)

    private val animationMario by option(
        "Mario Animation",
        "Enables the Mario animation.",
        EasingFunction.IN_OUT_ELASTIC,
        EasingFunction.entries
    )
    private val speedMario by slider(
        "Mario Speed",
        "The speed of the Mario animation.",
        5000,
        1000,
        10000,
        "ms",
        true
    ) {
        timeAnimatorMario.animationLength = it.toLong()
    }

    private var waitingForInput = -1L
    private var isWaitingForInput = false
    private const val TIMEOUT = 5000

    private var lastShiftPress = -1L
    private val maxTimeBetweenShiftPresses = 500L
    private var isSearching = ImBoolean(false)
    private var shouldFocus = false
    private val searchInput = ImString()
    private val prevSearchResult = mutableListOf<String>()
    var searchResult: IFeature? = null

    override fun renderInGameBackground(context: DrawContext) =
        theme.renderInGameBackground(context, this.width, this.height)

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)

        if (isWaitingForInput && System.currentTimeMillis() - waitingForInput > TIMEOUT) {
            EventKeybindsResponse(GLFW.GLFW_KEY_UNKNOWN).fire()
            isWaitingForInput = false
        }

        if (!isMarioRunning && timeAnimatorMario.isCompleted()) {
            timeAnimatorMario.setProgress(.0)
            return
        }
        isMarioRunning = true

        val x = -32 + ((mc.window.scaledWidth + 32) * animationMario.ease(timeAnimatorMario.getProgress())).toInt()
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
        context?.drawTexture(
            texturesMario[(System.currentTimeMillis() / 100.0 % texturesMario.size).toInt()],
            x,
            mc.window.scaledHeight - 32,
            32,
            32,
            0f,
            0f,
            400,
            400,
            400,
            400
        )
        isMarioRunning = x < mc.window.scaledWidth
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (isWaitingForInput) {
            isWaitingForInput = false
            EventKeybindsResponse(keyCode).fire()
            return true
        } else if (keybinds.contains(GLFWKey[keyCode])) {
            keybindReleased()
            return true
        } else if (keyCode == GLFWKey.LEFT_SHIFT.code) {
            if (System.currentTimeMillis() - lastShiftPress <= maxTimeBetweenShiftPresses) {
                searchInput.set("")
                isSearching.set(true)
                shouldFocus = true
                lastShiftPress = -1L
                return true
            } else
                lastShiftPress = System.currentTimeMillis()
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (isWaitingForInput) {
            isWaitingForInput = false
            EventKeybindsResponse(button).fire()
            return true
        } else if (keybinds.contains(GLFWKey[button])) {
            isSearching.set(false)
            shouldFocus = false
            keybindReleased()
            return true
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    private fun renderSearch() {
        OPEN_SANS_REGULAR.begin()
        if (beginCombo(
                "##prevSearchResult",
                prevSearchResult.firstOrNull() ?: "Previous Searches",
                ImGuiComboFlags.HeightSmall or ImGuiComboFlags.NoPreview
            )
        ) {
            for (i in prevSearchResult.indices) {
                val isSelected = prevSearchResult[i] == searchResult?.name
                if (selectable(prevSearchResult[i], isSelected)) {
                    searchResult = FeatureManager[prevSearchResult[i]]
                    prevSearchResult.removeAt(i)
                    prevSearchResult.add(0, searchResult!!.name)
                }
                if (isSelected) setItemDefaultFocus()
            }
            endCombo()
        }
        sameLine()
        setNextItemWidth(getColumnWidth())
        if (shouldFocus)
            setKeyboardFocusHere()
        shouldFocus = false
        inputTextWithHint("##search", "Search...", searchInput)
        if (GLFWKey.ENTER.isPressed()) {
            searchResult = FeatureManager.get(*Feature.Category.values()).maxByOrNull { FuzzySearch.weightedRatio(searchInput.get(), it.name) }
            if (searchResult != null) {
                prevSearchResult.remove(searchResult!!.name)
                prevSearchResult.add(0, searchResult!!.name)
            }
        }
        if (beginListBox("##searchResults", getColumnWidth(), mc.window.framebufferHeight * .15f)) {
            FeatureManager.get(*Feature.Category.values()).sortedByDescending { FuzzySearch.weightedRatio(searchInput.get(), it.name) }.forEach { feature ->
                if (selectable("[${feature.category}] ${feature.name}", false)) {
                    searchResult = feature
                    prevSearchResult.remove(feature.name)
                    prevSearchResult.add(0, feature.name)
                }
            }
            endListBox()
        }
        OPEN_SANS_REGULAR.end()
    }

    private fun renderSearchUI() {
        pushID("##search")
        OPEN_SANS_BOLD.begin()
        if (shouldFocus)
            setNextWindowFocus()
        var isFocused = true
        setNextWindowPos(getMainViewport().centerX, getMainViewport().centerY, ImGuiCond.Always, 0.5f, 0.5f)
        setNextWindowSize(mc.window.framebufferWidth * 0.3f, 0f)
        if (begin("\"SEARCH\"", isSearching, ImGuiWindowFlags.AlwaysAutoResize or ImGuiWindowFlags.NoCollapse or ImGuiWindowFlags.NoMove)) {
            isFocused = isWindowFocused(ImGuiFocusedFlags.RootAndChildWindows)
            renderSearch()
        }
        end()
        if (!isFocused)
            isSearching.set(false)
        OPEN_SANS_BOLD.end()
        popID()
    }

    override fun renderImGui() {
        if (isSearching.get())
            renderSearchUI()
        Feature.Category.entries.forEach(Feature.Category::render)
    }

    init {
        on<EventKeybindsRequest> {
            if (isWaitingForInput) EventKeybindsResponse(GLFW.GLFW_KEY_UNKNOWN).fire()
            waitingForInput = System.currentTimeMillis()
            isWaitingForInput = true
        }
    }

    override val shouldHandleEvents: Boolean
        get() = mc.currentScreen == null || mc.currentScreen == this

    override fun shouldPause(): Boolean = false

    override val name: String
        get() = "Feature Screen"
    override val desc: String
        get() = "Displays all features and their respective settings."
    override var keybinds by keybinds(
        "Keybinds",
        "All keys used to control the feature.",
        mutableSetOf(GLFWKey.INSERT, GLFWKey.RIGHT_SHIFT)
    ) {
        it.removeIf { key -> key == GLFWKey.MOUSE_BUTTON_LEFT || key == GLFWKey.MOUSE_BUTTON_RIGHT || key == GLFWKey.MOUSE_BUTTON_MIDDLE }
    }

    override val category: Feature.Category
        get() = Feature.Category.RENDER
}
