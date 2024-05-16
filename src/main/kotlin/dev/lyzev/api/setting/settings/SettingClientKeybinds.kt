/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.setting.settings

import dev.lyzev.api.events.EventKeybindsRequest
import dev.lyzev.api.events.EventKeybindsResponse
import dev.lyzev.api.events.EventListener
import dev.lyzev.api.events.on
import dev.lyzev.api.glfw.GLFWKey
import dev.lyzev.api.imgui.font.icon.FontAwesomeIcons
import dev.lyzev.api.setting.SettingClient
import dev.lyzev.schizoid.feature.IFeature
import imgui.ImGui.*
import kotlinx.serialization.json.*
import org.lwjgl.glfw.GLFW
import kotlin.math.max
import kotlin.reflect.KClass

/**
 * A specific implementation of the [SettingClient] class for keybind settings.
 *
 * @param container The class of the settings container where this setting belongs.
 * @param name The name of the setting.
 * @param value The initial value of the integer setting.
 * @param hide A lambda function that determines whether this setting is hidden or not.
 * @param change A lambda function that will be called when the value of the setting changes.
 */
class SettingClientKeybinds(
    container: KClass<out IFeature>,
    name: String,
    desc: String?,
    value: Set<GLFWKey>,
    val blacklist: Set<GLFWKey>,
    hide: () -> Boolean,
    change: (Set<GLFWKey>) -> Unit
) : SettingClient<Set<GLFWKey>>(container, name, desc, value, hide, change), EventListener {

    // A flag to check if the keybind is listening for input
    private var isListening = false

    override fun render() {
        val treeNode = treeNode(name)
        if (desc != null && isItemHovered()) setTooltip(desc)
        sameLine(max(getWindowContentRegionMaxX() - 10.5f - getStyle().windowPaddingX, 130f))
        if (FontAwesomeIcons.button(if (!isListening) FontAwesomeIcons.Plus else FontAwesomeIcons.Hourglass)) {
            isListening = true
            EventKeybindsRequest.fire()
        }
        if (isItemHovered()) setTooltip(
            """
                Press to bind.
                ESC = ABORT
                """.trimIndent()
        )
        if (treeNode) {
            if (beginListBox("", -1f, calcHeight(value.size))) {
                itemsToRemove.clear()
                if (value.isEmpty()) {
                    textDisabled("Looks like there's nothing here.")
                }
                for (key in value) {
                    pushID(key.name)
                    text(key.name)
                    sameLine(max(getWindowContentRegionMaxX() - 8.75f / 2f - getStyle().windowPaddingX, calcTextSize(key.name).x + getStyle().framePaddingX + 2))
                    if (FontAwesomeIcons.button(FontAwesomeIcons.Trash))
                        itemsToRemove.add(key)
                    if (isItemHovered()) setTooltip("Click to remove.")
                    popID()
                }
                endListBox()
            }
            value = value.minus(itemsToRemove)
            if (itemsToRemove.isNotEmpty()) {
                onChange(value)
            }

            treePop()
        }
    }

    override fun load(value: JsonElement) {
        this.value = value.jsonArray.map { GLFWKey.valueOf(it.jsonPrimitive.content) }.toSet()
    }

    override fun save(): JsonElement {
        return JsonArray(value.map { JsonPrimitive(it.name) })
    }

    override val shouldHandleEvents: Boolean
        get() = isListening

    init {
        /**
         * Listens for keybind events and adds the key to the value set.
         */
        on<EventKeybindsResponse> {
            if (isListening) {
                isListening = false
                if (it.key != GLFW.GLFW_KEY_ESCAPE && it.key != GLFW.GLFW_KEY_UNKNOWN && !blacklist.contains(GLFWKey[it.key])) {
                    this.value = this.value.plus(GLFWKey[it.key])
                    change(this.value)
                }
            }
        }
    }

    companion object {
        /**
         * A set of items to remove from the list.
         */
        private val itemsToRemove = mutableSetOf<GLFWKey>()
    }
}

/**
 * Creates a new keybind setting.
 *
 * @param name The name of the setting.
 * @param desc The description of the setting.
 * @param value The initial value of the keybind setting.
 * @param hide A lambda function that determines whether this setting is hidden or not.
 * @param change A lambda function that will be called when the value of the setting changes.
 *
 * @return The created keybind setting.
 */
fun IFeature.keybinds(
    name: String,
    desc: String? = null,
    value: Set<GLFWKey>,
    blacklist: Set<GLFWKey> = emptySet(),
    hide: () -> Boolean = { false },
    change: (Set<GLFWKey>) -> Unit = {}
) = SettingClientKeybinds(this::class, name, desc, value, blacklist, hide, change)
