/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature

import dev.lyzev.api.events.EventListener
import dev.lyzev.api.events.StartupEvent
import dev.lyzev.api.events.on
import org.reflections.Reflections
import java.lang.reflect.Modifier
import kotlin.jvm.internal.Reflection

/**
 * Singleton object responsible for initializing and managing features.
 */
object FeatureManager : EventListener {

    // The prefix used for commands.
    const val PREFIX = "."

    // A list of all features.
    val features = mutableListOf<Feature>()

    /**
     * Gets a feature by its name.
     */
    operator fun get(name: String): Feature? = features.firstOrNull { it.name.equals(name, true) }

    /**
     * Gets a list of features by their category.
     */
    operator fun get(category: Category): List<Feature> = features.filter { it.category == category }

    // Indicates whether the feature manager should handle events.
    override val shouldHandleEvents = true

    init {

        /**
         * Initializes the features during the startup event. It scans the "features" package for all features and
         * adds them to the [features] list.
         */
        on<StartupEvent>(1) {
            Reflections("${javaClass.packageName}.features").getSubTypesOf(Feature::class.java)
                .filter { !Modifier.isAbstract(it.modifiers) }
                .forEach { features += Reflection.getOrCreateKotlinClass(it).objectInstance as Feature }
            features.sortBy { it.name }
        }
    }
}
