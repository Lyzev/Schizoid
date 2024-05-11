/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.setting.settings

import com.google.gson.JsonObject
import dev.lyzev.api.setting.SettingClient
import dev.lyzev.schizoid.feature.IFeature
import imgui.ImGui.*
import imgui.flag.ImGuiColorEditFlags
import java.awt.Color
import kotlin.reflect.KClass

class SettingClientColor(
    container: KClass<out IFeature>,
    name: String,
    desc: String?,
    value: Color,
    private val useAlpha: Boolean,
    hide: () -> Boolean,
    change: (Color) -> Unit
) : SettingClient<Color>(container, name, desc, value, hide, change) {

    override fun render() {
        text(name)
        if (desc != null && isItemHovered()) setTooltip(desc)
        setNextItemWidth(200f)
        v[0] = value.red / 255f
        v[1] = value.green / 255f
        v[2] = value.blue / 255f
        if (!useAlpha) {
            if (colorPicker3("##$name", v, DEFAULT_FLAGS))
                value = Color(v[0], v[1], v[2])
        } else {
            v[3] = value.alpha / 255f
            if (colorPicker4("##$name", v,  DEFAULT_FLAGS or ImGuiColorEditFlags.AlphaBar))
                value = Color(v[0], v[1], v[2], v[3])
        }
    }

    override fun load(value: JsonObject) {
        val red = value["red"].asInt
        val green = value["green"].asInt
        val blue = value["blue"].asInt
        this.value = Color(red, green, blue)
    }

    override fun save(value: JsonObject) {
        value.addProperty("red", this.value.red)
        value.addProperty("green", this.value.green)
        value.addProperty("blue", this.value.blue)
    }

    companion object {
        private val v = FloatArray(4) { 0f }
        private const val DEFAULT_FLAGS = ImGuiColorEditFlags.DisplayHex or ImGuiColorEditFlags.NoSidePreview or ImGuiColorEditFlags.PickerHueWheel
    }
}

fun IFeature.color(
    name: String,
    desc: String? = null,
    value: Color,
    useAlpha: Boolean = false,
    hide: () -> Boolean = { false },
    change: (Color) -> Unit = {}
) = SettingClientColor(this::class, name, desc, value, useAlpha, hide, change)
