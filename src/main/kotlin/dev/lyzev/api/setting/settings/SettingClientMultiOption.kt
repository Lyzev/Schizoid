/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.setting.settings

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dev.lyzev.api.setting.SettingClient
import dev.lyzev.schizoid.feature.IFeature
import imgui.ImGui.*
import imgui.flag.ImGuiStyleVar
import kotlin.math.max
import kotlin.reflect.KClass

/**
 * A specific implementation of the [SettingClient] class for multi-option settings.
 *
 * @param container The class of the settings container where this setting belongs.
 * @param name The name of the setting.
 * @param desc The description of the setting.
 * @param value The initial value of the multi-option setting.
 * @param hide A lambda function that determines whether this setting is hidden or not.
 * @param change A lambda function that will be called when the value of the setting changes.
 */
class SettingClientMultiOptionString(
    container: KClass<out IFeature>,
    name: String,
    desc: String?,
    value: Set<Pair<String, Boolean>>,
    hide: () -> Boolean,
    change: (Set<Pair<String, Boolean>>) -> Unit
) : SettingClient<Set<Pair<String, Boolean>>>(container, name, desc, value, hide, change) {

    override fun render() {
        val treeNode = treeNode(name)
        if (desc != null && isItemHovered()) setTooltip(desc)
        if (treeNode) {
            if (beginListBox("", -1f, calcHeight(value.size))) {
                for (value in this.value) {
                    text(value.first)
                    sameLine(max(getWindowContentRegionMaxX() - 8.75f / 2f - getStyle().windowPaddingX, calcTextSize(value.first).x + getStyle().framePaddingX + 2))
                    pushStyleVar(ImGuiStyleVar.FramePadding, 0f, 0f)
                    if (checkbox("##${value.first}", value.second)) {
                        this.value = this.value.map { if (it.first == value.first) it.first to !it.second else it }.toSet()
                        onChange(this.value)
                    }
                    popStyleVar()
                }
                endListBox()
            }
            treePop()
        }
    }

    override fun load(value: JsonObject) {
        val values = value.getAsJsonArray("selected").map { it.asString }
        val tmp = mutableSetOf<Pair<String, Boolean>>()
        for (i in this.value.indices) {
            val selected = this.value.elementAt(i).first
            tmp.add(selected to values.contains(selected))
        }
        this.value = tmp
    }

    override fun save(value: JsonObject) {
        val selected = JsonArray()
        for (tmp in this.value) {
            if (tmp.second)
                selected.add(tmp.first)
        }
        value.add("selected", selected)
    }
}

/**
 * A specific implementation of the [SettingClient] class for multi-option settings.
 *
 * @param container The class of the settings container where this setting belongs.
 * @param name The name of the setting.
 * @param desc The description of the setting.
 * @param value The initial value of the multi-option setting.
 * @param hide A lambda function that determines whether this setting is hidden or not.
 * @param change A lambda function that will be called when the value of the setting changes.
 */
class SettingClientMultiOptionEnum<T : OptionEnum>(
    container: KClass<out IFeature>,
    name: String,
    desc: String?,
    value: Set<Pair<T, Boolean>>,
    hide: () -> Boolean,
    change: (Set<Pair<T, Boolean>>) -> Unit
) : SettingClient<Set<Pair<T, Boolean>>>(container, name, desc, value, hide, change) {

    override fun render() {
        val treeNode = treeNode(name)
        if (desc != null && isItemHovered()) setTooltip(desc)
        if (treeNode) {
            if (beginListBox("", -1f, calcHeight(value.size))) {
                for (value in this.value) {
                    text(value.first.key)
                    sameLine(max(getWindowContentRegionMaxX() - 8.75f / 2f - getStyle().windowPaddingX, calcTextSize(value.first.key).x + getStyle().framePaddingX + 2))
                    pushStyleVar(ImGuiStyleVar.FramePadding, 0f, 0f)
                    if (checkbox("##${value.first.key}", value.second)) {
                        this.value = this.value.map { if (it.first == value.first) it.first to !it.second else it }.toSet()
                        onChange(this.value)
                    }
                    popStyleVar()
                }
                endListBox()
            }
            treePop()
        }
    }

    override fun load(value: JsonObject) {
        val values = value.getAsJsonArray("selected").map { it.asString }
        val tmp = mutableSetOf<Pair<T, Boolean>>()
        for (i in this.value.indices) {
            val selected = this.value.elementAt(i).first
            tmp.add(selected to values.contains(selected.key))
        }
        this.value = tmp
    }

    override fun save(value: JsonObject) {
        val selected = JsonArray()
        for (tmp in this.value) {
            if (tmp.second)
                selected.add(tmp.first.key)
        }
        value.add("selected", selected)
    }
}

/**
 * A specific implementation of the [SettingClient] class for multi-option settings.
 *
 * @param name The name of the setting.
 * @param desc The description of the setting.
 * @param value The initial value of the multi-option setting.
 * @param hide A lambda function that determines whether this setting is hidden or not.
 * @param change A lambda function that will be called when the value of the setting changes.
 */
fun IFeature.multiOption(
    name: String,
    desc: String? = null,
    value: Set<Pair<String, Boolean>>,
    hide: () -> Boolean = { false },
    change: (Set<Pair<String, Boolean>>) -> Unit = {}
) = SettingClientMultiOptionString(this::class, name, desc, value, hide, change)

/**
 * A specific implementation of the [SettingClient] class for multi-option settings.
 *
 * @param name The name of the setting.
 * @param desc The description of the setting.
 * @param value The initial value of the multi-option setting.
 * @param hide A lambda function that determines whether this setting is hidden or not.
 * @param change A lambda function that will be called when the value of the setting changes.
 */
fun <T : OptionEnum> IFeature.multiOption(
    name: String,
    desc: String? = null,
    value: Set<Pair<T, Boolean>>,
    hide: () -> Boolean = { false },
    change: (Set<Pair<T, Boolean>>) -> Unit = {}
) = SettingClientMultiOptionEnum(this::class, name, desc, value, hide, change)

/**
 * A specific implementation of the [SettingClient] class for multi-option settings.
 *
 * @param name The name of the setting.
 * @param desc The description of the setting.
 * @param value The initial value of the multi-option setting.
 * @param hide A lambda function that determines whether this setting is hidden or not.
 * @param change A lambda function that will be called when the value of the setting changes.
 */
fun <T : OptionEnum> IFeature.multiOption(
    name: String,
    desc: String? = null,
    value: List<T>,
    hide: () -> Boolean = { false },
    change: (Set<Pair<T, Boolean>>) -> Unit = {}
) = multiOption(name, desc, value.map { it to false }.toSet(), hide, change)
