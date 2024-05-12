/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.render

import dev.lyzev.api.events.EventScheduleTask
import dev.lyzev.api.events.on
import dev.lyzev.api.opengl.WrappedNativeImageBackedTexture
import dev.lyzev.api.theme.WindowsTheme
import dev.lyzev.api.theme.theme
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.ModuleToggleableRenderImGuiContent
import dev.lyzev.wrmi.TrackInfo
import dev.lyzev.wrmi.WindowsReadMediaInfo
import imgui.ImGui
import net.minecraft.client.texture.NativeImage


object ModuleToggleableSpotify :
    ModuleToggleableRenderImGuiContent("Spotify", "ImGui Spotify player.", category = IFeature.Category.RENDER) {

    var trackInfo: TrackInfo? = null
    var trackCover: WrappedNativeImageBackedTexture? = null

    override fun renderImGuiContent() {
        if (trackInfo == null) {
            ImGui.text("No track playing.")
            return
        }
        ImGui.textWrapped(trackInfo!!.title)
        ImGui.textWrapped(trackInfo!!.artist)
        if (trackCover == null) {
            trackCover = WrappedNativeImageBackedTexture(NativeImage.read(trackInfo!!.thumbnailPng.inputStream()))
            trackCover!!.upload()
        }
        ImGui.image(trackCover!!.glId, trackCover!!.image!!.height / 3f, trackCover!!.image!!.width / 3f)
    }

    override val hide = theme != WindowsTheme
    override val shouldHandleEvents: Boolean
        get() = super.shouldHandleEvents && !hide

    init {
        on<EventScheduleTask> {
            val trackInfo = WindowsReadMediaInfo.getCurrentMediaInfo().firstOrNull { it.owner == "Spotify.exe" }
            if (trackInfo != null && (this.trackInfo == null || (this.trackInfo!!.title != trackInfo.title && this.trackInfo!!.artist != trackInfo.artist))) {
                this.trackInfo = trackInfo
                if (trackCover != null)
                    trackCover!!.close()
                trackCover = null
            }
        }
    }
}
