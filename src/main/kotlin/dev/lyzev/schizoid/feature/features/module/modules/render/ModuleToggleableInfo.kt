/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.render

import dev.lyzev.api.setting.settings.switch
import dev.lyzev.schizoid.feature.features.module.ModuleToggleableRenderImGuiContent
import imgui.ImGui.text
import net.minecraft.util.math.MathHelper

object ModuleToggleableInfo :
    ModuleToggleableRenderImGuiContent("Info", "Shows a water mark on the screen.", category = Category.RENDER) {

    val fps by switch("FPS", "Shows the current FPS.", true)
    val username by switch("Username", "Shows the current username.", true)
    val yaw by switch("Yaw", "Shows the current yaw.", true)
    val pitch by switch("Pitch", "Shows the current pitch.", true)
    val coords by switch("Coords", "Shows the current coordinates.", true)

    override fun renderImGuiContent() {
        if (fps)
            text("%.0f fps".format(1000f / (mc.lastFrameDuration * mc.renderTickCounter.tickTime)))
        if (username)
            text("Username: ${mc.session.username}")
        if (yaw)
            text("Yaw: %.1f°".format(MathHelper.wrapDegrees(mc.player?.yaw!!)))
        if (pitch)
            text("Pitch: %.1f°".format(mc.player?.pitch))
        if (coords)
            text("XYZ: %.1f / %.1f / %.1f".format(mc.player?.x, mc.player?.y, mc.player?.z))
    }
}
