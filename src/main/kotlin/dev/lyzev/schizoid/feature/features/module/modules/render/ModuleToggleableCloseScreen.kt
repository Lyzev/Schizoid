/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.render

import dev.lyzev.api.imgui.font.ImGuiFonts.OPEN_SANS_REGULAR
import dev.lyzev.api.setting.settings.switch
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.gui.guis.ImGuiScreenFeature
import dev.lyzev.schizoid.feature.features.module.ModuleToggleableRenderImGuiContent
import imgui.ImGui
import imgui.ImGui.button
import imgui.ImGui.getStyle

object ModuleToggleableCloseScreen :
    ModuleToggleableRenderImGuiContent(
        "Close Screen",
        "Shows a close screen button in ingame screens.",
        category = IFeature.Category.RENDER
    ) {

    val showInFeatureScreen by switch(
        "Show in feature screen",
        "Shows the close screen button in the feature screen.",
        false
    )

    override fun renderImGuiContent() {
        if (button("Close Screen", 200f, OPEN_SANS_REGULAR.size + getStyle().framePaddingY * 2))
            mc.currentScreen?.close()
    }

    override val shouldHandleEvents: Boolean
        get() = super.shouldHandleEvents && mc.currentScreen != null && (showInFeatureScreen || mc.currentScreen !is ImGuiScreenFeature)
}
