/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.render

import dev.lyzev.api.events.EventClipToSpace
import dev.lyzev.api.events.EventListener
import dev.lyzev.api.events.on
import dev.lyzev.api.setting.settings.slider
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.ModuleToggleable

object ModuleToggleableViewClip :
    ModuleToggleable("View Clip", "Makes third person view clip through walls.", category = IFeature.Category.RENDER),
    EventListener {

    val desiredCameraDistance by slider(
        "Desired Camera Distance",
        "The desired camera distance.",
        4f,
        0f,
        10f,
        1,
        "blocks",
        true
    )

    override val shouldHandleEvents: Boolean
        get() = isEnabled

    init {
        on<EventClipToSpace> { event ->
            event.desiredCameraDistance = desiredCameraDistance
        }
    }
}
