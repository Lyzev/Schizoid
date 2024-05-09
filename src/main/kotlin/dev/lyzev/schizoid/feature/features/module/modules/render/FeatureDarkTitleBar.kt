/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.render

import com.sun.jna.Pointer
import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.ptr.IntByReference
import dev.lyzev.api.events.EventListener
import dev.lyzev.api.events.EventOSThemeUpdate
import dev.lyzev.api.events.on
import dev.lyzev.api.jna.DwmApi
import dev.lyzev.api.setting.settings.OptionEnum
import dev.lyzev.api.setting.settings.option
import dev.lyzev.api.theme.OSTheme
import dev.lyzev.schizoid.feature.Feature
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWNativeWin32

object FeatureDarkTitleBar: Feature("Dark Title Bar", "Adds a dark Title Bar", emptySet(), Category.RENDER), EventListener {

    enum class Mode: OptionEnum {
        Light, Dark, System;

        override val key: String
            get() = name
    }

    val mode by option("Mode", "Mode", Mode.System, enumValues<Mode>().toList(), change = ::setTitleBarColor)

    init {
        setTitleBarColor(mode)

        on<EventOSThemeUpdate> {
            if (mode == Mode.System) {
                setTitleBarColor(if (it.theme == OSTheme.Theme.Dark) Mode.Dark else Mode.Light)
            }
        }
    }

    private fun setTitleBarColor(value: Mode) {
        val dark = when (value) {
            Mode.Light -> false
            Mode.Dark -> true
            Mode.System -> OSTheme.getCurrentTheme() == OSTheme.Theme.Dark
        }
        val win32 = GLFWNativeWin32.glfwGetWin32Window(mc.window.handle)
        val hwnd = HWND(Pointer(win32))
        DwmApi.DwmSetWindowAttribute(hwnd, 20, IntByReference(if (dark) 1 else 0), 4)

        // Workaround: Reload window
        GLFW.glfwHideWindow(mc.window.handle)
        GLFW.glfwShowWindow(mc.window.handle)
    }

    override fun keybindReleased() {
    }

    override val shouldHandleEvents = true
}
