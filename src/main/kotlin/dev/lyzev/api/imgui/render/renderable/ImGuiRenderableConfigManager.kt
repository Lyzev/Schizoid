/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.imgui.render.renderable

import dev.lyzev.api.glfw.GLFWKey
import dev.lyzev.api.imgui.font.ImGuiFonts.OPEN_SANS_BOLD
import dev.lyzev.api.imgui.font.ImGuiFonts.OPEN_SANS_REGULAR
import dev.lyzev.api.imgui.render.ImGuiRenderable
import dev.lyzev.api.setting.SettingInitializer
import dev.lyzev.schizoid.feature.FeatureManager
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.gui.guis.ImGuiScreenFeature.mc
import imgui.ImGui.*
import imgui.type.ImString
import me.xdrop.fuzzywuzzy.FuzzySearch
import net.minecraft.text.Text

class ImGuiRenderableConfigManager : ImGuiRenderable {

    private val input = ImString()

    override fun render() {
        pushID("##configmanger")
        OPEN_SANS_BOLD.begin()
        if (begin("\"CONFIG\"")) {
            OPEN_SANS_REGULAR.begin()
            setNextItemWidth(getColumnWidth() - OPEN_SANS_REGULAR.size * 2.2F)
            inputTextWithHint("##name", "Name", input)
            sameLine()
            if (button("+", OPEN_SANS_REGULAR.size * 1.5F, OPEN_SANS_REGULAR.size * 1.5F)) {
                SettingInitializer.loaded = input.get()
                input.set("")
            }
            if (beginListBox("##nameResults", getColumnWidth(), mc.window.framebufferHeight * .15f)) {
                SettingInitializer.available.sortedByDescending {
                    FuzzySearch.weightedRatio(input.get(), it)
                }.forEach {
                    if (selectable(it, it == SettingInitializer.loaded)) {
                        if (it != SettingInitializer.loaded) {
                            SettingInitializer.loaded = it
                            SettingInitializer.reload()
                        }
                    }
                }
                endListBox()
            }
            OPEN_SANS_REGULAR.end()
        }
        end()
        OPEN_SANS_BOLD.end()
        popID()
    }

    companion object {
        private const val MAX_TIME_BETWEEN_SHIFT_PRESSES = 300L
    }
}
