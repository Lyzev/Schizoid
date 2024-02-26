/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.setting.settings

import com.google.gson.JsonObject
import dev.lyzev.api.setting.SettingClient
import dev.lyzev.schizoid.feature.IFeature
import imgui.ImGui.*
import net.minecraft.util.math.MathHelper
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
class SettingClientSlider<T : Number>(
    container: KClass<out IFeature>,
    name: String,
    desc: String?,
    value: T,
    private val minValue: T,
    private val maxValue: T,
    private val decimalPlaces: Int,
    private val unit: String?,
    private val allowOutOfBounds: Boolean,
    hide: () -> Boolean,
    change: (T) -> Unit
) : SettingClient<T>(container, name, desc, value, hide, change) {

    private val valueType: KClass<out Number> = value::class

    override fun render() {
        text(name)
        if (desc != null && isItemHovered()) setTooltip(desc)
        if (value is Int) {
            i[0] = value as Int
            if (sliderInt("", i, minValue as Int, maxValue as Int, "%d" + if (unit != null) " $unit" else "")) {
                value = if (allowOutOfBounds)
                    i[0] as T
                else
                    MathHelper.clamp(i[0], minValue.toInt(), maxValue.toInt()) as T
            }
        } else if (value is Float) {
            f[0] = value as Float
            if (sliderFloat("", f, minValue as Float, maxValue as Float, "%.${decimalPlaces}f" + if (unit != null) " $unit" else "")) {
                value = if (allowOutOfBounds)
                    f[0] as T
                else
                    MathHelper.clamp(f[0], minValue.toFloat(), maxValue.toFloat()) as T
            }
        }
    }

    override fun setValue(ref: Any, prop: KProperty<*>, value: T) {
        if (allowOutOfBounds)
            super.setValue(ref, prop, value)
        else if (valueType == Int::class)
            super.setValue(ref, prop, MathHelper.clamp(value.toInt(), minValue.toInt(), maxValue.toInt()) as T)
        else if (valueType == Float::class)
            super.setValue(ref, prop, MathHelper.clamp(value.toFloat(), minValue.toFloat(), maxValue.toFloat()) as T)
    }

    override fun load(value: JsonObject) {
        if (value.has("number")) {
            if (valueType == Int::class) {
                if (allowOutOfBounds)
                    this.value = value["number"].asInt as T
                else
                    this.value = MathHelper.clamp(value["number"].asInt, minValue.toInt(), maxValue.toInt()) as T
            } else if (valueType == Float::class) {
                if (allowOutOfBounds)
                    this.value = value["number"].asFloat as T
                else
                    this.value = MathHelper.clamp(value["number"].asFloat, minValue.toFloat(), maxValue.toFloat()) as T
            }
        }
    }

    override fun save(value: JsonObject) {
        value.addProperty("number", this.value)
    }

    companion object {
        private val i = intArrayOf(0)
        private val f = floatArrayOf(0f)
    }
}

fun IFeature.slider(
    name: String,
    desc: String? = null,
    value: Int,
    minValue: Int,
    maxValue: Int,
    unit: String? = null,
    allowOutOfBounds: Boolean = false,
    hide: () -> Boolean = { false },
    change: (Int) -> Unit = {}
) = SettingClientSlider(this::class, name, desc, value, minValue, maxValue, 0, unit, allowOutOfBounds, hide, change)

fun IFeature.slider(
    name: String,
    desc: String? = null,
    value: Float,
    minValue: Float,
    maxValue: Float,
    decimalPlaces: Int = 1,
    unit: String? = null,
    allowOutOfBounds: Boolean = false,
    hide: () -> Boolean = { false },
    change: (Float) -> Unit = {}
) = SettingClientSlider(this::class, name, desc, value, minValue, maxValue, decimalPlaces, unit, allowOutOfBounds, hide, change)
