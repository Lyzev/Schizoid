/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature

import dev.lyzev.api.events.*
import dev.lyzev.schizoid.Schizoid
import dev.lyzev.schizoid.feature.features.command.Command
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket
import net.minecraft.text.Text
import org.reflections.Reflections
import java.lang.reflect.Modifier
import kotlin.jvm.internal.Reflection

/**
 * Singleton object responsible for initializing and managing features.
 */
object FeatureManager : EventListener {

    // The prefix used for commands.
    private const val PREFIX = "."

    // A list of all features.
    val features = mutableListOf<Feature>()

    /**
     * Gets a list of features by their type.
     */
    inline fun <reified T : Feature> get(): List<T> = features.filterIsInstance<T>()

    /**
     * Gets a feature by its name and type.
     *
     * @param alias The name of the feature.
     */
    inline fun <reified T : Feature> find(alias: String): T? =
        features.filterIsInstance<T>().firstOrNull { feature -> feature.aliases.any { it.equals(alias, true) } }

    /**
     * Gets a feature by its name.
     */
    operator fun get(alias: String): Feature? = features.firstOrNull { it.aliases.contains(alias) }

    /**
     * Gets a list of features by their category.
     */
    operator fun get(category: Feature.Category): List<Feature> = features.filter { it.category == category }

    /**
     * Sends a chat message to the player.
     *
     * @param message The message to send.
     */
    fun sendChatMessage(message: String) =
        Schizoid.mc.player?.sendMessage(Text.of("§7[§6${Schizoid.MOD_NAME}§7] §f$message"))

    // Indicates whether the feature manager should handle events.
    override val shouldHandleEvents = true

    init {

        /**
         * Initializes the features during the startup event. It scans the "features" package for all features and
         * adds them to the [features] list.
         */
        on<EventStartup>(Event.Priority.HIGH) {
            Reflections("${javaClass.packageName}.features").getSubTypesOf(Feature::class.java)
                .filter { !Modifier.isAbstract(it.modifiers) }
                .forEach { features += Reflection.getOrCreateKotlinClass(it).objectInstance as Feature }
            features.sortBy { it.name }
        }

        on<EventSendPacket> { event ->
            if (event.packet is ChatMessageC2SPacket) {
                val message = event.packet.chatMessage
                if (message.startsWith(PREFIX)) {
                    event.isCancelled = true
                    val args = message.substring(PREFIX.length).split(" ")
                    val feature = find<Command>(args[0])
                    if (feature != null) {
                        feature(args.drop(1))
                    } else {
                        sendChatMessage("Unknown command.")
                    }
                }
            }
        }
    }
}
