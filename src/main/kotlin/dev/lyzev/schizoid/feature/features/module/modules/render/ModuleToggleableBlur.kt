/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.render

import dev.lyzev.api.opengl.shader.blur.Blurs
import dev.lyzev.api.setting.settings.option
import dev.lyzev.api.setting.settings.slider
import dev.lyzev.api.setting.settings.switch
import dev.lyzev.schizoid.feature.features.module.ModuleToggleable
import dev.lyzev.api.settings.Setting.Companion.neq

object ModuleToggleableBlur :
    ModuleToggleable("Blur", "All settings related to blur effects.", category = Category.RENDER) {

    val method by lazy {
        option(
            "Method", "The method used to blur the screen.", Blurs.DUAL_KAWASE, Blurs.entries
        )
    }

    val strength by slider("Strength", "The strength of the blur effect.", 9, 1, 20)

    val acrylic by switch("Acrylic", "Adds an acrylic effect to the blur.", true)
    val luminosity by slider("Luminosity", "The luminosity of the acrylic effect.", 180, 0, 200, unit = "%%", allowOutOfBounds = true, hide = ::acrylic neq true)
    val noiseStrength by slider("Noise Strength", "The strength of the noise effect.", 50, 0, 100, unit = "%%", hide = ::acrylic neq true)
    val noiseSale by slider("Noise Scale", "The scale of the noise effect.", 100, 0, 100, unit = "%%", allowOutOfBounds = true, hide = {
        !acrylic || noiseStrength == 0
    })
    val RGBPuke by switch("RGB Puke", "Adds an RGB puke effect to the blur.", false)
    val RGBPukeOpacity by slider("RGB Puke Opacity", "The opacity of the RGB puke effect.", 20, 1, 100, unit = "%%", hide = {
        !RGBPuke || !acrylic
    })

    val dropShadow by switch("Drop Shadow", "Adds a drop shadow to the blur.", true)
    val dropShadowStrength by slider("Drop Shadow Strength", "The strength of the drop shadow effect.", 9, 1, 20, hide = ::dropShadow neq true)
    val dropShadowRGBPuke by switch("Drop Shadow RGB Puke", "Adds an RGB puke effect to the drop shadow.", false, hide = ::dropShadow neq true)

    val fog by switch("Fog", "Adds a fog effect to the blur.", true)
    val fogStrength by slider("Fog Strength", "The strength of the fog effect.", 9, 1, 20, hide = ::fog neq true)
    val fogRGBPuke by switch("Fog RGB Puke", "Adds an RGB puke effect to the fog.", false, hide = ::fog neq true)
    val fogRGBPukeOpacity by slider("Fog RGB Puke Opacity", "The opacity of the RGB puke effect.", 20, 1, 100, unit = "%%", hide = {
        !fogRGBPuke || !acrylic
    })

    init {
        toggle()
    }
}
