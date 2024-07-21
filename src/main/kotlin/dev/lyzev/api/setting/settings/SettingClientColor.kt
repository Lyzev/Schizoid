/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.setting.settings

import dev.lyzev.api.setting.SettingClient
import dev.lyzev.schizoid.feature.IFeature
import imgui.ImGui.*
import imgui.flag.ImGuiColorEditFlags
import imgui.flag.ImGuiStyleVar
import kotlinx.serialization.json.*
import java.awt.Color
import kotlin.reflect.KClass

class SettingClientColor(
    container: KClass<out IFeature>,
    name: String,
    desc: String?,
    value: Color,
    private val useAlpha: Boolean,
    private val useRGBPuke: Boolean = false,
    hide: () -> Boolean,
    change: (Color) -> Unit
) : SettingClient<Color>(container, name, desc, value, hide, change) {

    var isRGBPuke = false
    var saturation = 70
    var brightness = 100

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
        if (useRGBPuke) {
            pushStyleVar(ImGuiStyleVar.FramePadding, 0f, 0f)
            if (checkbox("$name RGB Puke", isRGBPuke)) {
                isRGBPuke = !isRGBPuke
            }
            popStyleVar()
            if (isRGBPuke) {
                val treeNode = treeNode("$name RGB Puke Settings")
                if (desc != null && isItemHovered()) setTooltip("Settings for the RGB Puke Effect.")
                if (treeNode) {
                    i[0] = saturation
                    text("$name Saturation")
                    setNextItemWidth(getColumnWidth())
                    if (sliderInt("###Saturation$name", i, 0, 100))
                        saturation = i[0]
                    i[0] = brightness
                    text("$name Brightness")
                    setNextItemWidth(getColumnWidth())
                    if (sliderInt("###Brightness$name", i, 0, 100))
                        brightness = i[0]
                    treePop()
                }
            }
        }
    }

    override fun load(value: JsonElement) {
        val red = value.jsonObject["red"]?.jsonPrimitive?.int ?: this.value.red
        val green = value.jsonObject["green"]?.jsonPrimitive?.int ?: this.value.green
        val blue = value.jsonObject["blue"]?.jsonPrimitive?.int ?: this.value.blue
        val alpha = value.jsonObject["alpha"]?.jsonPrimitive?.int ?: this.value.alpha
        this.value = Color(red, green, blue, alpha)
        isRGBPuke = value.jsonObject["isRGBPuke"]?.jsonPrimitive?.boolean ?: isRGBPuke
        saturation = value.jsonObject["saturation"]?.jsonPrimitive?.int ?: saturation
        brightness = value.jsonObject["brightness"]?.jsonPrimitive?.int ?: brightness
    }

    override fun save(): JsonElement {
        return JsonObject(
            mapOf(
                "red" to JsonPrimitive(value.red),
                "green" to JsonPrimitive(value.green),
                "blue" to JsonPrimitive(value.blue),
                "alpha" to JsonPrimitive(value.alpha),
                "isRGBPuke" to JsonPrimitive(isRGBPuke),
                "saturation" to JsonPrimitive(saturation),
                "brightness" to JsonPrimitive(brightness)
            )
        )
    }

    companion object {
        private val v = FloatArray(4) { 0f }
        private val i = IntArray(1) { 0 }
        private const val DEFAULT_FLAGS =
            ImGuiColorEditFlags.DisplayHex or ImGuiColorEditFlags.NoSidePreview or ImGuiColorEditFlags.PickerHueWheel
    }
}

fun IFeature.color(
    name: String,
    desc: String? = null,
    value: Color,
    useAlpha: Boolean = false,
    useRGBPuke: Boolean = false,
    hide: () -> Boolean = { false },
    change: (Color) -> Unit = {}
) = SettingClientColor(this::class, name, desc, value, useAlpha, useRGBPuke, hide, change)
