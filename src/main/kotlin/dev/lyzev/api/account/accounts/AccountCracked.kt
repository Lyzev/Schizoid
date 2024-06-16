/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.account.accounts

import com.mojang.util.UndashedUuid
import dev.lyzev.api.account.Account
import dev.lyzev.api.account.Account.Companion.setSession
import dev.lyzev.api.animation.EasingFunction
import dev.lyzev.api.animation.TimeAnimator
import dev.lyzev.api.imgui.font.ImGuiFonts
import dev.lyzev.api.imgui.font.ImGuiFonts.FONT_AWESOME_SOLID
import dev.lyzev.api.imgui.font.ImGuiFonts.OPEN_SANS_REGULAR
import dev.lyzev.api.imgui.font.icon.FontAwesomeIcons
import dev.lyzev.api.setting.settings.Switch
import dev.lyzev.schizoid.Schizoid
import dev.lyzev.schizoid.feature.features.gui.guis.ImGuiScreenAccountManager
import dev.lyzev.schizoid.feature.features.gui.guis.ImGuiScreenAccountManager.WINDOW_FLAGS
import imgui.ImGui.*
import imgui.flag.ImGuiStyleVar
import imgui.flag.ImGuiWindowFlags
import imgui.type.ImString
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import me.lyzev.network.http.HttpClient
import me.lyzev.network.http.HttpMethod
import net.minecraft.client.session.Session
import java.util.*

class AccountCracked(val username: String, val uuid: String) : Account {

    override val type = Session.AccountType.LEGACY

    override fun getSession(): Session {
        if (uuid.isBlank()) {
            runCatching {
                val response = HttpClient.request(HttpMethod.GET, "https://api.mojang.com/users/profiles/minecraft/$username")
                val minecraftResponse = Json.decodeFromString<MinecraftResponse>(response.toString())
                return Session(username, UndashedUuid.fromStringLenient(minecraftResponse.id), "", Optional.empty(), Optional.empty(), type)
            }.onFailure {
                Schizoid.logger.error("Failed to get cracked account session.", it)
                return Session(username, UUID.randomUUID(), "", Optional.empty(), Optional.empty(), type)
            }
        }
        return Session(username, UUID.fromString(uuid), "", Optional.empty(), Optional.empty(), type)
    }

    override fun render() {
        ImGuiFonts.OPEN_SANS_BOLD.begin()
        text("Cracked")
        ImGuiFonts.OPEN_SANS_BOLD.end()
        text(username)
    }

    override fun save() = JsonObject(mapOf("username" to JsonPrimitive(username), "uuid" to JsonPrimitive(uuid)))

    @Serializable
    data class MinecraftResponse(val id: String, val name: String)

    companion object {

        private val username = ImString("", 16)
        private val uuid = ImString("", 36)
        private var useOnlineUuid = true
        private var switchData = booleanArrayOf(useOnlineUuid)
        private val switch = Switch()
        private var msg: String? = null
        private val timeAnimator = TimeAnimator(4000)

        fun create(json: JsonElement) = AccountCracked(json.jsonObject["username"]!!.jsonPrimitive.content, json.jsonObject["uuid"]!!.jsonPrimitive.content)

        fun render() {
            pushID("cracked")
            setNextWindowPos(getMainViewport().centerX - 520f, getMainViewport().centerY - 170f)
            setNextWindowSize(250f, 0f)
            if (begin("\"CRACKED\"", WINDOW_FLAGS)) {
                OPEN_SANS_REGULAR.begin()
                setNextItemWidth(getColumnWidth())
                inputTextWithHint("##cracked-username", "Username", username)
                text("Use online UUID")
                sameLine()
                switch.render("Use online UUID", switchData)
                if (switchData[0] != useOnlineUuid) {
                    useOnlineUuid = switchData[0]
                    switch.timeAnimator.setReversed(!useOnlineUuid)
                }
                if (!useOnlineUuid) {
                    setNextItemWidth(getColumnWidth())
                    inputTextWithHint("##cracked-uuid", "UUID", uuid)
                }
                FONT_AWESOME_SOLID.begin()
                var size = calcTextSize(FontAwesomeIcons.SignInAlt)
                pushStyleVar(ImGuiStyleVar.FramePadding, OPEN_SANS_REGULAR.size * 1.5f - getStyle().itemInnerSpacingX - size.x, -1f)
                if (button(FontAwesomeIcons.SignInAlt, OPEN_SANS_REGULAR.size * 1.5f, OPEN_SANS_REGULAR.size * 1.5f)) {
                    if (username.get().isNotBlank() && (useOnlineUuid || uuid.get().isNotBlank())) {
                        setSession(AccountCracked(username.get(), if (useOnlineUuid) "" else uuid.get()).getSession())
                    } else {
                        msg = "The username or UUID is empty."
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
                    if (username.get().isNotBlank() && (useOnlineUuid || uuid.get().isNotBlank())) {
                        ImGuiScreenAccountManager.accounts += AccountCracked(username.get(), if (useOnlineUuid) "" else uuid.get())
                    } else {
                        msg = "The username or UUID is empty."
                    }
                }
                OPEN_SANS_REGULAR.begin()
                if (isItemHovered()) {
                    setTooltip("Add the account.")
                }
                OPEN_SANS_REGULAR.end()
                FONT_AWESOME_SOLID.end()
                popStyleVar(2)
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
