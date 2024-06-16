/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.gui.guis

import com.mojang.blaze3d.systems.RenderSystem
import dev.lyzev.api.account.Account
import dev.lyzev.api.account.Account.Companion.setSession
import dev.lyzev.api.account.accounts.AccountCracked
import dev.lyzev.api.account.accounts.AccountEasyMC
import dev.lyzev.api.account.accounts.AccountMicrosoft
import dev.lyzev.api.account.accounts.AccountTheAltening
import dev.lyzev.api.cryptography.cipher.AES
import dev.lyzev.api.cryptography.hwid.HWID
import dev.lyzev.api.events.*
import dev.lyzev.api.glfw.GLFWKey
import dev.lyzev.api.imgui.font.ImGuiFonts.*
import dev.lyzev.api.imgui.font.icon.FontAwesomeIcons
import dev.lyzev.api.opengl.shader.GLSLSandboxShader
import dev.lyzev.api.opengl.shader.ShaderGameOfLife
import dev.lyzev.api.opengl.shader.ShaderParticle
import dev.lyzev.api.setting.settings.keybinds
import dev.lyzev.schizoid.Schizoid
import dev.lyzev.schizoid.feature.features.gui.ImGuiScreen
import dev.lyzev.schizoid.feature.features.gui.guis.ImGuiScreenFeature.background
import dev.lyzev.schizoid.feature.features.module.ModuleToggleableRenderImGuiContent
import imgui.ImGui.*
import imgui.flag.ImGuiCond
import imgui.flag.ImGuiStyleVar
import imgui.flag.ImGuiWindowFlags
import kotlinx.serialization.json.*
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen
import java.io.File
import kotlin.concurrent.thread

object ImGuiScreenAccountManager : ImGuiScreen("Account Manager"), EventListener {

    const val WINDOW_FLAGS = ImGuiWindowFlags.NoResize or ImGuiWindowFlags.NoMove or ImGuiWindowFlags.NoCollapse
    val accounts = mutableSetOf<Account>()
    private var mainAccount: Account? = null
    private var skin = mc.textureManager.getTexture(mc.skinProvider.getSkinTextures(mc.gameProfile).texture)
    private val backgroundShader = GLSLSandboxShader("BackgroundNoise")

    override fun renderBackground(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        if (!isIngame) {
            backgroundShader.draw {
                this["primary"] = ImGuiScreenFeature.colorScheme[ImGuiScreenFeature.mode].primary
                this["secondary"] = ImGuiScreenFeature.colorScheme[ImGuiScreenFeature.mode].secondary
                this["accent"] = ImGuiScreenFeature.colorScheme[ImGuiScreenFeature.mode].accent
            }
        }
        if (background != "None") {
            RenderSystem.disableCull()
            RenderSystem.defaultBlendFunc()
            RenderSystem.enableBlend()
            when (background) {
                "Particle" -> ShaderParticle.draw()
                "Game of Life" -> ShaderGameOfLife.draw()
            }
            RenderSystem.enableCull()
        }
    }

    override fun renderImGui() {
        setNextWindowPos(getMainViewport().centerX, getMainViewport().centerY, ImGuiCond.Always, .5f, .6f)
        setNextWindowSize(500f, 780f)
        OPEN_SANS_BOLD.begin()
        if (begin("\"ACCOUNTS\"", WINDOW_FLAGS)) {
            OPEN_SANS_REGULAR.begin()
            val remove = mutableSetOf<Account>()
            for (account in accounts) {
                pushID(account.hashCode())
                account.render()
                val framePaddingX = getStyle().framePaddingX
                sameLine(getColumnWidth() - OPEN_SANS_REGULAR.size * 3f - framePaddingX * 4)
                if (mainAccount == account) {
                    FONT_AWESOME_SOLID.begin()
                } else {
                    FONT_AWESOME_REGULAR.begin()
                }
                var size = calcTextSize(FontAwesomeIcons.Star)
                pushStyleVar(
                    ImGuiStyleVar.FramePadding,
                    OPEN_SANS_REGULAR.size * 1.5f - getStyle().itemInnerSpacingX - size.x,
                    -1f
                )
                if (button(FontAwesomeIcons.Star, OPEN_SANS_REGULAR.size * 1.5f, OPEN_SANS_REGULAR.size * 1.5f)) {
                    mainAccount = if (mainAccount == account) {
                        null
                    } else {
                        account
                    }
                }
                OPEN_SANS_REGULAR.begin()
                if (isItemHovered())
                    setTooltip("Set as main account.")
                OPEN_SANS_REGULAR.end()
                if (mainAccount == account) {
                    FONT_AWESOME_SOLID.end()
                } else {
                    FONT_AWESOME_REGULAR.end()
                }
                sameLine(getColumnWidth() - OPEN_SANS_REGULAR.size * 1.5f - framePaddingX * 2)
                FONT_AWESOME_SOLID.begin()
                size = calcTextSize(FontAwesomeIcons.SignInAlt)
                pushStyleVar(
                    ImGuiStyleVar.FramePadding,
                    OPEN_SANS_REGULAR.size * 1.5f - getStyle().itemInnerSpacingX - size.x,
                    -1f
                )
                if (button(FontAwesomeIcons.SignInAlt, OPEN_SANS_REGULAR.size * 1.5f, OPEN_SANS_REGULAR.size * 1.5f)) {
                    thread {
                        account.getSession()?.let { setSession(it) }
                    }
                }
                OPEN_SANS_REGULAR.begin()
                if (isItemHovered())
                    setTooltip("Login to the account.")
                OPEN_SANS_REGULAR.end()
                sameLine(getColumnWidth() - framePaddingX)
                size = calcTextSize(FontAwesomeIcons.Trash)
                pushStyleVar(
                    ImGuiStyleVar.FramePadding,
                    OPEN_SANS_REGULAR.size * 1.5f - getStyle().itemInnerSpacingX - size.x,
                    -1f
                )
                if (button(FontAwesomeIcons.Trash, OPEN_SANS_REGULAR.size * 1.5f, OPEN_SANS_REGULAR.size * 1.5f)) {
                    remove += account
                }
                OPEN_SANS_REGULAR.begin()
                if (isItemHovered())
                    setTooltip("Delete the account.")
                OPEN_SANS_REGULAR.end()
                FONT_AWESOME_SOLID.end()
                popStyleVar(3)
                popID()
                separator()
            }
            accounts -= remove
            OPEN_SANS_REGULAR.end()
        }
        end()
        setNextWindowPos(getMainViewport().centerX - 520f, getMainViewport().centerY - 470f)
        setNextWindowSize(250f, 0f)
        if (begin("\"SESSION\"", WINDOW_FLAGS)) {
            OPEN_SANS_REGULAR.begin()
            val uvMinY = 8f
            val uvMaxY = 8f + uvMinY
            val cursorScreenPos = getCursorScreenPos()
            dummy(50f, 50f)
            getWindowDrawList().addImageRounded(
                skin!!.glId,
                cursorScreenPos.x,
                cursorScreenPos.y,
                cursorScreenPos.x + 50f,
                cursorScreenPos.y + 50f,
                8f / 64f,
                uvMinY / 64f,
                16f / 64f,
                uvMaxY / 64f,
                -1,
                5f
            )
            sameLine()
            if (beginChild(
                    "##Msg",
                    getColumnWidth(),
                    -1f,
                    false,
                    ImGuiWindowFlags.NoScrollbar or ImGuiWindowFlags.NoBackground
                )
            ) {
                if (mc.session.username.isNotBlank())
                    text(mc.session.username)
                val uuid = mc.session.uuidOrNull?.toString()
                if (uuid?.isNotBlank() == true) {
                    text(uuid.substring(0, uuid.length.coerceAtMost(12)) + if (uuid.length > 12) "..." else "")
                    if (isItemHovered())
                        setTooltip(uuid)
                }
            }
            endChild()
            sameLine(getWindowWidth() - OPEN_SANS_REGULAR.size * 1.5f - getStyle().framePaddingY * 2)
            FONT_AWESOME_SOLID.begin()
            val size = calcTextSize(FontAwesomeIcons.Copy)
            pushStyleVar(
                ImGuiStyleVar.FramePadding,
                OPEN_SANS_REGULAR.size * 1.5f - getStyle().itemInnerSpacingX - size.x,
                -1f
            )
            if (button(FontAwesomeIcons.Copy, OPEN_SANS_REGULAR.size * 1.5f, OPEN_SANS_REGULAR.size * 1.5f)) {
                copy(
                    JsonObject(
                        mapOf(
                            "username" to JsonPrimitive(mc.session.username),
                            "uuid" to JsonPrimitive(mc.session.uuidOrNull.toString()),
                            "type" to JsonPrimitive(mc.session.accountType.name)
                        )
                    ).toString()
                )
            }
            OPEN_SANS_REGULAR.begin()
            if (isItemHovered())
                setTooltip("Copy the session data.")
            OPEN_SANS_REGULAR.end()
            FONT_AWESOME_SOLID.end()
            popStyleVar()
            OPEN_SANS_REGULAR.end()
        }
        end()
        setNextWindowPos(getMainViewport().centerX, getMainViewport().centerY + 330f, ImGuiCond.Always, 0.5f, 0f)
        setNextWindowSize(250f, 0f)
        if (begin("\"CONTROL\"", WINDOW_FLAGS)) {
            OPEN_SANS_REGULAR.begin()
            if (button("Clear", getColumnWidth(), OPEN_SANS_REGULAR.size + getStyle().framePaddingY * 2)) {
                accounts.clear()
            }
            if (button("Close", getColumnWidth(), OPEN_SANS_REGULAR.size + getStyle().framePaddingY * 2)) {
                close()
            }
            OPEN_SANS_REGULAR.end()
        }
        end()
        AccountMicrosoft.render()
        AccountCracked.render()
        AccountEasyMC.render()
        AccountTheAltening.render()
        OPEN_SANS_BOLD.end()
    }

    init {
        val file = File(Schizoid.root, "accounts.json")
        val aes = AES(HWID.toString())
        runCatching {
            if (file.exists()) {
                val encrypted = file.readBytes()
                val decrypted = aes.decrypt(encrypted)
                val json = Json.parseToJsonElement(decrypted)
                json.jsonArray.forEach { entry ->
                    val obj = entry.jsonObject
                    val type = obj["type"]!!.jsonPrimitive.content
                    val account = when (type) {
                        "Cracked" -> AccountCracked.create(obj["data"]!!)
                        "EasyMC" -> AccountEasyMC.create(obj["data"]!!)
                        "TheAltening" -> AccountTheAltening.create(obj["data"]!!)
                        "Microsoft" -> AccountMicrosoft.create(obj["data"]!!)
                        else -> null
                    }
                    if (account != null) {
                        if (obj["main"]?.jsonPrimitive?.boolean == true) {
                            mainAccount = account
                            account.getSession()?.let { setSession(it) }
                        }
                        accounts += account
                    }
                }
            }
        }.onFailure {
            Schizoid.logger.error("Failed to load accounts.", it)
        }.onSuccess {
            Schizoid.logger.info("Loaded ${accounts.size} accounts.")
        }
        on<EventShutdown> {
            runCatching {
                val json = JsonArray(accounts.mapNotNull {
                    val type = when (it) {
                        is AccountCracked -> "Cracked"
                        is AccountEasyMC -> "EasyMC"
                        is AccountTheAltening -> "TheAltening"
                        is AccountMicrosoft -> "Microsoft"
                        else -> null
                    }
                    if (type != null) {
                        JsonObject(
                            mapOf(
                                "type" to JsonPrimitive(type),
                                "data" to it.save(),
                                "main" to JsonPrimitive(mainAccount == it)
                            )
                        )
                    } else {
                        null
                    }
                })
                val encrypted = aes.encrypt(json.toString())
                file.writeBytes(encrypted)
            }.onFailure {
                Schizoid.logger.error("Failed to save accounts.", it)
            }.onSuccess {
                Schizoid.logger.info("Saved ${accounts.size} accounts.")
            }
        }
        on<EventScheduleTask> {
            skin = mc.textureManager.getTexture(mc.skinProvider.getSkinTextures(mc.gameProfile).texture)
        }
        on<EventRenderImGuiContent> {
            if (mc.currentScreen is MultiplayerScreen && !isIngame) {
                setNextWindowPos(20f, 20f, ImGuiCond.FirstUseEver)
                OPEN_SANS_BOLD.begin()
                if (begin("\"${name.uppercase()}\"", ModuleToggleableRenderImGuiContent.WindowFlags.DEFAULT)) {
                    OPEN_SANS_REGULAR.begin()
                    if (button("Open", 150f, OPEN_SANS_REGULAR.size + getStyle().framePaddingY * 2)) {
                        mc.setScreen(this@ImGuiScreenAccountManager)
                    }
                    OPEN_SANS_REGULAR.end()
                }
                end()
                OPEN_SANS_BOLD.end()
            }
        }
    }

    override val desc = "A screen for managing accounts."
    override var keybinds by keybinds(
        "Keybinds",
        "All keys used to control the feature.",
        setOf(),
        setOf(GLFWKey.MOUSE_BUTTON_LEFT, GLFWKey.MOUSE_BUTTON_RIGHT, GLFWKey.MOUSE_BUTTON_MIDDLE)
    )

    override val shouldHandleEvents = true
}
