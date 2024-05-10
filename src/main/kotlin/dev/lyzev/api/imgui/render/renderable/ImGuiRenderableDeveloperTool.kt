/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.imgui.render.renderable

import dev.lyzev.api.events.EventReload
import dev.lyzev.api.imgui.font.ImGuiFonts.OPEN_SANS_BOLD
import dev.lyzev.api.imgui.font.ImGuiFonts.OPEN_SANS_REGULAR
import dev.lyzev.api.imgui.render.ImGuiRenderable
import imgui.ImGui.*

class ImGuiRenderableDeveloperTool : ImGuiRenderable {

    override fun render() {
        pushID("##developerTool")
        OPEN_SANS_BOLD.begin()
        if (begin("\"DEVELOPER TOOL\"")) {
            OPEN_SANS_BOLD.begin()
            text("\"SHADER\"")
            OPEN_SANS_BOLD.end()
            OPEN_SANS_REGULAR.begin()
            if (button("Reload Shaders", getColumnWidth(), OPEN_SANS_REGULAR.size + getStyle().framePaddingY * 2)) {
                EventReload.fire()
            }
            OPEN_SANS_REGULAR.end()
        }
        end()
        OPEN_SANS_BOLD.end()
        popID()
    }
}
