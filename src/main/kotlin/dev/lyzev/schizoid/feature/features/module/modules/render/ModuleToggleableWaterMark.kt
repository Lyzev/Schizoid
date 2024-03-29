/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.render

import dev.lyzev.api.imgui.font.ImGuiFonts.LEAGUE_SPARTAN_EXTRA_BOLD
import dev.lyzev.api.imgui.font.ImGuiFonts.OPEN_SANS_BOLD
import dev.lyzev.api.setting.settings.switch
import dev.lyzev.api.setting.settings.text
import dev.lyzev.schizoid.feature.features.module.ModuleToggleableRenderImGuiContent
import imgui.ImGui.*

object ModuleToggleableWaterMark :
    ModuleToggleableRenderImGuiContent("Water Mark", "Shows a water mark on the screen.", category = Category.RENDER) {

    val waterMark by text("Water Mark", value = "SCHIZOID")
    val showFPS by switch("Show FPS", "Shows the current FPS.", true)

    private val enza by switch("Enza", value = false)

    private val functions = Math::class.members.map { it.name }.toMutableList()
    private var time = System.currentTimeMillis()

    override fun renderImGuiContent() {
        LEAGUE_SPARTAN_EXTRA_BOLD.begin()
        dummy(115f, 0f)
        sameLine(15f)
        if (enza) {
            if (time + 300 < System.currentTimeMillis()) {
                time = System.currentTimeMillis()
                functions.shuffle()
            }
            text(functions.first())
        } else {
            text(waterMark)
        }
        LEAGUE_SPARTAN_EXTRA_BOLD.end()
        if (showFPS) {
            OPEN_SANS_BOLD.begin()
            var fps = 1000f / (mc.lastFrameDuration * mc.renderTickCounter.tickTime)
            if (enza) {
                fps += 1000f + Math.random().toFloat() * 100f;
            }
            text("%.0f fps".format(fps))
            OPEN_SANS_BOLD.end()
        }
    }
}
