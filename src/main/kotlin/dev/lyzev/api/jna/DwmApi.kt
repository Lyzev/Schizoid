/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */
package dev.lyzev.api.jna

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.PointerType
import com.sun.jna.platform.win32.WinDef.HWND

interface DwmApi : Library {

    fun DwmSetWindowAttribute(hwnd: HWND, dwAttribute: Int, pvAttribute: PointerType, cbAttribute: Int): Int

    companion object: DwmApi by Native.load("dwmapi", DwmApi::class.java)
}
