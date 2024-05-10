/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.opengl.shader

import dev.lyzev.api.events.EventListener
import dev.lyzev.schizoid.Schizoid
import dev.lyzev.schizoid.feature.features.gui.guis.ImGuiScreenFeature
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL43.*
import org.lwjgl.opengl.GL44
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object ShaderDualKawaseDown : ShaderDualKawase("Down")
object ShaderDualKawaseUp : ShaderDualKawase("Up")

abstract class ShaderDualKawase(shader: String) : ShaderVertexFragment("DualKawase$shader") {

    fun setUniforms(offset: Float, halfPixelSize: Vector2f, alpha: Boolean) {
        this["uTexture"] = 0
        this["uHalfTexelSize"] = halfPixelSize
        this["uAlpha"] = alpha
        this["uOffset"] = offset
    }
}

object ShaderKawase : ShaderVertexFragment("Kawase") {

    fun setUniforms(pixelSize: Vector2f, size: Float, alpha: Boolean) {
        this["uTexture"] = 0
        this["uTexelSize"] = pixelSize
        this["uAlpha"] = alpha
        this["uSize"] = size
    }
}

object ShaderBox : ShaderVertexFragment("Box") {

    fun setUniforms(direction: Vector2f, pixelSize: Vector2f, alpha: Boolean, size: Int) {
        this["uTexture"] = 0
        this["uDirection"] = direction
        this["uTexelSize"] = pixelSize
        this["uAlpha"] = alpha
        this["uSize"] = size
    }
}

object ShaderGaussian : ShaderVertexFragment("Gaussian") {

    fun setUniforms(
        direction: Vector2f,
        pixelSize: Vector2f,
        alpha: Boolean,
        gaussian: Vector3f,
        support: Int,
        linearSampling: Boolean
    ) {
        this["texture"] = 0
        this["direction"] = direction
        this["texelSize"] = pixelSize
        this["alpha"] = alpha
        this["gaussian"] = gaussian
        this["support"] = support
        this["linearSampling"] = linearSampling
    }
}

object ShaderAcrylic : ShaderVertexFragment("Acrylic")
object ShaderTint : ShaderVertexFragment("Tint")

object ShaderMask : ShaderVertexFragment("Mask")
object ShaderAdd : ShaderVertexFragment("Add")
object ShaderPassThrough : ShaderVertexFragment("PassThrough")

object ShaderDepth : ShaderVertexFragment("Depth")

object ShaderThreshold : ShaderVertexFragment("Threshold")
object ShaderBlend : ShaderVertexFragment("Blend")
object ShaderFlip : ShaderVertexFragment("Flip")

object ShaderReflection : ShaderVertexFragment("Reflection")

object ShaderParticle : ShaderCompute("Particle", 64, 1, 1), EventListener {

    var amount = 100_000
    private val xpos = doubleArrayOf(0.0)
    private val ypos = doubleArrayOf(0.0)
    private val mousePos = Vector2f()
    private val screenSize = Vector2f()

    private val beginTime = System.nanoTime()
    private var lastTime = 0.0
    private var deltaTime = 1.0f

    private var lastMXPos = -1.0
    private var lastMYPos = -1.0

    override fun draw() {
        deltaTime = deltaTime.coerceAtMost(100f)
        super.draw()
        bind()
        bindImageTexture()
        this["imgOutput"] = 0

        GLFW.glfwGetCursorPos(Schizoid.mc.window.handle, xpos, ypos)
        this["mousePos"] =
            mousePos.set(xpos[0].toFloat(), Schizoid.mc.window.framebufferHeight.toFloat() - ypos[0].toFloat())

        this["screenSize"] = screenSize.set(
            Schizoid.mc.window.framebufferWidth.toFloat(),
            Schizoid.mc.window.framebufferHeight.toFloat()
        )

        val left = GLFW.glfwGetMouseButton(Schizoid.mc.window.handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS
        val right = GLFW.glfwGetMouseButton(Schizoid.mc.window.handle, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS

        var force = 0.0f

        val dist = 0.1f + sqrt(((xpos[0] - lastMXPos) * (deltaTime / 16.6666)).pow(2.0) + ((ypos[0] - lastMYPos) * (deltaTime / 16.6666)).pow(2.0)).toFloat() / 400.0f

        if (left) force += dist
        if (right) force -= dist
        if (left && right) {
            val time = (System.nanoTime() - beginTime) / 1_000_000.0
            force = sin(time * 0.003f).toFloat() * 0.55f + 0.5f
        }

        lastMXPos = xpos[0]
        lastMYPos = ypos[0]

        this["force"] = force
        this["deltaTime"] = deltaTime
        this["colorIdle"] = ImGuiScreenFeature.colorScheme[ImGuiScreenFeature.mode].particleIdle
        this["colorActive"] = ImGuiScreenFeature.colorScheme[ImGuiScreenFeature.mode].particleActive

        var processed = 0
        while (processed < amount) {
            val processing = myGroupSizeX * myGroupSizeY * myGroupSizeZ
            this["arrayOffset"] = processed
            glDispatchCompute(myGroupSizeX, myGroupSizeY, myGroupSizeZ)
            processed += processing
        }
        glMemoryBarrier(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT)
        val time = (System.nanoTime() - beginTime) / 1_000_000.0
        deltaTime = (time - lastTime).toFloat()
        lastTime = time

        Schizoid.mc.framebuffer.beginWrite(true)
        drawTexture()
    }

    override val shouldHandleEvents = true

    private var positionBuffer: Int = 0

    override fun init() {
        super.init()
        bind()
        val buffer = (0..amount).flatMap {
            listOf(
                ThreadLocalRandom.current().nextFloat() * Schizoid.mc.window.framebufferWidth,
                ThreadLocalRandom.current().nextFloat() * Schizoid.mc.window.framebufferHeight,
                0.0f,
                0.0f
            )
        }.toTypedArray().toFloatArray()
        if (positionBuffer != 0) {
            GL15.glDeleteBuffers(positionBuffer)
        }
        positionBuffer = glGenBuffers()
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, positionBuffer)
        GL44.glBufferStorage(GL_SHADER_STORAGE_BUFFER, buffer, GL_MAP_WRITE_BIT or GL44.GL_DYNAMIC_STORAGE_BIT)
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, positionBuffer)
        unbind()
    }

    init {
        init()
    }
}
