/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.setting.settings

import dev.lyzev.api.setting.SettingClient
import dev.lyzev.schizoid.feature.IFeature
import imgui.ImGui.*
import imgui.flag.ImGuiColorEditFlags
import kotlinx.serialization.json.*
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
            if (colorPicker4("##$name", v, DEFAULT_FLAGS or ImGuiColorEditFlags.AlphaBar))
                value = Color(v[0], v[1], v[2], v[3])
        }
    }

    override fun load(value: JsonElement) {
        val red = value.jsonObject["red"]?.jsonPrimitive?.int ?: this.value.red
        val green = value.jsonObject["green"]?.jsonPrimitive?.int ?: this.value.green
        val blue = value.jsonObject["blue"]?.jsonPrimitive?.int ?: this.value.blue
        val alpha = value.jsonObject["alpha"]?.jsonPrimitive?.int ?: this.value.alpha
        this.value = Color(red, green, blue, alpha)
    }

    override fun save(): JsonElement {
        return JsonObject(
            mapOf(
                "red" to JsonPrimitive(value.red),
                "green" to JsonPrimitive(value.green),
                "blue" to JsonPrimitive(value.blue),
                "alpha" to JsonPrimitive(value.alpha)
            )
        )
    }

    companion object {
        private val v = FloatArray(4) { 0f }
        private const val DEFAULT_FLAGS =
            ImGuiColorEditFlags.DisplayHex or ImGuiColorEditFlags.NoSidePreview or ImGuiColorEditFlags.PickerHueWheel
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
