/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.opengl

import com.google.common.math.IntMath
import com.mojang.blaze3d.platform.GlConst
import dev.lyzev.api.events.EventListener
import dev.lyzev.api.events.EventWindowResize
import dev.lyzev.api.events.on
import dev.lyzev.schizoid.Schizoid
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.Framebuffer
import net.minecraft.client.gl.SimpleFramebuffer
import net.minecraft.client.util.ScreenshotRecorder
import java.util.UUID

/**
 * A simple wrapper around the [SimpleFramebuffer] class.
 * This class is used to create a framebuffer with a size multiplier which uses [GlConst.GL_LINEAR] as the texture filter.
 *
 * @param lod The level of detail of the framebuffer.
 * @param useDepth Whether to use a depth attachment or not.
 * @see SimpleFramebuffer
 * @see EventListener
 */
class WrappedFramebuffer(
    val name: String = UUID.randomUUID().toString(),
    lod: Int = 0,
    width: Int = MinecraftClient.getInstance().window.framebufferWidth,
    height: Int = MinecraftClient.getInstance().window.framebufferHeight,
    useDepth: Boolean = false,
    fixedSize: Boolean = false,
    linear: Boolean = true
) : SimpleFramebuffer(
    width / IntMath.pow(2, lod),
    height / IntMath.pow(2, lod),
    useDepth,
    MinecraftClient.IS_SYSTEM_MAC
), EventListener {

    override val shouldHandleEvents = !fixedSize

    /**
     * Initializes the framebuffer.
     */
    init {
        Schizoid.logger.info("Initialize framebuffer $name")
        fbos.putIfAbsent(name, this)
        setClearColor(0f, 0f, 0f, 0f)
        clear()
        if (linear) setTexFilter(GlConst.GL_LINEAR)
        /**
         * Listens for window resize events and resizes the framebuffer accordingly.
         */
        on<EventWindowResize> {
            resize(
                MinecraftClient.getInstance().window.framebufferWidth / IntMath.pow(2, lod),
                MinecraftClient.getInstance().window.framebufferHeight / IntMath.pow(2, lod),
                MinecraftClient.IS_SYSTEM_MAC
            )
            if (linear) setTexFilter(GlConst.GL_LINEAR)
        }
    }

    override fun initFbo(width: Int, height: Int, getError: Boolean) {
        super.initFbo(width, height, getError)
        @Suppress("SENSELESS_COMPARISON") // false detection since the method is called from the constructor and the parameters are still null
        if (name != null)
            fbos.putIfAbsent(name, this)
    }

    override fun delete() {
        super.delete()
        fbos.remove(name)
    }

    companion object {
        val fbos = mutableMapOf<String, WrappedFramebuffer>()
    }
}

fun Framebuffer.clear() = clear(MinecraftClient.IS_SYSTEM_MAC)

fun Framebuffer.save() = ScreenshotRecorder.saveScreenshot(MinecraftClient.getInstance().runDirectory, this) {}
