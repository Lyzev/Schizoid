/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.account.accounts

import com.google.gson.JsonParser
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService
import dev.lyzev.api.account.Account
import dev.lyzev.api.account.Account.Companion.applyLoginEnvironment
import dev.lyzev.api.account.Account.Companion.setSession
import dev.lyzev.api.animation.EasingFunction
import dev.lyzev.api.animation.TimeAnimator
import dev.lyzev.api.imgui.font.ImGuiFonts.OPEN_SANS_REGULAR
import dev.lyzev.api.imgui.font.icon.FontAwesomeIcons
import dev.lyzev.schizoid.Schizoid
import dev.lyzev.schizoid.feature.features.gui.guis.ImGuiScreenAccountManager
import dev.lyzev.schizoid.feature.features.gui.guis.ImGuiScreenAccountManager.WINDOW_FLAGS
import dev.lyzev.schizoid.injection.accessor.MinecraftClientAccessor
import imgui.ImGui.*
import imgui.flag.ImGuiInputTextFlags
import imgui.flag.ImGuiStyleVar
import imgui.flag.ImGuiWindowFlags
import imgui.type.ImString
import kotlinx.serialization.json.*
import net.minecraft.client.session.Session
import net.raphimc.minecraftauth.MinecraftAuth
import net.raphimc.minecraftauth.step.AbstractStep
import net.raphimc.minecraftauth.step.java.session.StepFullJavaSession
import net.raphimc.minecraftauth.step.msa.StepCredentialsMsaCode.MsaCredentials
import net.raphimc.minecraftauth.step.msa.StepLocalWebServer.LocalWebServerCallback
import net.raphimc.minecraftauth.step.msa.StepMsaDeviceCode.MsaDeviceCodeCallback
import net.raphimc.minecraftauth.util.MicrosoftConstants
import java.awt.Desktop
import java.net.URI
import java.util.*
import kotlin.concurrent.thread

/**
 * Represents a Microsoft account.
 */
class AccountMicrosoft(
    var session: StepFullJavaSession.FullJavaSession,
    private val auth: AbstractStep<*, StepFullJavaSession.FullJavaSession>
) : Account {

    override val type = Account.Types.MICROSOFT

    override fun getSession(): Session {
        msg = null
        val authenticationService =
            YggdrasilAuthenticationService((Schizoid.mc as MinecraftClientAccessor).networkProxy)
        applyLoginEnvironment(authenticationService, authenticationService.createMinecraftSessionService())
        if (session.isExpired) {
            session = auth.refresh(MinecraftAuth.createHttpClient(), session)
        }
        return Session(
            session.mcProfile.name,
            session.mcProfile.id,
            session.mcProfile.mcToken.accessToken,
            Optional.empty(),
            Optional.empty(),
            sessionType
        )
    }

    override fun save(): JsonElement = JsonObject(
        mapOf(
            "session" to auth.toJson(session).let { Json.parseToJsonElement(it.toString()) }, "type" to JsonPrimitive(
                when (auth) {
                    MinecraftAuth.JAVA_DEVICE_CODE_LOGIN -> "JAVA_DEVICE_CODE_LOGIN"
                    webLoginAuthStep -> "JAVA_WEB_LOGIN"
                    MinecraftAuth.JAVA_CREDENTIALS_LOGIN -> "JAVA_CREDENTIALS_LOGIN"
                    else -> throw IllegalArgumentException("Unknown type.")
                }
            )
        )
    )

    override fun render() {
        super.render()
        text(session.mcProfile.name)
    }

    companion object : Account.Type<AccountMicrosoft> {

        private val email = ImString("", 64)
        private val emailRegex = Regex("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")
        private val password = ImString("", 64)
        private var isHidden = true
        private var msg: String? = null
        private val timeAnimator = TimeAnimator(4000)
        private val webLoginAuthStep = MinecraftAuth.builder()
            .withTimeout(300)
            .withClientId("4673b348-3efa-4f6a-bbb6-34e141cdc638") // thanks to meteor client, please don't sue me :)
            .withScope(MicrosoftConstants.SCOPE2)
            .withRedirectUri("http://127.0.0.1")
            .localWebServer()
            .withDeviceToken("Win32")
            .regularAuthentication(MicrosoftConstants.JAVA_XSTS_RELYING_PARTY)
            .buildMinecraftJavaProfileStep(false)

        override val sessionType = Session.AccountType.MSA

        override fun create(json: JsonElement) =
            when (json.apply { println(this.toString()) }.jsonObject["type"]!!.jsonPrimitive.content) {
                "JAVA_DEVICE_CODE_LOGIN" -> AccountMicrosoft(
                    MinecraftAuth.JAVA_DEVICE_CODE_LOGIN.fromJson(JsonParser.parseString(json.jsonObject["session"]!!.jsonObject.toString()).asJsonObject),
                    MinecraftAuth.JAVA_DEVICE_CODE_LOGIN
                )

                "JAVA_WEB_LOGIN" -> AccountMicrosoft(
                    webLoginAuthStep.fromJson(
                        JsonParser.parseString(
                            json.jsonObject["session"]!!.jsonObject.toString()
                        ).asJsonObject
                    ),
                    webLoginAuthStep
                )

                "JAVA_CREDENTIALS_LOGIN" -> AccountMicrosoft(
                    MinecraftAuth.JAVA_CREDENTIALS_LOGIN.fromJson(JsonParser.parseString(json.jsonObject["session"]!!.jsonObject.toString()).asJsonObject),
                    MinecraftAuth.JAVA_CREDENTIALS_LOGIN
                )

                else -> throw IllegalArgumentException("Unknown type.")
            }

        private fun login(email: String, password: String): StepFullJavaSession.FullJavaSession =
            MinecraftAuth.JAVA_CREDENTIALS_LOGIN.getFromInput(
                MinecraftAuth.createHttpClient(),
                MsaCredentials(email, password)
            )

        override fun render() {
            pushID("microsoft")
            setNextWindowPos(getMainViewport().centerX - 520f, getMainViewport().centerY - 345f)
            setNextWindowSize(250f, 0f)
            if (begin("\"MICROSOFT\"", WINDOW_FLAGS)) {
                OPEN_SANS_REGULAR.begin()
                setNextItemWidth(getColumnWidth())
                inputTextWithHint("##microsoft-email", "E-Mail", email)
                setNextItemWidth(getWindowWidth() - ImGuiScreenAccountManager.buttonSize - getStyle().itemSpacingX - getStyle().windowPaddingX * 2)
                inputTextWithHint(
                    "##microsoft-password",
                    "Password",
                    password,
                    if (isHidden) ImGuiInputTextFlags.Password else ImGuiInputTextFlags.None
                )
                sameLine()
                val icon = if (isHidden) FontAwesomeIcons.EyeSlash else FontAwesomeIcons.Eye
                if (ImGuiScreenAccountManager.button(icon, "Toggle visibility of the password.")) {
                    isHidden = !isHidden
                }
                if (ImGuiScreenAccountManager.button(FontAwesomeIcons.SignInAlt, "Login to the account.")) {
                    if (email.get().matches(emailRegex) && password.get()
                            .isNotBlank()
                    ) {
                        thread {
                            runCatching {
                                val session = login(email.get(), password.get())
                                setSession(AccountMicrosoft(session, MinecraftAuth.JAVA_CREDENTIALS_LOGIN).getSession())
                            }.onFailure {
                                msg = "Failed to login to Microsoft account."
                                Schizoid.logger.error("Failed to login to Microsoft account.", it)
                            }
                        }
                    } else {
                        msg = "An input is not valid."
                    }
                }
                sameLine()
                if (ImGuiScreenAccountManager.button(FontAwesomeIcons.Plus, "Add the account.")) {
                    if (email.get().matches(emailRegex) && password.get()
                            .isNotBlank()
                    ) {
                        thread {
                            runCatching {
                                val session = login(email.get(), password.get())
                                ImGuiScreenAccountManager.accounts += AccountMicrosoft(
                                    session,
                                    MinecraftAuth.JAVA_CREDENTIALS_LOGIN
                                )
                            }.onFailure {
                                msg = "Failed to add Microsoft account."
                                Schizoid.logger.error("Failed to add Microsoft account.", it)
                            }
                        }
                    } else {
                        msg = "An input is not valid."
                    }
                }
                sameLine()
                if (ImGuiScreenAccountManager.button(FontAwesomeIcons.Globe, "Add the account using a web login.")) {
                    thread {
                        val httpClient = MinecraftAuth.createHttpClient()
                        val javaSession = webLoginAuthStep.getFromInput(
                            httpClient,
                            LocalWebServerCallback {
                                Desktop.getDesktop().browse(URI(it.authenticationUrl))
                            })
                        ImGuiScreenAccountManager.accounts += AccountMicrosoft(javaSession, webLoginAuthStep)
                    }
                }
                sameLine()
                if (ImGuiScreenAccountManager.button(
                        FontAwesomeIcons.LaptopCode,
                        "Add the account using a device code."
                    )
                ) {
                    thread {
                        val httpClient = MinecraftAuth.createHttpClient()
                        val javaSession = MinecraftAuth.JAVA_DEVICE_CODE_LOGIN.getFromInput(
                            httpClient,
                            MsaDeviceCodeCallback { msaDeviceCode ->
                                Schizoid.logger.info("Go to " + msaDeviceCode.verificationUri)
                                Schizoid.logger.info("Enter code " + msaDeviceCode.userCode)
                                Desktop.getDesktop().browse(URI(msaDeviceCode.directVerificationUri))
                            })
                        ImGuiScreenAccountManager.accounts += AccountMicrosoft(
                            javaSession,
                            MinecraftAuth.JAVA_DEVICE_CODE_LOGIN
                        )
                    }
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
}
