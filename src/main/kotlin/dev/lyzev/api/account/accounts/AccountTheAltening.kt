/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.account.accounts

import com.mojang.authlib.Environment
import com.mojang.authlib.exceptions.InvalidCredentialsException
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService
import de.florianmichael.waybackauthlib.WaybackAuthLib
import dev.lyzev.api.account.Account
import dev.lyzev.api.account.Account.Companion.applyLoginEnvironment
import dev.lyzev.api.account.Account.Companion.setSession
import dev.lyzev.api.account.accounts.AccountMicrosoft.Companion
import dev.lyzev.api.animation.EasingFunction
import dev.lyzev.api.animation.TimeAnimator
import dev.lyzev.api.imgui.font.ImGuiFonts
import dev.lyzev.api.imgui.font.ImGuiFonts.FONT_AWESOME_SOLID
import dev.lyzev.api.imgui.font.ImGuiFonts.OPEN_SANS_REGULAR
import dev.lyzev.api.imgui.font.icon.FontAwesomeIcons
import dev.lyzev.schizoid.Schizoid
import dev.lyzev.schizoid.feature.features.gui.guis.ImGuiScreenAccountManager
import dev.lyzev.schizoid.feature.features.gui.guis.ImGuiScreenAccountManager.WINDOW_FLAGS
import dev.lyzev.schizoid.injection.accessor.YggdrasilMinecraftSessionServiceAccessor
import imgui.ImGui.*
import imgui.flag.ImGuiInputTextFlags
import imgui.flag.ImGuiStyleVar
import imgui.flag.ImGuiWindowFlags
import imgui.type.ImString
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import net.minecraft.client.session.Session
import java.net.Proxy
import java.util.*


class AccountTheAltening(val token: String) : Account {

    private var isHidden = true
    private val hidden = "*".repeat(token.length)

    override val type = Session.AccountType.MOJANG

    override fun getSession(): Session? {
        applyLoginEnvironment(service, YggdrasilMinecraftSessionServiceAccessor.createYggdrasilMinecraftSessionService(service.servicesKeySet, service.proxy, environment));
        val auth = WaybackAuthLib(environment.servicesHost)
        auth.username = token
        auth.password = "Elevating my Minecraft gameplay with ${Schizoid.MOD_NAME}."
        runCatching {
            auth.logIn()
            msg = null
            return Session(auth.currentProfile.name, auth.currentProfile.id, auth.accessToken, Optional.empty(), Optional.empty(), Session.AccountType.MOJANG)
        }.onFailure {
            msg = if (it is InvalidCredentialsException) "The token is invalid." else "An error occurred while logging in."
            Schizoid.logger.error(msg, it)
        }
        return null
    }

    override fun render() {
        ImGuiFonts.OPEN_SANS_BOLD.begin()
        text("The Altening")
        ImGuiFonts.OPEN_SANS_BOLD.end()
        text(if (isHidden) hidden else token)
        sameLine(getColumnWidth() - OPEN_SANS_REGULAR.size * 3f - getStyle().framePaddingX * 3)
        FONT_AWESOME_SOLID.begin()
        val icon = if (isHidden) FontAwesomeIcons.EyeSlash else FontAwesomeIcons.Eye
        val size = calcTextSize(icon)
        pushStyleVar(ImGuiStyleVar.FramePadding, OPEN_SANS_REGULAR.size * 1.5f - getStyle().itemInnerSpacingX - size.x, -1f)
        if (button(icon, OPEN_SANS_REGULAR.size * 1.5f, OPEN_SANS_REGULAR.size * 1.5f)) {
            isHidden = !isHidden
        }
        OPEN_SANS_REGULAR.begin()
        if (isItemHovered())
            setTooltip("Toggle visibility of the token.")
        OPEN_SANS_REGULAR.end()
        FONT_AWESOME_SOLID.end()
        popStyleVar()
    }

    override fun save() = JsonPrimitive(token)

    companion object {
        private const val AUTH = "http://authserver.thealtening.com"
        private const val SESSION = "http://sessionserver.thealtening.com"

        val environment = Environment(SESSION, AUTH, "TheAltening")
        val service = YggdrasilAuthenticationService(Proxy.NO_PROXY, environment)

        private val token = ImString("", 32)
        private var isHidden = true
        private var msg: String? = null
        private val timeAnimator = TimeAnimator(4000)

        fun create(json: JsonElement) = AccountEasyMC(json.jsonPrimitive.content)

        fun render() {
            pushID("thealtening")
            setNextWindowPos(getMainViewport().centerX + 270f, getMainViewport().centerY - 330f)
            setNextWindowSize(250f, 0f)
            if (begin("\"THE ALTENING\"", WINDOW_FLAGS)) {
                OPEN_SANS_REGULAR.begin()
                setNextItemWidth(getColumnWidth() - OPEN_SANS_REGULAR.size * 1.5f - getStyle().framePaddingX)
                inputTextWithHint("##thealtening-token", "Token", token, if (isHidden) ImGuiInputTextFlags.Password else ImGuiInputTextFlags.None)
                sameLine()
                FONT_AWESOME_SOLID.begin()
                val icon = if (isHidden) FontAwesomeIcons.EyeSlash else FontAwesomeIcons.Eye
                var size = calcTextSize(icon)
                pushStyleVar(ImGuiStyleVar.FramePadding, OPEN_SANS_REGULAR.size * 1.5f - getStyle().itemInnerSpacingX - size.x, -1f)
                if (button(icon, OPEN_SANS_REGULAR.size * 1.5f, OPEN_SANS_REGULAR.size * 1.5f)) {
                    isHidden = !isHidden
                }
                OPEN_SANS_REGULAR.begin()
                if (isItemHovered())
                    setTooltip("Toggle visibility of the token.")
                OPEN_SANS_REGULAR.end()
                size = calcTextSize(FontAwesomeIcons.SignInAlt)
                pushStyleVar(ImGuiStyleVar.FramePadding, OPEN_SANS_REGULAR.size * 1.5f - getStyle().itemInnerSpacingX - size.x, -1f)
                if (button(FontAwesomeIcons.SignInAlt, OPEN_SANS_REGULAR.size * 1.5f, OPEN_SANS_REGULAR.size * 1.5f)) {
                    if (token.get().isNotBlank()) {
                        val session = AccountTheAltening(token.get()).getSession()
                        if (session != null) {
                            setSession(session)
                        }
                    } else {
                        msg = "The token is empty."
                    }
                }
                OPEN_SANS_REGULAR.begin()
                if (isItemHovered()) {
                    setTooltip("Login to the account.")
                }
                OPEN_SANS_REGULAR.end()
                sameLine()
                size = calcTextSize(FontAwesomeIcons.Plus)
                pushStyleVar(ImGuiStyleVar.FramePadding, OPEN_SANS_REGULAR.size * 1.5f - getStyle().itemInnerSpacingX - size.x, -1f)
                if (button(FontAwesomeIcons.Plus, OPEN_SANS_REGULAR.size * 1.5f, OPEN_SANS_REGULAR.size * 1.5f)) {
                    if (token.get().isNotBlank()) {
                        ImGuiScreenAccountManager.accounts += AccountTheAltening(token.get())
                    } else {
                        msg = "The token is empty."
                    }
                }
                OPEN_SANS_REGULAR.begin()
                if (isItemHovered()) {
                    setTooltip("Add the account.")
                }
                OPEN_SANS_REGULAR.end()
                FONT_AWESOME_SOLID.end()
                popStyleVar(3)
                sameLine()
                if (msg != null) {
                    pushStyleVar(ImGuiStyleVar.WindowPadding, 0f, 0f)
                    size = calcTextSize(msg)
                    if (beginChild(
                            "##Msg",
                            getColumnWidth(),
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
                        textColored(0xFF0000FF.toInt(), msg)
                    }
                    endChild()
                    popStyleVar()
                }
                OPEN_SANS_REGULAR.end()
            }
            end()
            popID()
        }
    }
}
