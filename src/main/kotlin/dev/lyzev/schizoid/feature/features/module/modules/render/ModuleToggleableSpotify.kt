/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.render

import dev.lyzev.api.animation.EasingFunction
import dev.lyzev.api.animation.TimeAnimator
import dev.lyzev.api.events.EventScheduleTask
import dev.lyzev.api.events.on
import dev.lyzev.api.imgui.render.ImGuiRenderable
import dev.lyzev.api.opengl.WrappedNativeImageBackedTexture
import dev.lyzev.api.setting.settings.OptionEnum
import dev.lyzev.api.setting.settings.multiOption
import dev.lyzev.api.setting.settings.slider
import dev.lyzev.api.theme.WindowsTheme
import dev.lyzev.api.theme.theme
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.ModuleToggleableRenderImGuiContent
import dev.lyzev.wrmi.TrackInfo
import dev.lyzev.wrmi.WindowsReadMediaInfo
import imgui.ImGui
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiStyleVar
import imgui.flag.ImGuiWindowFlags
import net.minecraft.client.texture.NativeImage

object ModuleToggleableSpotify :
    ModuleToggleableRenderImGuiContent("Spotify", "ImGui Spotify player.", category = IFeature.Category.RENDER) {

    val infos by multiOption("Infos", "The information to display.", Info.entries.map { it to true }.toSet())
    val size by slider("Size", "The size of the Spotify player.", 5, 0, 100, "%%")

    var trackInfo: TrackInfo? = null

    override fun renderImGuiContent() {
        if (trackInfo == null) {
            ImGui.text("No track playing.")
            return
        }
        infos.forEach {
            if (it.second)
                it.first.render()
        }
    }

    override val hide = theme != WindowsTheme

    override val shouldHandleEvents: Boolean
        get() = super.shouldHandleEvents && !hide

    init {
        on<EventScheduleTask> {
            val trackInfo = WindowsReadMediaInfo.getCurrentMediaInfo().firstOrNull { it.owner == "Spotify.exe" }
            if (trackInfo == null) {
                this.trackInfo = null
            } else {
                Info.Progress.update(trackInfo)
                if (this.trackInfo == null || (this.trackInfo!!.title != trackInfo.title && this.trackInfo!!.artist != trackInfo.artist)) {
                    Info.Title.update(trackInfo)
                    Info.Artist.update(trackInfo)
                    Info.Cover.update(trackInfo)
                }
                this.trackInfo = trackInfo
            }
        }
    }

    enum class Info : OptionEnum, ImGuiRenderable {
        Title {

            private var title: String? = null
            private val timeAnimator = TimeAnimator(3000)

            override fun update(trackInfo: TrackInfo) {
                title = trackInfo.title
            }

            override fun render() {
                if (title != null && title!!.isNotEmpty()) {
                    ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0f, 0f)
                    val size = ImGui.calcTextSize(title)
                    if (ImGui.beginChild("##Title", width, size.y, false, ImGuiWindowFlags.NoScrollbar or ImGuiWindowFlags.NoBackground)) {
                        if (size.x > ImGui.getColumnWidth()) {
                            val progress = timeAnimator.getProgressNotClamped()
                            if (progress < -.2 || progress > 1.2)
                                timeAnimator.setReversed(!timeAnimator.reversed)
                            ImGui.setScrollX((EasingFunction.LINEAR(timeAnimator.getProgress()) * (size.x - ImGui.getColumnWidth())).toFloat())
                        }
                        ImGui.text(title)
                    }
                    ImGui.endChild()
                    ImGui.popStyleVar()
                }
            }
        }, Artist {

            private var artist: String? = null
            private val timeAnimator = TimeAnimator(3000)

            override fun update(trackInfo: TrackInfo) {
                artist = trackInfo.artist
            }

            override fun render() {
                if (artist != null) {
                    ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0f, 0f)
                    val size = ImGui.calcTextSize(artist)
                    if (ImGui.beginChild("##Artist", width, size.y, false, ImGuiWindowFlags.NoScrollbar or ImGuiWindowFlags.NoBackground)) {
                        if (size.x > ImGui.getColumnWidth()) {
                            val progress = timeAnimator.getProgressNotClamped()
                            if (progress < -.2 || progress > 1.2)
                                timeAnimator.setReversed(!timeAnimator.reversed)
                            ImGui.setScrollX((EasingFunction.LINEAR(timeAnimator.getProgress()) * (size.x - ImGui.getColumnWidth())).toFloat())
                        }
                        ImGui.text(artist)
                    }
                    ImGui.endChild()
                    ImGui.popStyleVar()
                }
            }
        }, Progress {

            private var time = 0L
            private var trackInfo: TrackInfo? = null

            override fun update(trackInfo: TrackInfo) {
                if (time == 0L || (this.trackInfo != null && this.trackInfo!!.time != trackInfo.time))
                    time = System.currentTimeMillis() - trackInfo.time
                this.trackInfo = trackInfo
            }

            override fun render() {
                val time = System.currentTimeMillis() - this.time
                ImGui.pushStyleColor(ImGuiCol.PlotHistogram, ImGui.getColorU32(ImGuiCol.Button))
                ImGui.progressBar(time.toFloat() / trackInfo!!.duration.toFloat(), width, 3f, "")
                ImGui.popStyleColor()
            }
        }, Cover {

            private var trackCover: WrappedNativeImageBackedTexture? = null

            override fun update(trackInfo: TrackInfo) {
                if (trackCover != null)
                    trackCover!!.close()
                trackCover = null
            }

            override fun render() {
                if (trackCover == null) {
                    trackCover = WrappedNativeImageBackedTexture(NativeImage.read(trackInfo!!.thumbnailPng.inputStream()))
                    trackCover!!.upload()
                }
                ImGui.image(trackCover!!.glId, width, trackCover!!.image!!.height * width / trackCover!!.image!!.width)
            }
        };

        val width: Float
            get() = mc.window.framebufferWidth * (.05f + .1f * (size / 100f))

        override val key = name

        abstract fun update(trackInfo: TrackInfo)
    }
}
