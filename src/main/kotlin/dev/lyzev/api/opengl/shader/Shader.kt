/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.opengl.shader

import com.mojang.blaze3d.systems.RenderSystem
import dev.lyzev.api.events.EventListener
import dev.lyzev.api.events.EventReload
import dev.lyzev.api.events.EventWindowResize
import dev.lyzev.api.events.on
import dev.lyzev.schizoid.Schizoid
import net.minecraft.client.gl.GlProgramManager
import net.minecraft.client.render.BufferRenderer
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.GL_RGBA32F
import org.lwjgl.opengl.GL43.GL_COMPUTE_SHADER
import org.lwjgl.opengl.GL44
import java.awt.Color
import java.io.FileNotFoundException

/**
 * Abstract class representing a Shader.
 * @param shader The name of the shader.
 */
abstract class Shader(val shader: String) : EventListener {

    var program = glCreateProgram()
    private val uniforms = HashMap<String, Int>()

    /**
     * Binds the shader program.
     */
    fun bind() = GlProgramManager.useProgram(program)

    /**
     * Binds the shader program.
     */
    fun unbind() = GlProgramManager.useProgram(0)

    /**
     * Sets a uniform float value.
     * @param name The name of the uniform.
     * @param value The float value to set.
     */
    operator fun set(name: String, value: Float) = glUniform1f(this[name], value)

    /**
     * Sets a uniform integer value.
     * @param name The name of the uniform.
     * @param value The integer value to set.
     */
    operator fun set(name: String, value: Int) = glUniform1i(this[name], value)

    /**
     * Sets a uniform integer array value.
     * @param name The name of the uniform.
     * @param value The integer array value to set.
     */
    operator fun set(name: String, value: IntArray) = glUniform1iv(this[name], value)

    /**
     * Sets a uniform boolean value.
     * @param name The name of the uniform.
     * @param value The boolean value to set.
     */
    operator fun set(name: String, value: Boolean) = glUniform1i(this[name], if (value) 1 else 0)

    /**
     * Sets a uniform [Vector2f] value.
     * @param name The name of the uniform.
     * @param value The [Vector2f] value to set.
     */
    operator fun set(name: String, value: Vector2f) = glUniform2f(this[name], value.x, value.y)

    /**
     * Sets a uniform [Vector3f] value.
     * @param name The name of the uniform.
     * @param value The [Vector3f] value to set.
     */
    operator fun set(name: String, value: Vector3f) = glUniform3f(this[name], value.x, value.y, value.z)

    operator fun set(name: String, value: Color) =
        glUniform3f(this[name], value.red / 255f, value.green / 255f, value.blue / 255f)

    /**
     * Sets a uniform [Vector4f] value.
     * @param name The name of the uniform.
     * @param value The [Vector4f] value to set.
     */
    operator fun set(name: String, value: Vector4f) = glUniform4f(this[name], value.x, value.y, value.z, value.w)

    /**
     * Sets a uniform float array value.
     * @param name The name of the uniform.
     * @param value The float array value to set.
     */
    operator fun set(name: String, value: FloatArray) = glUniform1fv(this[name], value)

    /**
     * A buffer for matrix values.
     */
    private val buffer = FloatArray(16)

    /**
     * Sets a uniform [Matrix4f] value.
     * @param name The name of the uniform.
     * @param transpose Whether to transpose the matrix.
     * @param value The [Matrix4f] value to set.
     */
    operator fun set(name: String, transpose: Boolean, value: Matrix4f) =
        glUniformMatrix4fv(this[name], transpose, value.get(buffer))

    /**
     * Retrieves the location of a uniform variable.
     * @param name The name of the uniform.
     * @return The location of the uniform variable.
     */
    private operator fun get(name: String): Int = uniforms.getOrPut(name) { glGetUniformLocation(program, name) }

    /**
     * Processes include directives in shader source code.
     * @param source The shader source code.
     * @return The shader source code with include directives replaced by the included file content.
     */
    private fun processIncludes(source: String): String {
        var modifiedSource = source
        val includeRegex = Regex("#include \"(.*?)\"")
        var matchResult = includeRegex.find(modifiedSource)
        while (matchResult != null) {
            val includePath = matchResult.groupValues[1]
            val includeFile = javaClass.classLoader.getResource("$PATH/include/$includePath")
            if (includeFile == null) {
                Schizoid.logger.error(FileNotFoundException("Could not find include file: $includePath"))
                return modifiedSource
            }
            val includeContent = includeFile.readBytes().decodeToString()
            modifiedSource = modifiedSource.replace(matchResult.value, includeContent)
            matchResult = includeRegex.find(modifiedSource)
        }
        return modifiedSource
    }

    /**
     * Compiles a shader.
     * @param type The type of the shader.
     * @param source The shader source code.
     * @return The shader ID.
     */
    fun compile(type: Int, source: String): Int {
        val sourceWithIncludes = processIncludes(source)
        val shader = glCreateShader(type)
        glShaderSource(shader, sourceWithIncludes)
        glCompileShader(shader)
        val isCompiled = glGetShaderi(shader, GL_COMPILE_STATUS)
        if (isCompiled == 0) {
            Schizoid.logger.error(sourceWithIncludes)
            Schizoid.logger.error(
                glGetShaderInfoLog(shader, glGetShaderi(shader, GL_INFO_LOG_LENGTH)),
                IllegalStateException("Failed to compile shader")
            )
        }
        return shader
    }

    open fun init() {
        glLinkProgram(program)

        val isLinked = glGetProgrami(program, GL_LINK_STATUS)

        if (isLinked == 0)
            Schizoid.logger.error(
                glGetProgramInfoLog(
                    program,
                    glGetProgrami(program, GL_INFO_LOG_LENGTH)
                ), IllegalStateException("Shader failed to link")
            )
    }

    fun reload() {
        glDeleteProgram(program)
        program = glCreateProgram()
        init()
    }

    override val shouldHandleEvents = true

    init {
        on<EventReload> {
            if (!Schizoid.DEVELOPER_MODE) return@on
            reload()
        }
    }

    companion object {

        /**
         * The path to the shaders.
         */
        val PATH = "assets/${Schizoid.MOD_ID}/shaders"

        /**
         * Draws a full screen quad.
         */
        fun drawFullScreen() {
            val bufferBuilder = RenderSystem.renderThreadTesselator().buffer
            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION)
            bufferBuilder.vertex(1.0, 1.0, .0).next()
            bufferBuilder.vertex(1.0, -1.0, .0).next()
            bufferBuilder.vertex(-1.0, -1.0, .0).next()
            bufferBuilder.vertex(-1.0, 1.0, .0).next()
            BufferRenderer.draw(bufferBuilder.end())
        }
    }
}

abstract class ShaderVertexFragment(
    shader: String
) : Shader(shader) {

    final override fun init() {
        val vSource = ClassLoader.getSystemResourceAsStream("$PATH/core/$shader/${shader}_VP.glsl")?.use {
            it.readAllBytes().decodeToString()
        } ?: "null"
        val vShader = compile(GL_VERTEX_SHADER, vSource)
        glAttachShader(program, vShader)

        val fSource = ClassLoader.getSystemResourceAsStream("$PATH/core/$shader/${shader}_FP.glsl")?.use {
            it.readAllBytes().decodeToString()
        } ?: "null"
        val fShader = compile(GL_FRAGMENT_SHADER, fSource)
        glAttachShader(program, fShader)

        glDeleteShader(vShader)
        glDeleteShader(fShader)
        super.init()
    }

    init {
        init()
    }
}

abstract class ShaderCompute(
    shader: String,
    val myGroupSizeX: Int,
    val myGroupSizeY: Int,
    val myGroupSizeZ: Int
) : Shader(shader) {

    var texture: Int = 0

    open fun draw() = clearTexture()

    fun bindImageTexture() = GL44.glBindImageTexture(0, texture, 0, false, 0, GL_READ_WRITE, GL_RGBA32F)

    private fun clearTexture() = GL44.glClearTexImage(texture, 0, GL_RGBA, GL_FLOAT, transparent)

    fun drawTexture() {
        ShaderPassThrough.bind()
        RenderSystem.activeTexture(GL_TEXTURE0)
        RenderSystem.bindTexture(texture)
        ShaderPassThrough["uTexture"] = 0
        ShaderPassThrough["uScale"] = 1f
        drawFullScreen()
        ShaderPassThrough.unbind()
    }

    private fun genTexture() {
        texture = glGenTextures()
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, texture)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexImage2D(
            GL_TEXTURE_2D,
            0,
            GL_RGBA32F,
            Schizoid.mc.window.framebufferWidth,
            Schizoid.mc.window.framebufferHeight,
            0,
            GL_RGBA,
            GL_FLOAT,
            0
        )
    }

    override fun init() {
        ClassLoader.getSystemResourceAsStream("$PATH/core/$shader/${shader}_CP.glsl")
            ?.use {
                val computeShader = compile(
                    GL_COMPUTE_SHADER,
                    it.readAllBytes().decodeToString().format(myGroupSizeX, myGroupSizeY, myGroupSizeZ)
                )
                glAttachShader(program, computeShader)
                glDeleteShader(computeShader)
            }

        if (texture == 0) {
            genTexture()
        }
        super.init()
    }

    init {
        on<EventWindowResize> {
            // delete old texture and create a new one
            if (texture != 0) {
                glDeleteTextures(texture)
            }
            genTexture()
        }
    }

    companion object {
        private val transparent = floatArrayOf(0f, 0f, 0f, 0f)
    }
}
