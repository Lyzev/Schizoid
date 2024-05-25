/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.render

import com.mojang.blaze3d.systems.RenderSystem
import dev.lyzev.api.animation.EasingFunction
import dev.lyzev.api.animation.TimeAnimator
import dev.lyzev.api.events.EventKeystroke
import dev.lyzev.api.events.EventMouseClick
import dev.lyzev.api.events.EventScheduleTask
import dev.lyzev.api.events.on
import dev.lyzev.api.glfw.GLFWKey
import dev.lyzev.api.hash.Sha512
import dev.lyzev.api.imgui.font.ImGuiFonts
import dev.lyzev.api.imgui.font.icon.FontAwesomeIcons
import dev.lyzev.api.opengl.Render
import dev.lyzev.api.opengl.WrappedFramebuffer
import dev.lyzev.api.opengl.WrappedNativeImageBackedTexture
import dev.lyzev.api.opengl.clear
import dev.lyzev.api.opengl.shader.Shader
import dev.lyzev.api.opengl.shader.ShaderPassThrough
import dev.lyzev.api.opengl.shader.blur.BlurHelper
import dev.lyzev.api.setting.settings.*
import dev.lyzev.schizoid.Schizoid
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.ModuleToggleableRenderImGuiContent
import dev.redstones.mediaplayerinfo.IMediaSession
import dev.redstones.mediaplayerinfo.MediaInfo
import dev.redstones.mediaplayerinfo.MediaPlayerInfo
import imgui.ImGui.*
import imgui.ImVec2
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiStyleVar
import imgui.flag.ImGuiWindowFlags
import net.minecraft.client.texture.NativeImage
import org.lwjgl.opengl.GL13

object ModuleToggleableMediaPlayer : ModuleToggleableRenderImGuiContent(
    "Media Player", "The OS media player visualized with ImGui.", category = IFeature.Category.RENDER
) {

    val infos by multiOption(
        "Infos", "The information to display.", Info.entries.map { it to (it != Info.Progress) }.toSet()
    )
    val owner by multiText(
        "Owner",
        "The owner of the media player.\nFor example: SPOTIFY, CHROME, ...\nNote: Sort by priority.",
        setOf("SPOTIFY", "CHROME"),
        true
    )
    val keybindsInScreen by switch("Keybinds in Screen", "Listen to keybinds in screen.", false)
    val skip by keybinds("Skip", "Skip the current track.")
    val previous by keybinds("Previous", "Skip to the previous track.")
    val playPause by keybinds("Play & Pause", "Play or pause the current track.")

    private val fallbackArtwork =
        WrappedNativeImageBackedTexture(NativeImage.read(javaClass.getResourceAsStream("/assets/${Schizoid.MOD_ID}/textures/vinyl.png"))).apply { upload() }
    private val noArtwork = setOf(
        "29dbaf46e36d5f2b46e1d1c3ea46e65c4bfd17ec29e82a5fbfb6d6c47ec3089cf2a54b4ebc7fde3fb5c27c1caa1a24d333b413a98a15858b7622bc7be71e8ce2", // Spotify
        "51ee4a2b0b0a7020bd38ac604030a02c6f0c9c297c73afe80446c3f92ba69399d27e231ab08eeb7204df7cd81e163f57bd12b716083770dc5f745e1654af9329", // Chrome
        "408b4bbece1ba73dbeb3b46bd31f43002fc775bc21164f4a08dd038133529c736be3fe9c578b8ad3fd74ba1aad9521d9b8b2bbf606971b30e5cfa8e83d27a7b1" // Vivaldi
    )
    private var session: IMediaSession? = null
    private val blurredArtwork = WrappedFramebuffer(width = 1000, height = 1000, fixedSize = true)

    override fun renderImGuiContent() = with(getCursorScreenPos()) {
        val drawList = getWindowDrawList()
        dummy(370f, 80f)
        drawList.addImageRounded(
            blurredArtwork.colorAttachment, x, y + 5, x + 370, y + 75, 0.2f, 0.31f, .8f, .46f, 0xFFAAAAAA.toInt(), 5f
        )
        infos.forEach { if (it.second) it.first.render(this) }
    }

    init {
        on<EventScheduleTask> {
            val session =
                MediaPlayerInfo.getMediaSessions().sortedBy {
                        owner.forEachIndexed { index, s ->
                            if (it.owner.uppercase().contains(s)) return@sortedBy index
                        }
                        return@sortedBy owner.size
                    }
                    .firstOrNull {
                        it.media.artist.isNotEmpty() || it.media.title.isNotEmpty()
                    }
            if (session == null) {
                val dummy = MediaInfo(
                    "Waiting for media." + ".".repeat(((System.currentTimeMillis() / 1000) % 3).toInt()),
                    "",
                    ByteArray(0),
                    0,
                    1,
                    true
                )
                Info.entries.forEach { it.update(dummy) }
                this.session = null
            } else {
                if (this.session == null || this.session!!.media.title != session.media.title || this.session!!.media.artist != session.media.artist || !this.session!!.media.artworkPng.contentEquals(session.media.artworkPng)) Info.entries.forEach {
                    it.update(
                        session.media
                    )
                }
                this.session = session
            }
        }

        on<EventKeystroke> { event ->
            if ((Schizoid.mc.currentScreen == null || keybindsInScreen) && event.action == 1) {
                if (session != null) {
                    if (skip.contains(GLFWKey[event.key])) {
                        session!!.next()
                    }
                    if (previous.contains(GLFWKey[event.key])) {
                        session!!.previous()
                    }
                    if (playPause.contains(GLFWKey[event.key])) {
                        session!!.playPause()
                    }
                }
            }
        }

        on<EventMouseClick> { event ->
            if ((Schizoid.mc.currentScreen == null || keybindsInScreen) && event.action == 1) {
                if (session != null) {
                    if (skip.contains(GLFWKey[event.button])) {
                        session!!.next()
                    }
                    if (previous.contains(GLFWKey[event.button])) {
                        session!!.previous()
                    }
                    if (playPause.contains(GLFWKey[event.button])) {
                        session!!.playPause()
                    }
                }
            }
        }
    }

    enum class Info : OptionEnum {
        Title {

            private var title: String? = null
            private val timeAnimator = TimeAnimator(4000)

            override fun update(media: MediaInfo) {
                title = media.title
                timeAnimator.setReversed(false)
                timeAnimator.setProgress(0.0)
            }

            override fun render(cursorPos: ImVec2) {
                if (title != null) {
                    ImGuiFonts.OPEN_SANS_BOLD_BIG.begin()
                    pushStyleVar(ImGuiStyleVar.WindowPadding, 0f, 0f)
                    val title = if (title!!.isNotEmpty()) title else "Unknown Title"
                    val size = calcTextSize(title)
                    setCursorPosY(getCursorPosY() - 50)
                    setCursorPosX(getCursorPosX() + 90)
                    if (beginChild(
                            "##Title",
                            getColumnWidth() - 20f,
                            size.y,
                            false,
                            ImGuiWindowFlags.NoScrollbar or ImGuiWindowFlags.NoBackground
                        )
                    ) {
                        if (size.x > getColumnWidth()) {
                            val progress = timeAnimator.getProgressNotClamped()
                            if (progress < -.2 || progress > 1.2) timeAnimator.setReversed(!timeAnimator.reversed)
                            setScrollX((EasingFunction.LINEAR(timeAnimator.getProgress()) * (size.x - getColumnWidth())).toFloat())
                        }
                        val cursorPos = getCursorScreenPos()
                        dummy(size.x, size.y)
                        getWindowDrawList().addText(cursorPos.x + 1, cursorPos.y + 1, 0xFF000000.toInt(), title)
                        getWindowDrawList().addText(cursorPos.x, cursorPos.y, 0xFFFFFFFF.toInt(), title)
                    }
                    endChild()
                    popStyleVar()
                    ImGuiFonts.OPEN_SANS_BOLD_BIG.end()
                }
            }
        },
        Artist {

            private var artist: String? = null
            private val timeAnimator = TimeAnimator(4000)

            override fun update(media: MediaInfo) {
                artist = media.artist
                timeAnimator.setReversed(false)
                timeAnimator.setProgress(0.0)
            }

            override fun render(cursorPos: ImVec2) {
                if (artist != null) {
                    ImGuiFonts.OPEN_SANS_BOLD_MEDIUM.begin()
                    pushStyleVar(ImGuiStyleVar.WindowPadding, 0f, 0f)
                    val artist = if (artist!!.isNotEmpty()) artist else "Unknown Artist"
                    val size = calcTextSize(artist)
                    setCursorPosY(getCursorPosY() - 65)
                    setCursorPosX(getCursorPosX() + 90)
                    if (beginChild(
                            "##Artist",
                            getColumnWidth() - 20f,
                            size.y,
                            false,
                            ImGuiWindowFlags.NoScrollbar or ImGuiWindowFlags.NoBackground
                        )
                    ) {
                        if (size.x > getColumnWidth()) {
                            val progress = timeAnimator.getProgressNotClamped()
                            if (progress < -.2 || progress > 1.2) timeAnimator.setReversed(!timeAnimator.reversed)
                            setScrollX((EasingFunction.LINEAR(timeAnimator.getProgress()) * (size.x - getColumnWidth())).toFloat())
                        }
                        val cursorPos = getCursorScreenPos()
                        dummy(size.x, size.y)
                        getWindowDrawList().addText(cursorPos.x + 1, cursorPos.y + 1, 0xFF000000.toInt(), artist)
                        getWindowDrawList().addText(cursorPos.x, cursorPos.y, 0xFFFFFFFF.toInt(), artist)
                    }
                    endChild()
                    popStyleVar()
                    ImGuiFonts.OPEN_SANS_BOLD_MEDIUM.end()
                }
            }
        },
        Progress {

            override fun update(media: MediaInfo) {}

            override fun render(cursorPos: ImVec2) {
                if (session == null) return
                setCursorPosY(getCursorPosY() + 28)
                setCursorPosX(getCursorPosX() + 80)
                pushStyleColor(ImGuiCol.PlotHistogram, getColorU32(ImGuiCol.Text, .4f))
                progressBar(session!!.media.position / session!!.media.duration.toFloat(), 290f, 3f, "##Progress")
                popStyleColor()
            }
        },
        Artwork {

            private var artwork: WrappedNativeImageBackedTexture? = null
            private var media: MediaInfo? = null

            override fun update(media: MediaInfo) {
                if (artwork != null && artwork != fallbackArtwork) artwork!!.close()
                artwork = null
                this.media = media
            }

            override fun render(cursorPos: ImVec2) {
                if (media == null) return
                if (artwork == null) {
                    artwork =
                        if (media!!.artworkPng.isEmpty() || Sha512.hash(media!!.artworkPng) in noArtwork) fallbackArtwork
                        else WrappedNativeImageBackedTexture(NativeImage.read(media!!.artworkPng.inputStream())).apply { upload() }
                    Render.store()
                    RenderSystem.disableCull()
                    RenderSystem.defaultBlendFunc()
                    RenderSystem.enableBlend()
                    BlurHelper.mode.switchStrength(6)
                    BlurHelper.mode.render(artwork!!.glId)
                    blurredArtwork.clear()
                    blurredArtwork.beginWrite(true)
                    ShaderPassThrough.bind()
                    RenderSystem.activeTexture(GL13.GL_TEXTURE0)
                    BlurHelper.mode.blur.output.beginRead()
                    ShaderPassThrough["uTexture"] = 0
                    ShaderPassThrough["uScale"] = 1f
                    Shader.drawFullScreen()
                    ShaderPassThrough.unbind()
                    mc.framebuffer.beginWrite(true)
                    RenderSystem.enableCull()
                    Render.store()
                }
                with(cursorPos) {
                    val drawList = getWindowDrawList()
                    drawList.addRectFilled(x, y, x + 80, y + 80, 0xFF000000.toInt(), 5f)
                    drawList.addImageRounded(
                        artwork!!.glId, x, y, x + 80, y + 80, if (session != null && session!!.owner.contains(
                                "spotify", true
                            ) && artwork != fallbackArtwork
                        ) .12f else 0f, 0f, if (session != null && session!!.owner.contains(
                                "spotify", true
                            ) && artwork != fallbackArtwork
                        ) .88f else 1f, if (session != null && session!!.owner.contains(
                                "spotify", true
                            ) && artwork != fallbackArtwork
                        ) .78f else 1f, -1, 5f
                    )
                }
            }
        },
        PauseIndicator {

            override val key = "Pause Indicator"
            private var time = System.currentTimeMillis()

            override fun update(media: MediaInfo) {}

            override fun render(cursorPos: ImVec2) {
                if (session == null) return
                if (!session!!.media.playing) {
                    if (System.currentTimeMillis() / 500 % 2 == 0L) {
                        ImGuiFonts.FONT_AWESOME_SOLID.begin()
                        getWindowDrawList().addText(
                            cursorPos.x + 356, cursorPos.y + 56, 0xFF000000.toInt(), FontAwesomeIcons.Pause
                        )
                        getWindowDrawList().addText(
                            cursorPos.x + 355, cursorPos.y + 55, 0xFFFFFFFF.toInt(), FontAwesomeIcons.Pause
                        )
                        ImGuiFonts.FONT_AWESOME_SOLID.end()
                    }
                    time = System.currentTimeMillis()
                } else if (System.currentTimeMillis() - time <= 500) {
                    ImGuiFonts.FONT_AWESOME_SOLID.begin()
                    getWindowDrawList().addText(
                        cursorPos.x + 356, cursorPos.y + 56, 0xFF000000.toInt(), FontAwesomeIcons.Play
                    )
                    getWindowDrawList().addText(
                        cursorPos.x + 355, cursorPos.y + 55, 0xFFFFFFFF.toInt(), FontAwesomeIcons.Play
                    )
                    ImGuiFonts.FONT_AWESOME_SOLID.end()
                }
            }
        };

        override val key = name

        abstract fun update(media: MediaInfo)

        abstract fun render(cursorPos: ImVec2)
    }
}
