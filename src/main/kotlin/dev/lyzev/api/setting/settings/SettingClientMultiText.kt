/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.setting.settings

import dev.lyzev.api.imgui.font.icon.FontAwesomeIcons
import dev.lyzev.api.setting.SettingClient
import dev.lyzev.schizoid.feature.IFeature
import imgui.ImGui.*
import imgui.flag.ImGuiInputTextFlags
import imgui.type.ImString
import kotlinx.serialization.json.*
import kotlin.math.max
import kotlin.reflect.KClass

/**
 * A specific implementation of the [SettingClient] class for multi-text settings.
 *
 * @param container The class of the settings container where this setting belongs.
 * @param name The name of the setting.
 * @param desc The description of the setting.
 * @param value The initial value of the multi-text setting.
 * @param hide A lambda function that determines whether this setting is hidden or not.
 * @param change A lambda function that will be called when the value of the setting changes.
 */
class SettingClientMultiText(
    container: KClass<out IFeature>,
    name: String,
    desc: String?,
    value: Set<String>,
    val upperCase: Boolean,
    hide: () -> Boolean,
    change: (Set<String>) -> Unit
) : SettingClient<Set<String>>(container, name, desc, value, hide, change) {

    private val add = ImString()

    override fun render() {
        val treeNode = treeNode(name)
        if (desc != null && isItemHovered()) setTooltip(desc)
        if (treeNode) {
            setNextItemWidth(getColumnWidth())
            if (inputTextWithHint(
                    "##AddText",
                    "Add text...",
                    add,
                    ImGuiInputTextFlags.EnterReturnsTrue or if (upperCase) ImGuiInputTextFlags.CharsUppercase else 0
                )
            ) {
                if (add.get().isNotEmpty()) {
                    value = value.plus(add.get())
                    onChange(value)
                    add.set("")
                }
            }
            if (isItemHovered()) setTooltip("Press enter to add.")
            if (beginListBox("", -1f, calcHeight(value.size))) {
                itemsToRemove.clear()
                if (value.isEmpty()) {
                    textDisabled("Looks like there's nothing here.")
                }
                for (i in value.indices) {
                    val text = value.elementAt(i)
                    pushID(text)
                    text(text)
                    sameLine(
                        max(
                            getWindowContentRegionMaxX() - 17.5f * 2 - getStyle().windowPaddingX,
                            calcTextSize(text).x + getStyle().framePaddingX + 2
                        )
                    )
                    if (FontAwesomeIcons.button(FontAwesomeIcons.Edit)) {
                        openPopup("Edit")
                        buffer.set(text)
                        focus = true
                    }
                    if (isItemHovered())
                        setTooltip("Click to edit.")
                    sameLine()
                    if (FontAwesomeIcons.button(FontAwesomeIcons.Trash))
                        itemsToRemove.add(text)
                    if (isItemHovered()) setTooltip("Click to remove.")
                    if (beginPopup("Edit")) {
                        if (focus) {
                            setKeyboardFocusHere()
                            focus = false
                        }
                        if (inputText(
                                "##EditText",
                                buffer,
                                ImGuiInputTextFlags.EnterReturnsTrue or if (upperCase) ImGuiInputTextFlags.CharsUppercase else 0
                            )
                        ) {
                            if (buffer.get().isNotEmpty()) {
                                if (text != buffer.get()) {
                                    edit(text, buffer.get())
                                }
                            } else {
                                itemsToRemove.add(text)
                            }
                            closeCurrentPopup()
                        }
                        if (isItemHovered()) setTooltip("Press enter to save.")
                        endPopup()
                    }
                    popID()
                }
                value = value.minus(itemsToRemove)
                endListBox()
            }
            treePop()
        }
    }

    private fun edit(old: String, new: String) {
        value = value.minus(old).plus(new)
        onChange(value)
    }

    override fun load(value: JsonElement) {
        this.value =
            value.jsonArray.map { it.jsonPrimitive.content.let { i -> if (upperCase) i.uppercase() else i } }.toSet()
    }

    override fun save(): JsonElement {
        return JsonArray(value.map { JsonPrimitive(it) })
    }

    companion object {
        private val itemsToRemove = mutableSetOf<String>()

        private val buffer = ImString()

        private var focus = false
    }
}

/**
 * Creates a new multi-text setting.
 *
 * @param name The name of the setting.
 * @param desc The description of the setting.
 * @param value The initial value of the multi-text setting.
 * @param hide A lambda function that determines whether this setting is hidden or not.
 * @param change A lambda function that will be called when the value of the setting changes.
 */
fun IFeature.multiText(
    name: String,
    desc: String? = null,
    value: Set<String> = mutableSetOf(),
    upperCase: Boolean = false,
    hide: () -> Boolean = { false },
    change: (Set<String>) -> Unit = {}
) = SettingClientMultiText(this::class, name, desc, value, upperCase, hide, change)
