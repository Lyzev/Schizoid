/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.render

import com.mojang.blaze3d.systems.RenderSystem
import dev.lyzev.api.events.*
import dev.lyzev.api.opengl.WrappedFramebuffer
import dev.lyzev.api.opengl.shader.Shader
import dev.lyzev.api.opengl.shader.ShaderDistortion
import dev.lyzev.api.opengl.shader.ShaderGlass
import dev.lyzev.api.opengl.shader.ShaderGlitch
import dev.lyzev.api.opengl.shader.blur.BlurHelper
import dev.lyzev.api.setting.settings.OptionEnum
import dev.lyzev.api.setting.settings.option
import dev.lyzev.api.setting.settings.slider
import dev.lyzev.api.settings.Setting.Companion.neq
import dev.lyzev.schizoid.Schizoid.mc
import dev.lyzev.schizoid.feature.features.module.ModuleToggleable
import net.minecraft.client.render.*
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.RotationAxis
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import org.joml.Vector2f
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12
import org.lwjgl.opengl.GL13
import su.mandora.tarasande.util.render.animation.EasingFunction
import java.nio.ByteBuffer
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

// TODO: NOT FINISHED!!!
// apply shaders on torus and more
// improve performance
// fix crash when unpause reflection mode
// command to create entity for testing: /summon minecraft:villager ~ ~ ~ {Attributes:[{Name:generic.maxHealth, Base:2147483647}],NoAI:1,Health:2147483647}
object ModuleToggleableTorus :
    ModuleToggleable("Torus", "Renders a Torus.", category = Category.RENDER),
    EventListener {

    private val toruses = mutableListOf<Torus>()
    private val mask = WrappedFramebuffer(useDepth = true)
    private val texelSize = Vector2f()

    val mode by option("Mode", "The effect to apply to the torus", Modes.BLUR, Modes.entries)

    override val shouldHandleEvents: Boolean
        get() = isEnabled && !mc.gameRenderer.isRenderingPanorama

    init {
        on<EventAttackEntity> {
            toruses.add(Torus(mc.crosshairTarget!!.pos, mc.player!!.rotationClient))
        }

        on<EventRenderWorld>(Event.Priority.LOW) { event ->
            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            RenderSystem.defaultBlendFunc()
            RenderSystem.enableBlend()
//            mask.copyDepthFrom(mc.framebuffer)
            toruses.removeIf { System.currentTimeMillis() - it.spawn > 1000L }
            if (mode == Modes.REFLECTION) {
                mc.framebuffer.beginWrite(true)
                RenderSystem.disableDepthTest()
                RenderSystem.depthMask(false)
                if (mc.currentScreen == null || !mc.currentScreen!!.shouldPause()) {
                    CubeMapRenderer.render()
                }
            } else {
                mask.clear()
                mask.beginWrite(true)
                RenderSystem.disableDepthTest()
                RenderSystem.setShader { GameRenderer.getPositionColorProgram() }
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
            }
            for (torus in toruses) {
                torus.render(event.matrices)
            }
            GL11.glDisable(GL11.GL_LINE_SMOOTH)

            if (toruses.isNotEmpty())
                mode.render()

            RenderSystem.disableDepthTest()
            RenderSystem.enableCull()
            RenderSystem.depthMask(true)
            RenderSystem.activeTexture(GL13.GL_TEXTURE0)
            mc.framebuffer.beginWrite(true)
        }
    }

    class Torus(private val pos: Vec3d, private val rotation: Vec2f) {

        val spawn = System.currentTimeMillis()

        fun render(matrices: MatrixStack) {
            val cam = mc.gameRenderer.camera.pos
            matrices.push()
            matrices.translate(pos.x - cam.x, pos.y - cam.y, pos.z - cam.z)
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-rotation.y)) // yaw
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(rotation.x)) // pitch
            val model = matrices.peek().positionMatrix
            if (mode == Modes.REFLECTION) {
                RenderSystem.disableCull()
                ShaderGlass.bind()
                ShaderGlass["ModelViewMat", false] = model
                ShaderGlass["ProjMat", false] = RenderSystem.getProjectionMatrix()
                GL13.glActiveTexture(GL13.GL_TEXTURE0)
//                mc.framebuffer.beginRead()
                GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, Modes.REFLECTION.cubeMapRenderer.cubeMapTextureId)
                ShaderGlass["cubeMap"] = 0
                ShaderGlass["CameraPosition"] = cam.toVector3f()
            }
            val delta = System.currentTimeMillis() - spawn
            val bufferBuilder = Tessellator.getInstance().buffer

            val slices = 10
            val loops = 30
            val rad = .6f + 2f * delta / 1000f
            val innerRad = .4f - .4f * EasingFunction.IN_CUBIC(delta / 1000.0).toFloat()
            val outerRad = rad
            if (mode == Modes.REFLECTION) {
                bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION)
            } else {
                bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)
            }
            for (i in 0 until slices) {
                val theta = 2f * Math.PI.toFloat() * i / slices
                val nextTheta = 2f * Math.PI.toFloat() * (i + 1) / slices

                for (j in 0 until loops) {
                    val phi = 2f * Math.PI.toFloat() * j / loops
                    val nextPhi = 2f * Math.PI.toFloat() * (j + 1) / loops

                    val x1 = (outerRad + innerRad * cos(theta)) * cos(phi)
                    val y1 = (outerRad + innerRad * cos(theta)) * sin(phi)
                    val z1 = innerRad * sin(theta)

                    val x2 = (outerRad + innerRad * cos(nextTheta)) * cos(phi)
                    val y2 = (outerRad + innerRad * cos(nextTheta)) * sin(phi)
                    val z2 = innerRad * sin(nextTheta)

                    val x3 = (outerRad + innerRad * cos(nextTheta)) * cos(nextPhi)
                    val y3 = (outerRad + innerRad * cos(nextTheta)) * sin(nextPhi)
                    val z3 = innerRad * sin(nextTheta)

                    val x4 = (outerRad + innerRad * cos(theta)) * cos(nextPhi)
                    val y4 = (outerRad + innerRad * cos(theta)) * sin(nextPhi)
                    val z4 = innerRad * sin(theta)

                    if (mode == Modes.REFLECTION) {
                        bufferBuilder.vertex(x1.toDouble(), y1.toDouble(), z1.toDouble()).normal(x1, y1, z1).next()
                        bufferBuilder.vertex(x2.toDouble(), y2.toDouble(), z2.toDouble()).normal(x2, y2, z2).next()
                        bufferBuilder.vertex(x3.toDouble(), y3.toDouble(), z3.toDouble()).normal(x3, y3, z3).next()
                        bufferBuilder.vertex(x4.toDouble(), y4.toDouble(), z4.toDouble()).normal(x4, y4, z4).next()
                    } else {
                        bufferBuilder.vertex(model, x1, y1, z1).color(1f, 1f, 1f, 1f).next()
                        bufferBuilder.vertex(model, x2, y2, z2).color(1f, 1f, 1f, 1f).next()
                        bufferBuilder.vertex(model, x3, y3, z3).color(1f, 1f, 1f, 1f).next()
                        bufferBuilder.vertex(model, x4, y4, z4).color(1f, 1f, 1f, 1f).next()
                    }
                }
            }
            if (mode == Modes.REFLECTION) {
                BufferRenderer.draw(bufferBuilder.end())
            } else {
                BufferRenderer.drawWithGlobalProgram(bufferBuilder.end())
            }
            matrices.pop()
            if (mode == Modes.REFLECTION) {
                ShaderGlass.unbind()
            }
        }
    }

    interface Mode {
        fun render()
    }

    enum class Modes(override val key: String) : OptionEnum, Mode {
        BLUR("Blur") {

            val strength by slider("Blur Strength", "The strength of the blur effect.", 6, 1, 20, hide = ::mode neq this)

            override fun render() {
                BlurHelper.draw(mask, false, blurStrength = strength)
            }
        },
        GLITCH("Glitch") {

            val rate by slider("Glitch Rate", "The rate of the glitch effect.", 75, 1, 100, "%%", true, ::mode neq this)
            val speed by slider("Glitch Speed", "The speed of the glitch effect.", 75, 1, 100, "%%", true, ::mode neq this)

            override fun render() {
                RenderSystem.disableCull()
                mc.framebuffer.beginWrite(true)
                ShaderGlitch.bind()
                RenderSystem.activeTexture(GL13.GL_TEXTURE1)
                mc.framebuffer.beginRead()
                ShaderGlitch["scene"] = 1
                RenderSystem.activeTexture(GL13.GL_TEXTURE0)
                mask.beginRead()
                ShaderGlitch["mask"] = 0
                ShaderGlitch["texelSize"] =
                    texelSize.set(1f / mc.framebuffer.textureWidth, 1f / mc.framebuffer.textureHeight)
                ShaderGlitch["time"] = (System.nanoTime() * speed / 100f) / 1e9f
                ShaderGlitch["rate"] = rate / 100f
                Shader.drawFullScreen()
                ShaderGlitch.unbind()
            }
        },
        DISTORTION("Distortion") {

            val speed by slider("Distortion Speed", "The speed of the distortion effect.", 75, 1, 100, "%%", true, ::mode neq this)

            override fun render() {
                RenderSystem.disableCull()
                mc.framebuffer.beginWrite(true)
                ShaderDistortion.bind()
                RenderSystem.activeTexture(GL13.GL_TEXTURE1)
                mc.framebuffer.beginRead()
                ShaderDistortion["scene"] = 1
                RenderSystem.activeTexture(GL13.GL_TEXTURE0)
                mask.beginRead()
                ShaderDistortion["mask"] = 0
                ShaderDistortion["texelSize"] =
                    texelSize.set(1f / mc.framebuffer.textureWidth, 1f / mc.framebuffer.textureHeight)
                ShaderDistortion["time"] = (System.nanoTime() * speed / 100f) / 1e9f
                Shader.drawFullScreen()
                ShaderDistortion.unbind()
            }
        },
        REFLECTION("Reflection") {

            override fun render() {
            }
        };
        val cubeMapRenderer = CubeMapRenderer
    }
}


object CubeMapRenderer : EventListener {

    var cubeMapTextureId = -1

    private fun getTextureData(textureId: Int, size: Int): ByteBuffer {
        // Bind the texture
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId)

        // Allocate a ByteBuffer to store the pixel data
        val buffer: ByteBuffer = BufferUtils.createByteBuffer(size * size * 4) // 3 for RGB

        // Read the pixel data into the ByteBuffer
        GL11.glReadPixels(0, 0, size, size, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer)

        // Unbind the texture
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0)

        return buffer
    }

    fun render() {
        val size = min(mc.window.framebufferWidth, mc.window.framebufferHeight)
        val textureData = getTextureData(mc.framebuffer.colorAttachment, size)

        // Bind the cube map texture
        GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, cubeMapTextureId)

        // For each face of the cube
        for (i in 0..5) {
            // Render the 2D texture to the face
            GL11.glTexImage2D(
                GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i,
                0,
                GL11.GL_RGBA,
                size,
                size,
                0,
                GL11.GL_RGBA,
                GL11.GL_UNSIGNED_BYTE,
                textureData
            )
        }
    }

    override val shouldHandleEvents: Boolean
        get() = true

    init {
        cubeMapTextureId = GL11.glGenTextures()
        GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, cubeMapTextureId)

        val size = min(mc.window.framebufferWidth, mc.window.framebufferHeight)

        // For each face of the cube
        for (i in 0 until 6) {
            // Create the face
            GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL11.GL_RGBA, size, size, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, null as ByteBuffer?)
        }

        // Set texture parameters
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, 0)

        on<EventWindowResize> {
            // delete old texture
            GL11.glDeleteTextures(cubeMapTextureId)
            // create new texture
            cubeMapTextureId = GL11.glGenTextures()
            GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, cubeMapTextureId)

            val size = min(mc.window.framebufferWidth, mc.window.framebufferHeight)

            // For each face of the cube
            for (i in 0 until 6) {
                // Create the face
                GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL11.GL_RGBA, size, size, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, null as ByteBuffer?)
            }

            // Set texture parameters
            GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
            GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, 0)
        }
    }
}
