/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.account

import com.mojang.authlib.minecraft.MinecraftSessionService
import com.mojang.authlib.minecraft.UserApiService
import com.mojang.authlib.yggdrasil.ServicesKeyType
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService
import com.mojang.authlib.yggdrasil.YggdrasilEnvironment
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService
import dev.lyzev.schizoid.injection.accessor.FileCacheAccessor
import dev.lyzev.schizoid.Schizoid
import dev.lyzev.schizoid.feature.features.gui.guis.ImGuiScreenAccountManager.mc
import dev.lyzev.schizoid.injection.accessor.*
import kotlinx.serialization.json.JsonElement
import net.minecraft.client.network.SocialInteractionsManager
import net.minecraft.client.realms.RealmsClient
import net.minecraft.client.realms.RealmsPeriodicCheckers
import net.minecraft.client.session.ProfileKeys
import net.minecraft.client.session.Session
import net.minecraft.client.session.Session.AccountType
import net.minecraft.client.session.report.AbuseReportContext
import net.minecraft.client.texture.AbstractTexture
import net.minecraft.client.texture.PlayerSkinProvider
import net.minecraft.network.encryption.SignatureVerifier
import java.net.URI
import java.util.concurrent.CompletableFuture


interface Account {

    val type: AccountType

    fun getSession(): Session?

    fun render()

    fun save(): JsonElement

    companion object {

        // Credit to https://github.com/axieum/authme
        fun setSession(session: Session) {
            // Use an accessor mixin to update the 'private final' Minecraft session
            (mc as MinecraftClientAccessor).setSession(session)
            (mc.splashTextLoader as SplashTextResourceSupplierAccessor).setSession(session)

            // Re-create the game profile future
            (mc as MinecraftClientAccessor).setGameProfileFuture(CompletableFuture.supplyAsync {
                mc.sessionService.fetchProfile(
                    session.uuidOrNull, true
                )
            })

            // Re-create the user API service (ignore offline session)
            var userApiService = UserApiService.OFFLINE
            if (session.accessToken.isNotBlank()) {
                userApiService =
                    (mc as MinecraftClientAccessor).authenticationService.createUserApiService(session.accessToken)
            }
            (mc as MinecraftClientAccessor).setUserApiService(userApiService)

            // Re-create the social interactions manager
            (mc as MinecraftClientAccessor).setSocialInteractionsManager(
                SocialInteractionsManager(mc, userApiService)
            )

            // Re-create the profile keys
            (mc as MinecraftClientAccessor).setProfileKeys(
                ProfileKeys.create(userApiService, session, mc.runDirectory.toPath())
            )

            // Re-create the abuse report context
            (mc as MinecraftClientAccessor).setAbuseReportContext(
                AbuseReportContext.create(
                    (mc.abuseReportContext as AbuseReportContextAccessor).getEnvironment(), userApiService
                )
            )

            // Necessary for Realms to re-check for a valid session
            val realmsClient = RealmsClient.createRealmsClient(mc)
            (mc as MinecraftClientAccessor).setRealmsPeriodicCheckers(RealmsPeriodicCheckers(realmsClient))
            RealmsAvailabilityAccessor.setCurrentFuture(null)

            Schizoid.logger.info(
                "Minecraft session for {} (uuid={}) has been applied", session.username, session.uuidOrNull
            )
        }

        // Credit to https://github.com/MeteorDevelopment/meteor-client
        fun applyLoginEnvironment(authService: YggdrasilAuthenticationService, sessService: MinecraftSessionService?) {
            val mca = mc as MinecraftClientAccessor
            mca.setAuthenticationService(authService)
            SignatureVerifier.create(authService.servicesKeySet, ServicesKeyType.PROFILE_KEY)
            mca.setSessionService(sessService)
            val skinCache = (mc.skinProvider as PlayerSkinProviderAccessor).getSkinCache()
            val skinCachePath = (skinCache as FileCacheAccessor).getDirectory()
            mca.setSkinProvider(PlayerSkinProvider(mc.textureManager, skinCachePath, sessService, mc))
        }
    }
}
