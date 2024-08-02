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

/**
 * Represents a cracked account.
 */
class AccountCracked(val username: String, var uuid: String) : Account {

    override val type = Account.Types.CRACKED

    override fun getSession(): Session {
        if (uuid.isBlank()) {
            runCatching {
                val response =
                    HttpClient.request(HttpMethod.GET, "https://api.mojang.com/users/profiles/minecraft/$username")
                val minecraftResponse = Json.decodeFromString<MinecraftResponse>(response.toString())
                uuid = UndashedUuid.fromStringLenient(minecraftResponse.id).toString()
            }.onFailure {
                Schizoid.logger.error("Failed to get cracked account session.", it)
                uuid = UUID.randomUUID().toString()
            }
        }
        return Session(username, UUID.fromString(uuid), "", Optional.empty(), Optional.empty(), sessionType)
    }

    override fun save() = JsonObject(mapOf("username" to JsonPrimitive(username), "uuid" to JsonPrimitive(uuid)))

    override fun render() {
        super.render()
        text(username)
    }

    companion object : Account.Type<AccountCracked> {

        private val chars = ('a'..'z') + ('A'..'Z') + ('0'..'9')

        private val username = ImString("", 16)
        private val uuid = ImString("", 36)
        private var useOnlineUuid = true
        private var switchData = booleanArrayOf(useOnlineUuid)
        private val switch = Switch()
        private var msg: String? = null
        private val timeAnimator = TimeAnimator(4000)

        override val sessionType = Session.AccountType.LEGACY

        override fun create(json: JsonElement) = AccountCracked(
            json.jsonObject["username"]!!.jsonPrimitive.content,
            json.jsonObject["uuid"]!!.jsonPrimitive.content
        )

        override fun render() {
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
                if (ImGuiScreenAccountManager.button(FontAwesomeIcons.SignInAlt, "Login to the account.")) {
                    if (username.get().isNotBlank() && (useOnlineUuid || uuid.get().isNotBlank())) {
                        setSession(AccountCracked(username.get(), if (useOnlineUuid) "" else uuid.get()).getSession())
                    } else {
                        msg = "The username or UUID is empty."
                    }
                }
                sameLine()
                if (ImGuiScreenAccountManager.button(FontAwesomeIcons.Plus, "Add the account.")) {
                    if (username.get().isNotBlank() && (useOnlineUuid || uuid.get().isNotBlank())) {
                        ImGuiScreenAccountManager.accounts += AccountCracked(
                            username.get(),
                            if (useOnlineUuid) "" else uuid.get()
                        )
                    } else {
                        msg = "The username or UUID is empty."
                    }
                }
                sameLine()
                if (ImGuiScreenAccountManager.button(FontAwesomeIcons.UserPlus, "Add a account with a random username.")) {
                    ImGuiScreenAccountManager.accounts += AccountCracked(
                        (1..16).map { chars.random() }.joinToString(""),
                        if (useOnlineUuid) "" else uuid.get()
                    )
                }
                sameLine()
                if (msg != null) {
                    pushStyleVar(ImGuiStyleVar.WindowPadding, 0f, 0f)
                    val size = calcTextSize(msg)
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

    /**
     * Represents a response from the Minecraft API.
     */
    @Serializable
    data class MinecraftResponse(val id: String, val name: String)

}
