/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.setting.settings

import com.google.gson.JsonObject
import su.mandora.tarasande.util.render.animation.EasingFunction
import su.mandora.tarasande.util.render.animation.TimeAnimator
import dev.lyzev.api.setting.SettingClient
import dev.lyzev.schizoid.feature.IFeature
import imgui.ImColor
import imgui.ImGui.*
import net.minecraft.util.math.MathHelper
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * A specific implementation of the [Setting] class for switch settings.
 *
 * @param container The class of the settings container where this setting belongs.
 * @param name The name of the setting.
 * @param value The initial value of the boolean setting.
 * @param hide A lambda function that determines whether this setting is hidden or not.
 * @param change A lambda function that will be called when the value of the setting changes.
 */
class SettingClientSwitch(
    container: KClass<out IFeature>,
    name: String,
    desc: String?,
    value: Boolean,
    hide: () -> Boolean,
    change: (Boolean) -> Unit
) : SettingClient<Boolean>(container, name, desc, value, hide, change) {

    private val timeAnimator = TimeAnimator(300)

    var track = floatArrayOf(0f, 0f, 0.8f)
    var trackActive = floatArrayOf(0.06027397f, 1f, 0.5f)
    var thumb = ImColor.rgb(255, 255, 255)

    private var shadowValue
        get() = value
        set(value) {
            this.value = value
            timeAnimator.setReversed(!value)
        }

    fun switch(strId: String, v: BooleanArray) {
        val p = getCursorScreenPos()
        val drawList = getWindowDrawList()

        val height = 17.5f
        val width = height * 1.8f
        val radius = height * 0.5f

        if (invisibleButton(strId, width, height))
            v[0] = !v[0]

        val delta = EasingFunction.LINEAR.ease(timeAnimator.getProgress()).toFloat()
        val trackColor = ImColor.hsl(MathHelper.lerp(delta, track[0], trackActive[0]), MathHelper.lerp(delta, track[1], trackActive[1]), MathHelper.lerp(delta, track[2], trackActive[2]))
        drawList.addRectFilled(p.x, p.y + height * 0.15f, p.x + width, p.y + height * 0.85f, trackColor, height * 0.35f)

        drawList.addCircleFilled(
            (p.x + radius + (width - height) * EasingFunction.IN_OUT_BACK.ease(timeAnimator.getProgress())).toFloat(),
            p.y + height * 0.5f,
            radius,
            thumb
        )
    }

    override fun render() {
        text(name)
        if (desc != null && isItemHovered()) setTooltip(desc)
        sameLine()
        v[0] = shadowValue
        switch(name, v)
        if (v[0] != shadowValue)
            shadowValue = v[0]
    }

    override fun load(value: JsonObject) {
        this.shadowValue = value["enabled"].asBoolean
    }

    override fun save(value: JsonObject) = value.addProperty("enabled", this.shadowValue)

    override fun setValue(ref: Any, prop: KProperty<*>, value: Boolean) {
        timeAnimator.setReversed(!value)
        super.setValue(ref, prop, value)
    }

    init {
        timeAnimator.setReversed(!value)
    }

    companion object {
        private val v = booleanArrayOf(false)
    }
}

fun IFeature.switch(
    name: String,
    desc: String? = null,
    value: Boolean,
    hide: () -> Boolean = { false },
    change: (Boolean) -> Unit = {}
) = SettingClientSwitch(this::class, name, desc, value, hide, change)
