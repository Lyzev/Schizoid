/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.imgui.render.renderable

import dev.lyzev.api.imgui.font.ImGuiFonts
import dev.lyzev.api.imgui.font.ImGuiFonts.OPEN_SANS_BOLD
import dev.lyzev.api.imgui.font.ImGuiFonts.OPEN_SANS_REGULAR
import dev.lyzev.api.imgui.font.icon.FontAwesomeIcons
import dev.lyzev.api.imgui.render.ImGuiRenderable
import dev.lyzev.api.setting.SettingInitializer
import imgui.ImGui.*
import imgui.type.ImString
import me.xdrop.fuzzywuzzy.FuzzySearch

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
            ImGuiFonts.FONT_AWESOME_SOLID.begin()
            if (button(FontAwesomeIcons.Plus, OPEN_SANS_REGULAR.size * 1.5F, OPEN_SANS_REGULAR.size * 1.5F)) {
                val name = input.get()
                if (name.isNotEmpty()) {
                    SettingInitializer.loaded = name
                    input.set("")
                }
            }
            ImGuiFonts.FONT_AWESOME_SOLID.end()
            if (beginListBox("##nameResults", -1f, -1f)) {
                SettingInitializer.available.sortedByDescending {
                    FuzzySearch.weightedRatio(input.get(), it)
                }.forEach {
                    if (selectable(it, it == SettingInitializer.loaded)) {
                        if (getIO().keyCtrl && it != "default" && it == SettingInitializer.loaded) {
                            SettingInitializer.loaded = "default"
                            SettingInitializer.reload()
                            SettingInitializer.getConfigFile(it)?.delete()
                        } else if (it != SettingInitializer.loaded) {
                            SettingInitializer.loaded = it
                            SettingInitializer.reload()
                        }
                    }
                    if (it != SettingInitializer.loaded && isItemHovered())
                        setTooltip("Click to load this config." + if (it != "default") "\nCTRL + Click to delete this config." else "")
                }
                endListBox()
            }
            OPEN_SANS_REGULAR.end()
        }
        end()
        OPEN_SANS_BOLD.end()
        popID()
    }
}
