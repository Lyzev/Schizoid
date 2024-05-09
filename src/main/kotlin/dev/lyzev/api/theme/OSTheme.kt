/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.theme

import com.sun.jna.platform.win32.*
import com.sun.jna.platform.win32.WinReg.HKEYByReference
import dev.lyzev.api.events.EventListener
import dev.lyzev.api.events.EventOSThemeUpdate
import dev.lyzev.api.events.EventShutdown
import dev.lyzev.api.events.on
import kotlin.concurrent.thread

val theme: OSTheme = when {
    System.getProperty("os.name").lowercase().startsWith("windows") -> WindowsTheme
    else -> throw RuntimeException("OS ${System.getProperty("os.name")} not supported")
}

interface OSTheme {

    companion object: OSTheme by theme

    enum class Theme {
        Dark, Light
    }

    fun getCurrentTheme(): Theme

    fun startListenForUpdatesThread()

}

object WindowsTheme: OSTheme, EventListener {

    private const val regKey = "Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize"

    val keyRef = HKEYByReference()
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
        val key = if (!Advapi32Util.registryKeyExists(WinReg.HKEY_CURRENT_USER, regKey)) {
            null
        } else if (Advapi32Util.registryValueExists(WinReg.HKEY_CURRENT_USER, regKey, "AppsUseLightTheme")) {
            "AppsUseLightTheme"
        } else if (Advapi32Util.registryValueExists(WinReg.HKEY_CURRENT_USER, regKey, "SystemUsesLightTheme")) {
            "SystemUsesLightTheme"
        } else {
            null
        }
        return if (key == null || Advapi32Util.registryGetIntValue(WinReg.HKEY_CURRENT_USER, regKey, key) == 0) {
            OSTheme.Theme.Dark
        } else {
            OSTheme.Theme.Light
        }
    }

    override fun startListenForUpdatesThread() {
        Advapi32.INSTANCE.RegOpenKeyEx(WinReg.HKEY_CURRENT_USER, regKey, 0, WinNT.KEY_NOTIFY, keyRef)
        listenForUpdatesThread.start()
        on<EventShutdown> {
            isShuttingDown = true
            listenForUpdatesThread.interrupt()
            Advapi32.INSTANCE.RegCloseKey(keyRef.value)
        }

    }

    override val shouldHandleEvents = true

}
