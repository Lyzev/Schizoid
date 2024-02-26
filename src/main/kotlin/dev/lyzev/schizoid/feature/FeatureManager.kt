/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature

import dev.lyzev.api.events.*
import dev.lyzev.api.glfw.GLFWKey
import dev.lyzev.schizoid.Schizoid
import dev.lyzev.schizoid.Schizoid.mc
import dev.lyzev.schizoid.feature.FeatureManager.PREFIX
import dev.lyzev.schizoid.feature.FeatureManager.features
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

    val features = mutableListOf<IFeature>()

    /**
     * Gets a list of features by their type.
     */
    inline fun <reified T : IFeature> get(): List<T> = features.filterIsInstance<T>()

    /**
     * Gets a feature by its name and type.
     *
     * @param alias The name of the feature.
     */
    inline fun <reified T : IFeature> find(alias: String): T? =
        features.filterIsInstance<T>().firstOrNull { feature -> feature.aliases.any { it.equals(alias, true) } }

    /**
     * Gets a feature by its name.
     */
    operator fun get(alias: String): IFeature? = features.firstOrNull { it.aliases.contains(alias) }

    /**
     * Gets a list of features by their category.
     */
    operator fun get(category: Feature.Category): List<IFeature> = features.filter { it.category == category }

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
            Reflections("${javaClass.packageName}.features").getSubTypesOf(IFeature::class.java)
                .filter { !Modifier.isInterface(it.modifiers) && !Modifier.isAbstract(it.modifiers) }
                .forEach {
                    val feature = Reflection.getOrCreateKotlinClass(it).objectInstance as IFeature
                    features += feature
                    Schizoid.logger.info("Initialized feature: ${feature.name}.")
                }
            features.sortBy { it.name }
        }

        on<EventKeystroke> { event ->
            if (mc.currentScreen == null && event.action == 1)
                features.filter { it.keybinds.contains(GLFWKey[event.key]) }.forEach { it.keybindReleased() }
        }

        on<EventMouseClick> { event ->
            if (mc.currentScreen == null && event.action == 1)
                features.filter { it.keybinds.contains(GLFWKey[event.button]) }.forEach { it.keybindReleased() }
        }

        on<EventSendPacket> { event ->
            if (event.packet is ChatMessageC2SPacket) {
                val message = event.packet.chatMessage
                if (message.startsWith(PREFIX)) {
                    event.isCancelled = true
                    val args = message.substring(PREFIX.length).split(" ")
                    val feature = find<IFeature>(args[0])
                    if (feature != null) {
                        feature.keybindReleased()
                        sendChatMessage(Component.text("Called ${feature.name}."))
                    } else FuzzySearch.extractOne(args[0], features.flatMap { it.aliases.toList() }).let { result ->
                        val response = Component.text().content("Unknown feature.").color(NamedTextColor.RED)
                        if (result.score > 80 && args[0].isNotBlank()) response.append(Component.text(" Did you mean "))
                            .append(
                                Component.text(PREFIX + result.string).clickEvent(
                                    ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, PREFIX + result.string)
                                ).hoverEvent(HoverEventSource {
                                    HoverEvent.showText(
                                        Component.text("Usage: $PREFIX${result.string}")
                                    )
                                })
                            ).append(Component.text("?"))
                        else response.append(Component.text(" Try using ")).append(
                            Component.text("${PREFIX}features")
                                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, "${PREFIX}features"))
                                .hoverEvent(HoverEventSource {
                                    HoverEvent.showText(
                                        Component.text("Opens the feature screen.")
                                    )
                                })
                        ).append(Component.text(" to see all features."))
                        sendChatMessage(response.build())
                    }
                }
            }
        }
    }
}
