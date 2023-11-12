/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature

import dev.lyzev.api.events.*
import dev.lyzev.schizoid.Schizoid
import dev.lyzev.schizoid.feature.FeatureManager.PREFIX
import dev.lyzev.schizoid.feature.FeatureManager.features
import dev.lyzev.schizoid.feature.features.command.Command
import me.xdrop.fuzzywuzzy.FuzzySearch
import net.kyori.adventure.platform.fabric.FabricClientAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.event.HoverEventSource
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket
import org.reflections.Reflections
import java.lang.reflect.Modifier
import kotlin.jvm.internal.Reflection


/**
 * Singleton object responsible for initializing and managing features.
 *
 * @property PREFIX The prefix used for commands.
 * @property features The list of features.
 */
object FeatureManager : EventListener {

    private const val PREFIX = "."

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
    fun sendChatMessage(message: Component) = FabricClientAudiences.of().audience().sendMessage(
        Component.text("[", NamedTextColor.GRAY).append(Component.text(Schizoid.MOD_NAME, NamedTextColor.GOLD))
            .append(Component.text("] ", NamedTextColor.GRAY)).append(message)
    )


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
                    val cmd = find<Command>(args[0])
                    if (cmd != null) {
                        cmd.args.reset()
                        val (success, error) = cmd.args.parse(*args.drop(1).toTypedArray())
                        if (success)
                            cmd()
                        else {
                            sendChatMessage(Component.text(error, NamedTextColor.RED))
                            sendChatMessage(Component.text("Usage: $PREFIX", NamedTextColor.RED).append(cmd.usage))
                        }
                    } else FuzzySearch.extractOne(args[0], features.flatMap { it.aliases.toList() }).let { result ->
                        val response = Component.text().content("Unknown command.").color(NamedTextColor.RED)
                        if (result.score > 80 && args[0].isNotBlank()) response.append(Component.text(" Did you mean "))
                            .append(
                                Component.text(PREFIX + result.string).clickEvent(
                                    ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, PREFIX + result.string)
                                ).hoverEvent(HoverEventSource {
                                    HoverEvent.hoverEvent(
                                        HoverEvent.Action.SHOW_TEXT, Component.text("Usage: $PREFIX").append(
                                            (features[result.index] as Command).usage
                                        )
                                    )
                                })
                            ).append(Component.text("?"))
                        else response.append(Component.text(" Try using ")).append(
                            Component.text(PREFIX + "help")
                                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, PREFIX + "help"))
                                .hoverEvent(HoverEventSource {
                                    HoverEvent.hoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        Component.text("Use the help command to get more information about commands.")
                                    )
                                })
                        ).append(Component.text(" for a list of commands."))
                        sendChatMessage(response.build())
                    }
                }
            }
        }
    }
}
