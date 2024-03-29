/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature

import dev.lyzev.api.events.*
import dev.lyzev.api.glfw.GLFWKey
import dev.lyzev.schizoid.Schizoid
import dev.lyzev.schizoid.Schizoid.mc
import net.minecraft.text.Text
import org.reflections.Reflections
import java.lang.reflect.Modifier
import kotlin.jvm.internal.Reflection


/**
 * Singleton object responsible for initializing and managing features.
 */
object FeatureManager : EventListener {

    /**
     * The list of features.
     */
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
    inline fun <reified T : IFeature> find(name: String): T? =
        features.filterIsInstance<T>().firstOrNull { feature -> feature.name.equals(name, true) }

    /**
     * Gets a feature by its name.
     */
    operator fun get(name: String): IFeature? = features.firstOrNull { feature -> feature.name.equals(name, true) }

    /**
     * Gets a list of features by their category.
     */
    operator fun get(category: Feature.Category): List<IFeature> = features.filter { it.category == category }

    /**
     * Gets a list of features by their categories.
     */
    operator fun get(vararg category: Feature.Category): List<IFeature> = features.filter { category.contains(it.category) }

    /**
     * Sends a chat message to the player.
     *
     * @param message The message to send.
     */
    fun sendChatMessage(text: Text) = mc.player?.sendMessage(text)

    override val shouldHandleEvents = true

    init {
        // Initialize features.
        Reflections("${javaClass.packageName}.features").getSubTypesOf(IFeature::class.java)
            .filter { !Modifier.isInterface(it.modifiers) && !Modifier.isAbstract(it.modifiers) }
            .forEach {
                val feature = Reflection.getOrCreateKotlinClass(it).objectInstance as IFeature
                features += feature
                Schizoid.logger.info("Initialized feature: ${feature.name}.")
            }
        features.sortBy { it.name }

        on<EventKeystroke> { event ->
            if (mc.currentScreen == null && event.action == 1)
                features.filter { it.keybinds.contains(GLFWKey[event.key]) }.forEach { it.keybindReleased() }
        }

        on<EventMouseClick> { event ->
            if (mc.currentScreen == null && event.action == 1)
                features.filter { it.keybinds.contains(GLFWKey[event.button]) }.forEach { it.keybindReleased() }
        }
    }
}
