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
import dev.lyzev.api.opengl.WrappedNativeImageBackedTexture
import dev.lyzev.api.setting.SettingClient
import dev.lyzev.schizoid.Schizoid
import dev.lyzev.schizoid.feature.IFeature
import imgui.ImGui.*
import net.minecraft.client.texture.NativeImage
import org.lwjgl.glfw.GLFW
import kotlin.reflect.KClass

/**
 * A specific implementation of the [Setting] class for keybind settings.
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

    private var isListening = false

    override fun render() {
        val treeNode = treeNode(name)
        if (desc != null && isItemHovered()) setTooltip(desc)
        sameLine(getWindowContentRegionMaxX() - 10.5f - getStyle().windowPaddingX)
        if (imageButton(if (!isListening) add!!.glId else hourglass!!.glId, 8.75f, 8.75f)) {
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
            val height = value.size * 26f + 2f
            if (beginListBox("", -1f, if (height > MAX_HEIGHT) MAX_HEIGHT else height)) {
                itemsToRemove.clear()
                for (key in value) {
                    if (selectable(key.name)) itemsToRemove.add(key)
                    sameLine(getWindowContentRegionMaxX() - 8.75f / 2f - getStyle().windowPaddingX)
                    pushID(key.name)
                    if (imageButton(x!!.glId, 8.75f, 8.75f)) itemsToRemove.add(key)
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
        private const val MAX_HEIGHT = 5f * 26f + 2f
        private val itemsToRemove = mutableSetOf<GLFWKey>()
        private val x by lazy { javaClass.classLoader.getResourceAsStream("assets/${Schizoid.MOD_ID}/textures/x.png")
            ?.use { WrappedNativeImageBackedTexture(NativeImage.read(it)) } }
        private val hourglass by lazy { javaClass.classLoader.getResourceAsStream("assets/${Schizoid.MOD_ID}/textures/hourglass.png")
            ?.use { WrappedNativeImageBackedTexture(NativeImage.read(it)) } }
        private val add by lazy { javaClass.classLoader.getResourceAsStream("assets/${Schizoid.MOD_ID}/textures/add.png")
            ?.use { WrappedNativeImageBackedTexture(NativeImage.read(it)) } }
    }
}

fun IFeature.keybinds(
    name: String,
    desc: String? = null,
    value: MutableSet<GLFWKey>,
    hide: () -> Boolean = { false },
    change: (MutableSet<GLFWKey>) -> Unit = {}
) = SettingClientKeybinds(this::class, name, desc, value, hide, change)
