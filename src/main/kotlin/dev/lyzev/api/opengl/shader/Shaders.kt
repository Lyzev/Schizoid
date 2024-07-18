/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.opengl.shader

import com.mojang.blaze3d.systems.RenderSystem
import dev.lyzev.api.opengl.Render
import dev.lyzev.api.opengl.WrappedFramebuffer
import dev.lyzev.api.opengl.clear
import dev.lyzev.api.setting.settings.OptionEnum
import dev.lyzev.schizoid.Schizoid
import dev.lyzev.schizoid.feature.features.gui.FeatureImGui
import dev.lyzev.schizoid.feature.features.module.modules.render.ModuleToggleableBlur.mc
import net.minecraft.client.gl.Framebuffer
import net.minecraft.util.math.MathHelper
import org.joml.Matrix2f
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
        this["Tex0"] = 0
        this["HalfTexelSize"] = halfPixelSize
        this["Alpha"] = alpha
        this["Offset"] = offset
    }
}

object ShaderKawase : ShaderVertexFragment("Kawase") {

    fun setUniforms(pixelSize: Vector2f, size: Float, alpha: Boolean) {
        this["Texture"] = 0
        this["TexelSize"] = pixelSize
        this["Alpha"] = alpha
        this["Size"] = size
    }
}

object ShaderBox : ShaderVertexFragment("Box") {

    fun setUniforms(direction: Vector2f, pixelSize: Vector2f, alpha: Boolean, size: Int) {
        this["Tex0"] = 0
        this["Direction"] = direction
        this["TexelSize"] = pixelSize
        this["Alpha"] = alpha
        this["Size"] = size
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
        this["Texture"] = 0
        this["Direction"] = direction
        this["TexelSize"] = pixelSize
        this["Alpha"] = alpha
        this["Gaussian"] = gaussian
        this["Support"] = support
        this["LinearSampling"] = linearSampling
    }
}

object ShaderAcrylic : ShaderVertexFragment("Acrylic") {
    val initTime = System.nanoTime()
}

object ShaderTint : ShaderVertexFragment("Tint") {

    var rgbPukeMode = "Noise"
        set(value) {
            field = value
            reload()
        }

    @Suppress("USELESS_ELVIS")
    override fun preprocess(source: String) = super.preprocess(source.replace("\${RGBPukeMode}", "RGBPuke${rgbPukeMode ?: "Noise"}"))

    val initTime = System.nanoTime()

    enum class RGBPukeMode : OptionEnum {
        Noise,
        Circle;

        override val key = name
    }
}

object ShaderMask : ShaderVertexFragment("Mask")
object ShaderAdd : ShaderVertexFragment("Add")
object ShaderPassThrough : ShaderVertexFragment("PassThrough")
object ShaderNoAlpha : ShaderVertexFragment("NoAlpha")

object ShaderAlphaOcclusion : ShaderVertexFragment("AlphaOcclusion")

object ShaderJumpFloodInit : ShaderVertexFragment("JumpFloodInit")
object ShaderJumpFlood : ShaderVertexFragment("JumpFlood")

object ShaderOutlineSolid : ShaderVertexFragment("OutlineSolid")
object ShaderOutlineLinear : ShaderVertexFragment("OutlineLinear")

object ShaderDepth : ShaderVertexFragment("Depth")
object ShaderLinearizeDepth : ShaderVertexFragment("LinearizeDepth")

object ShaderThreshold : ShaderVertexFragment("Threshold")
object ShaderBlend : ShaderVertexFragment("Blend")

object ShaderReflection : ShaderVertexFragment("Reflection")

object ShaderColorGrading : ShaderVertexFragment("ColorGrading")

object ShaderParticle : ShaderCompute("Particle", 64, 1, 1) {

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
        super.draw()
        deltaTime = deltaTime.coerceAtMost(100f)

        GLFW.glfwGetCursorPos(Schizoid.mc.window.handle, xpos, ypos)
        this["MousePos"] =
            mousePos.set(xpos[0].toFloat(), Schizoid.mc.window.framebufferHeight.toFloat() - ypos[0].toFloat())

        this["ScreenSize"] = screenSize.set(
            Schizoid.mc.window.framebufferWidth.toFloat(), Schizoid.mc.window.framebufferHeight.toFloat()
        )

        val left = GLFW.glfwGetMouseButton(Schizoid.mc.window.handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS
        val right = GLFW.glfwGetMouseButton(Schizoid.mc.window.handle, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS

        var force = 0.0f

        val dist = 0.1f + sqrt(
            ((xpos[0] - lastMXPos) * (deltaTime / 16.6666)).pow(2.0) + ((ypos[0] - lastMYPos) * (deltaTime / 16.6666)).pow(
                2.0
            )
        ).toFloat() / 400.0f

        if (left) force += dist
        if (right) force -= dist
        if (left && right) {
            val time = (System.nanoTime() - beginTime) / 1_000_000f
            force = sin(time * .01f) * dist + dist / 2f
        }

        lastMXPos = xpos[0]
        lastMYPos = ypos[0]

        this["Force"] = force
        this["DeltaTime"] = deltaTime
        this["ColorIdle"] = FeatureImGui.colorScheme[FeatureImGui.mode].particleIdle
        this["ColorActive"] = FeatureImGui.colorScheme[FeatureImGui.mode].particleActive

        var processed = 0
        while (processed < amount) {
            val processing = localSizeX * localSizeY * localSizeZ
            this["ArrayOffset"] = processed
            glDispatchCompute(localSizeX, localSizeY, localSizeZ)
            processed += processing
        }
        glMemoryBarrier(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT)
        if (IS_AMD_VENDOR)
            glFinish()
        val time = (System.nanoTime() - beginTime) / 1_000_000.0
        deltaTime = (time - lastTime).toFloat()
        lastTime = time

        drawTexture()
    }

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

object ShaderRandom : ShaderVertexFragment("Random")

object ShaderMovingAveragesBox : ShaderCompute("MovingAveragesBox", 32, 1, 1) {

    private val horizontal = Matrix2f(1f, 0f, 0f, 1f)
    private val vertical = Matrix2f(0f, 1f, 1f, 0f)

    fun render(fbo: Framebuffer, texture: Int, direction: Boolean, alpha: Boolean, strength: Int) {
        bind()
        GL44.glBindImageTexture(0, fbo.colorAttachment, 0, false, 0, GL_WRITE_ONLY, GL_RGBA8)
        this["Img0"] = 0
        GL44.glBindImageTexture(1, texture, 0, false, 0, GL_READ_ONLY, GL_RGBA8)
        this["Img1"] = 1
        this["Direction", false] = if (direction) horizontal else vertical
        this["Alpha"] = alpha
        this["Strength"] = strength
        glDispatchCompute(
            ((if (direction) fbo.textureHeight else fbo.textureWidth) + localSizeX - 1) / localSizeX,
            localSizeY,
            localSizeZ
        )
        glMemoryBarrier(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT)
        if (IS_AMD_VENDOR)
            glFinish()
        unbind()
    }

    init {
        init()
    }
}

object ShaderGameOfLife : ShaderCompute("GameOfLife", 32, 1, 1) {

    private lateinit var before: WrappedFramebuffer
    private lateinit var after: WrappedFramebuffer

    private var initTime = System.currentTimeMillis()
    var queueGenPixels = true
    var deltaTime = 1000 / 10
    var size = 3
    var b = "3"
    var s = "236"

    override fun draw() {
        if (queueGenPixels) {
            generateRandomPixels()
            queueGenPixels = false
        }
        if (System.currentTimeMillis() - initTime > deltaTime) {
            initTime = System.currentTimeMillis()
            after.clear()
            bind()
            GL44.glBindImageTexture(0, before.colorAttachment, 0, false, 0, GL_READ_ONLY, GL_RGBA8)
            this["Img0"] = 0
            GL44.glBindImageTexture(1, after.colorAttachment, 0, false, 0, GL_WRITE_ONLY, GL_RGBA8)
            this["Img1"] = 1
            glDispatchCompute((before.textureHeight + localSizeX - 1) / localSizeX, localSizeY, localSizeZ)
            glMemoryBarrier(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT)
            if (IS_AMD_VENDOR)
                glFinish()
            unbind()
            before.clear()
            before.beginWrite(true)
            ShaderPassThrough.bind()
            RenderSystem.activeTexture(GL_TEXTURE0)
            after.beginRead()
            ShaderPassThrough["Tex0"] = 0
            ShaderPassThrough["Scale"] = 1f
            ShaderPassThrough["Alpha"] = true
            drawFullScreen()
            ShaderPassThrough.unbind()
        }
        Schizoid.mc.framebuffer.beginWrite(true)
        ShaderTint.bind()
        RenderSystem.activeTexture(GL_TEXTURE0)
        after.beginRead()
        ShaderTint["Tex0"] = 0
        ShaderTint["Color"] = FeatureImGui.colorScheme[FeatureImGui.mode].particleIdle
        ShaderTint["RGBPuke"] = false
        ShaderTint["Opacity"] = 1f
        ShaderTint["Alpha"] = true
        ShaderTint["Multiplier"] = 1f
        ShaderTint["Time"] = (System.nanoTime() - ShaderTint.initTime) / 1000000000f
        ShaderTint["Yaw"] = mc.player?.yaw ?: 0f
        ShaderTint["Pitch"] = mc.player?.pitch ?: 0f
        drawFullScreen()
        ShaderTint.unbind()
    }

    private fun generateRandomPixels() {
        before.clear()
        before.beginWrite(true)
        ShaderRandom.bind()
        ShaderRandom["Time"] = System.nanoTime() / 1000000000f
        drawFullScreen()
        ShaderRandom.unbind()
    }

    override fun delete() {
        super.delete()
        before.delete()
        after.delete()
    }

    override fun init() {
        super.init()
        before = WrappedFramebuffer("GameOfLife+Before", size, linear = false)
        after = WrappedFramebuffer("GameOfLife+After", size, linear = false)
        queueGenPixels = true
    }

    override fun preprocess(source: String) = processIncludes(source).format(
        localSizeX,
        localSizeY,
        localSizeZ,
        b.toCharArray().joinToString(", "),
        s.toCharArray().joinToString(", ")
    )

    init {
        init()
    }
}

object ShaderPosCol : ShaderVertexFragment("PosCol")
