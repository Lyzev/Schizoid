/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.render

import dev.lyzev.api.animation.EasingFunction
import dev.lyzev.api.animation.TimeAnimator
import dev.lyzev.api.events.EventGetFOV
import dev.lyzev.api.events.EventGetMouseSensitivity
import dev.lyzev.api.events.EventListener
import dev.lyzev.api.events.on
import dev.lyzev.api.glfw.GLFWKey
import dev.lyzev.api.setting.settings.keybinds
import dev.lyzev.api.setting.settings.option
import dev.lyzev.api.setting.settings.slider
import dev.lyzev.api.setting.settings.switch
import dev.lyzev.api.settings.Setting.Companion.neq
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.ModuleToggleable
import net.minecraft.client.util.InputUtil
import net.minecraft.util.math.MathHelper

/**
 * This enum represents the possible units of the timer.
 * It can be either seconds or ticks.
 */
object ModuleToggleableZoom : ModuleToggleable("Zoom", "Allows you to zoom.", category = IFeature.Category.RENDER),
    EventListener {

    /**
     * The fov of the zoom.
     */
    val fov by slider("FOV", "The fov of the zoom.", 10f, 1f, 30f, allowOutOfBounds = true)

    /**
     * The sensitivity multiplier of the zoom. Changes by default nothing.
     * */
    val sensitivityMultiplier by slider("Sentitivity Multiplier", "The sensifivity multiplier applied while zooming.", 1F, 0f, 1f, allowOutOfBounds = true)

    val smoothCamera by switch("Smooth Camera", "Enables the smooth camera.", false)

    /**
     * The keys to zoom.
     */
    val zoomKeys by keybinds("Zoom Key", "The key to zoom.", setOf(GLFWKey.C))

    val animation by switch("Animation", "Enables the animation.", true)

    /**
     * The animation type.
     */
    val animationType by option(
        "Animation Type",
        "The animation type.",
        EasingFunction.IN_SINE,
        EasingFunction.entries,
        hide = ::animation neq true
    )

    override val shouldHandleEvents: Boolean
        get() = isEnabled && isIngame && mc.currentScreen == null

    init {
        val timeAnimator = TimeAnimator(100)
        slider(
            "Animation Speed",
            "The speed of the animation.",
            100,
            1,
            1000,
            "ms",
            true,
            hide = ::animation neq true
        ) {
            timeAnimator.animationLength = it.toLong()
        }
        var originalFov = 0.0
        var originalSmoothCamera = false
        var isZooming = false
        timeAnimator.setReversed(true)
        on<EventGetMouseSensitivity> { event ->
            if (isZooming) {
                event.sensitivity *= sensitivityMultiplier
            }
        }
        on<EventGetFOV> { event ->
            var isKeyDown = false
            zoomKeys.forEach { key ->
                if (InputUtil.isKeyPressed(mc.window.handle, key.code)) {
                    if (!isZooming) {
                        timeAnimator.setReversed(false)
                        originalSmoothCamera = mc.options.smoothCameraEnabled
                        mc.options.smoothCameraEnabled = smoothCamera
                    }
                    if (animation) event.fov =
                        MathHelper.lerp(animationType(timeAnimator.getProgress()), originalFov, fov.toDouble())
                    else event.fov = fov.toDouble()
                    isZooming = true
                    isKeyDown = true
                }
            }
            if (isZooming && !isKeyDown) {
                timeAnimator.setReversed(true)
                isZooming = false
                mc.options.smoothCameraEnabled = originalSmoothCamera
            }
            if (!isKeyDown) {
                originalFov = event.fov
                if (animation) event.fov =
                    MathHelper.lerp(animationType(timeAnimator.getProgress()), originalFov, fov.toDouble())
                else event.fov = originalFov
            }
        }
    }
}
