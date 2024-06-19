/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.render

import dev.lyzev.api.animation.EasingFunction
import dev.lyzev.api.animation.TimeAnimator
import dev.lyzev.api.events.*
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
import kotlin.math.sign

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
     * The weight of the mouse movement applied while zooming.
     */
    val weight by slider("Weight", "The weight of the mouse movement applied while zooming.", 20, 1, 100, "%%")

    val smoothCamera by switch("Smooth Camera", "Enables the smooth camera.", false)

    val scrollable by switch("Scrolling", "Enables zooming with the mouse wheel.", true)

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
        var scrollOffset = 0
        timeAnimator.setReversed(true)
        on<EventUpdateMouse> {
            if (!isZooming)
                return@on
            val mouse = mc.mouse
            val weight = weight / 100f
            mouse.cursorDeltaX *= weight
            mouse.cursorDeltaY *= weight
        }
        on<EventMouseScroll> { event ->
            if (isZooming && scrollable) {
                event.isCancelled = true
                scrollOffset += sign(event.vertical).toInt()
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
                    scrollOffset = scrollOffset.coerceIn(fov.toInt() - 70, fov.toInt() - 1)
                    event.fov = (if (scrollable) -scrollOffset else 0) + if (animation) MathHelper.lerp(
                        animationType(timeAnimator.getProgress()), originalFov, fov.toDouble()
                    )
                    else fov.toDouble()
                    isZooming = true
                    isKeyDown = true
                }
            }
            if (isZooming && !isKeyDown) {
                timeAnimator.setReversed(true)
                isZooming = false
                scrollOffset = 0
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
