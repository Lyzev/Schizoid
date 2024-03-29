/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.setting.settings

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dev.lyzev.api.events.EventKeybindsRequest
import dev.lyzev.api.events.EventKeybindsResponse
import dev.lyzev.api.events.EventListener
import dev.lyzev.api.events.on
import dev.lyzev.api.glfw.GLFWKey
import dev.lyzev.api.imgui.font.ImGuiFonts
import dev.lyzev.api.imgui.font.icon.FontAwesomeIcons
import dev.lyzev.api.opengl.WrappedNativeImageBackedTexture
import dev.lyzev.api.setting.SettingClient
import dev.lyzev.schizoid.Schizoid
import dev.lyzev.schizoid.feature.IFeature
import imgui.ImGui.*
import net.minecraft.client.texture.NativeImage
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
    value: MutableSet<GLFWKey>,
    hide: () -> Boolean,
    val change: (MutableSet<GLFWKey>) -> Unit
) : SettingClient<MutableSet<GLFWKey>>(container, name, desc, value, hide, change), EventListener {

    // A flag to check if the keybind is listening for input
    private var isListening = false

    override fun render() {
        val treeNode = treeNode(name)
        if (desc != null && isItemHovered()) setTooltip(desc)
        sameLine(max(getWindowContentRegionMaxX() - 10.5f - getStyle().windowPaddingX, 130f))
        ImGuiFonts.FONT_AWESOME_SOLID.begin()
        if (button(if (!isListening) FontAwesomeIcons.Plus else FontAwesomeIcons.Hourglass, 17.5f, 17.5f)) {
            isListening = true
            EventKeybindsRequest.fire()
        }
        ImGuiFonts.FONT_AWESOME_SOLID.end()
        if (isItemHovered()) setTooltip(
            """
                Press to bind.
                ESC = ABORT
                """.trimIndent()
        )
        if (treeNode) {
            val height = value.size * 26f + 2f
            if (beginListBox("", -1f, if (height > MAX_HEIGHT) MAX_HEIGHT else height)) {
                itemsToRemove.clear()
                for (key in value) {
                    if (selectable(key.name)) itemsToRemove.add(key)
                    sameLine(getWindowContentRegionMaxX() - 8.75f / 2f - getStyle().windowPaddingX)
                    pushID(key.name)
                    ImGuiFonts.FONT_AWESOME_SOLID.begin()
                    if (button(FontAwesomeIcons.Trash, 17.5f, 17.5f)) itemsToRemove.add(key)
                    ImGuiFonts.FONT_AWESOME_SOLID.end()
                    if (isItemHovered()) setTooltip("Click to remove.")
                    popID()
                }
                endListBox()
            }
            value.removeAll(itemsToRemove)
            if (itemsToRemove.isNotEmpty()) {
                change(value)
            }

            treePop()
        }
    }

    override fun load(value: JsonObject) {
        val keys = value["keys"].asJsonArray
        this.value.clear()
        keys.map { GLFWKey.valueOf(it.asString) }.forEach(this.value::add)
    }

    override fun save(value: JsonObject) =
        value.add("keys", JsonArray().apply { this@SettingClientKeybinds.value.map { it.name }.forEach(this::add) })

    override val shouldHandleEvents: Boolean
        get() = isListening

    init {
        /**
         * Listens for keybind events and adds the key to the value set.
         */
        on<EventKeybindsResponse> {
            if (isListening) {
                isListening = false
                if (it.key != GLFW.GLFW_KEY_ESCAPE && it.key != GLFW.GLFW_KEY_UNKNOWN) {
                    value.add(GLFWKey[it.key])
                    change(value)
                }
            }
        }
    }

    companion object {
        /**
         * The maximum height of the list box.
         */
        private const val MAX_HEIGHT = 5f * 26f + 2f

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
    value: MutableSet<GLFWKey>,
    hide: () -> Boolean = { false },
    change: (MutableSet<GLFWKey>) -> Unit = {}
) = SettingClientKeybinds(this::class, name, desc, value, hide, change)
