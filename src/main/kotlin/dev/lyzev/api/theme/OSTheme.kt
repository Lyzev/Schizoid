/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.theme

import com.sun.jna.platform.win32.*
import com.sun.jna.platform.win32.WinReg.HKEYByReference
import dev.lyzev.api.events.*
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.concurrent.thread

val theme: OSTheme = when {
    System.getProperty("os.name").lowercase().startsWith("windows") -> WindowsTheme
    System.getProperty("os.name").lowercase().startsWith("mac") -> MacOSTheme
    System.getProperty("os.name").contains("linux", true) -> LinuxTheme
    else -> throw RuntimeException("OS ${System.getProperty("os.name")} not supported")
}

interface OSTheme : EventListener {

    companion object: OSTheme by theme

    enum class Theme {
        Dark, Light
    }

    fun getCurrentTheme(): Theme

    fun startListenForUpdatesThread()

    override val shouldHandleEvents: Boolean
        get() = theme == this
}

// Windows implementation
object WindowsTheme : OSTheme {

    private const val REG_KEY = "Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize"

    private val keyRef = HKEYByReference()
    private var isShuttingDown = false

    private val listenForUpdatesThread = thread(false) {
        while (true) {
            val handle = Kernel32.INSTANCE.CreateEvent(null, true, false, null)
            Advapi32.INSTANCE.RegNotifyChangeKeyValue(keyRef.value, true, WinNT.REG_NOTIFY_CHANGE_LAST_SET or WinNT.REG_NOTIFY_CHANGE_NAME, handle, true)
            val res = Kernel32.INSTANCE.WaitForSingleObject(handle, 1000)
            if (res == WinBase.WAIT_OBJECT_0 && !isShuttingDown) {
                EventOSThemeUpdate(getCurrentTheme()).fire()
            }
            Kernel32.INSTANCE.CloseHandle(handle)
        }
    }

    override fun getCurrentTheme(): OSTheme.Theme {
        val key = if (!Advapi32Util.registryKeyExists(WinReg.HKEY_CURRENT_USER, REG_KEY)) {
            null
        } else if (Advapi32Util.registryValueExists(WinReg.HKEY_CURRENT_USER, REG_KEY, "AppsUseLightTheme")) {
            "AppsUseLightTheme"
        } else if (Advapi32Util.registryValueExists(WinReg.HKEY_CURRENT_USER, REG_KEY, "SystemUsesLightTheme")) {
            "SystemUsesLightTheme"
        } else {
            null
        }
        return if (key == null || Advapi32Util.registryGetIntValue(WinReg.HKEY_CURRENT_USER, REG_KEY, key) == 0) {
            OSTheme.Theme.Dark
        } else {
            OSTheme.Theme.Light
        }
    }

    override fun startListenForUpdatesThread() {
        Advapi32.INSTANCE.RegOpenKeyEx(WinReg.HKEY_CURRENT_USER, REG_KEY, 0, WinNT.KEY_NOTIFY, keyRef)
        listenForUpdatesThread.start()
        on<EventShutdown> {
            isShuttingDown = true
            listenForUpdatesThread.interrupt()
            Advapi32.INSTANCE.RegCloseKey(keyRef.value)
        }
    }
}

// MacOS implementation
object MacOSTheme : OSTheme {

    private var job: Job? = null
    private var lastTheme = getCurrentTheme()

    override fun getCurrentTheme(): OSTheme.Theme {
        val process = ProcessBuilder("defaults", "read", "-g", "AppleInterfaceStyle").start()
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        val theme = reader.readLine()
        return if (theme == "Dark") OSTheme.Theme.Dark else OSTheme.Theme.Light
    }

    override fun startListenForUpdatesThread() {
        on<EventScheduleTask> {
            val theme = getCurrentTheme()
            if (lastTheme != theme) {
                EventOSThemeUpdate(theme).fire()
                lastTheme = theme
            }
        }
    }
}

// Linux implementation
object LinuxTheme : OSTheme {

    private var job: Job? = null
    private var lastTheme = MacOSTheme.getCurrentTheme()

    override fun getCurrentTheme(): OSTheme.Theme {
        val process = ProcessBuilder("gsettings", "get", "org.gnome.desktop.interface", "gtk-theme").start()
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        val theme = reader.readLine()
        return if (theme.contains("dark", ignoreCase = true)) OSTheme.Theme.Dark else OSTheme.Theme.Light
    }

    override fun startListenForUpdatesThread() {
        on<EventScheduleTask> {
            val theme = getCurrentTheme()
            if (lastTheme != theme) {
                EventOSThemeUpdate(theme).fire()
                lastTheme = theme
            }
        }
    }
}
