/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.render

import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.ModuleToggleableRenderImGuiContent

object ModuleToggleableScoreBoard :
    ModuleToggleableRenderImGuiContent("Score Board", "Renders score board in imgui.", category = IFeature.Category.RENDER) {

    override fun renderImGuiContent() {

    }
}
