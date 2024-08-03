/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.render

import dev.lyzev.api.animation.EasingFunction
import dev.lyzev.api.animation.TimeAnimator
import dev.lyzev.api.events.EventListener
import dev.lyzev.api.events.EventRenderImGuiContent
import dev.lyzev.api.events.on
import dev.lyzev.api.imgui.font.ImGuiFonts.OPEN_SANS_BOLD
import dev.lyzev.api.imgui.font.ImGuiFonts.OPEN_SANS_REGULAR
import dev.lyzev.api.setting.settings.multiOption
import dev.lyzev.api.setting.settings.option
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.ModuleToggleable
import dev.lyzev.schizoid.feature.features.module.ModuleToggleableRenderImGuiContent.Companion.calc
import dev.lyzev.schizoid.feature.features.module.ModuleToggleableRenderImGuiContent.WindowFlags
import dev.lyzev.schizoid.feature.features.module.modules.render.ModuleToggleableNotifications.Notification.Type
import imgui.ImGui.*
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiWindowFlags
import imgui.type.ImBoolean
import net.minecraft.util.math.MathHelper

object ModuleToggleableNotifications :
    ModuleToggleable(
        "Notifications",
        "Displays notifications in the right bottom corner.",
        category = IFeature.Category.RENDER
    ), EventListener {

    val easingFunction by option(
        "Easing Function",
        "The easing function used for the animations.",
        EasingFunction.LINEAR,
        EasingFunction.entries
    )

    private var windowFlags = WindowFlags.DEFAULT

    private val notifications = mutableListOf<Notification>()

    fun info(text: String) = notifications.add(Notification(text, Type.INFO))

    fun warning(text: String) = notifications.add(Notification(text, Type.WARNING))

    fun error(text: String) = notifications.add(Notification(text, Type.ERROR))

    override val shouldHandleEvents: Boolean
        get() = isEnabled

    init {
        isEnabled = true // enabled by default

        multiOption("Window Flags", "The ImGui window flags of the notifications.", WindowFlags.entries) {
            windowFlags = it.calc(WindowFlags.DEFAULT)
        }

        on<EventRenderImGuiContent> {
            notifications.removeIf { it.isFinished() }
            notifications.forEachIndexed { index, notification ->
                notification.render(index)
            }
        }
    }

    class Notification(val text: String, val type: Type) {

        private val initTime = System.currentTimeMillis()
        private val textScrolling = TimeAnimator(4000)
        private val animation = TimeAnimator(200)

        private var lastIndex = -1
        private var lastIndexTime = System.currentTimeMillis()

        fun render(index: Int) {
            val duration = type.duration + animation.animationLength * 2f
            if (index == lastIndex) {
                lastIndexTime = System.currentTimeMillis()
            }
            if (lastIndex == -1 || (lastIndex != index && System.currentTimeMillis() - lastIndexTime > animation.animationLength)) {
                lastIndex = index
            }
            val lastIndexProgress = EasingFunction.LINEAR((System.currentTimeMillis() - lastIndexTime) / animation.animationLength.toDouble()).toFloat()
            val index = MathHelper.lerp(lastIndexProgress, lastIndex.toFloat(), index.toFloat())
            if (System.currentTimeMillis() - initTime >= duration - animation.animationLength) {
                if (!animation.reversed) {
                    animation.setReversed(true)
                }
            }
            val progress = easingFunction(animation.getProgress()).toFloat()
            val textProgress = EasingFunction.LINEAR(textScrolling.getProgress())
            val drawList = getForegroundDrawList()
            val x = mc.window.framebufferWidth - WIDTH - 10f + (WIDTH * (1f - progress))
            val y = mc.window.framebufferHeight - HEIGHT - 10f - index * (HEIGHT + 10)
            drawList.addRectFilled(
                x,
                y,
                x + WIDTH,
                y + HEIGHT,
                getColorU32(ImGuiCol.WindowBg),
                5f
            )
            val livingTime = (System.currentTimeMillis() - initTime) / duration
            drawList.addRectFilled(
                x,
                y + HEIGHT - 3f,
                x + WIDTH * (1f - livingTime),
                y + HEIGHT,
                getColorU32(ImGuiCol.Text, 0.4f),
                5f
            )
            drawList.addText(
                OPEN_SANS_BOLD.font,
                17.5f,
                x + WIDTH / 2f - OPEN_SANS_BOLD.font.calcTextSizeAX(17.5f, WIDTH, WIDTH, "\"${type.name}\"") / 2f,
                y + 10f,
                -1,
                "\"${type.name}\""
            )
            drawList.addText(
                OPEN_SANS_REGULAR.font,
                17.5f,
                x + 10f,
                y + 27.5f,
                -1,
                text,
                WIDTH - 20f
            )
        }

        fun isFinished() = System.currentTimeMillis() - initTime > (type.duration + animation.animationLength * 2f)

        init {
            textScrolling.setReversed(false)
            textScrolling.setProgress(0.0)
            animation.setReversed(false)
            animation.setProgress(0.0)
        }

        companion object {
            const val WIDTH = 250f
            const val HEIGHT = 80f
        }

        enum class Type(val duration: Long) {
            INFO(1000),
            WARNING(2000),
            ERROR(3000)
        }
    }
}
