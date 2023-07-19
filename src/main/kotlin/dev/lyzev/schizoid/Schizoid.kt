/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid

import net.fabricmc.api.ModInitializer
import org.apache.logging.log4j.LogManager

object Schizoid : ModInitializer {

    const val MOD_ID = "schizoid"
    const val VERSION = "1.0.0"
    val AUTHORS = listOf("Lyzev")

    val logger = LogManager.getLogger(MOD_ID)

    override fun onInitialize() {
        val init = System.currentTimeMillis()
        runCatching {
            logger.info("Initializing Schizoid v$VERSION by ${AUTHORS.joinToString(" & ")}...")
            TODO("Finish client base before initializing!")
        }.onSuccess {
            logger.info("Initialized Schizoid v$VERSION by ${AUTHORS.joinToString(" & ")} in ${System.currentTimeMillis() - init}ms!")
        }.onFailure {
            logger.error("Failed to initialize Schizoid!", it)
        }
    }
}