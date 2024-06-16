/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.cryptography.hwid

import java.util.*

val HWID = UUID.nameUUIDFromBytes((System.getProperty("user.name") + System.getProperty("user.home") + System.getProperty("os.version") + System.getProperty("os.arch")).encodeToByteArray())
