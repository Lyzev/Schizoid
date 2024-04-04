/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.opengl.shader

import com.mojang.blaze3d.systems.RenderSystem
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
import java.awt.Color
import java.io.FileNotFoundException

/**
 * Abstract class representing a Shader.
 * @param shader The name of the shader.
 */
abstract class Shader(shader: String) {

    private val program: Int = glCreateProgram()
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

    operator fun set(name: String, value: Color) = glUniform3f(this[name], value.red / 255f, value.green / 255f, value.blue / 255f)

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
    operator fun set(name: String, transpose: Boolean, value: Matrix4f) = glUniformMatrix4fv(this[name], transpose, value.get(buffer))

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
    private fun compile(type: Int, source: String): Int {
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

    /**
     * Initializes the shader program.
     */
    init {
        javaClass.classLoader.getResourceAsStream("$PATH/core/$shader/${shader}_VP.glsl")?.use {
                glAttachShader(program, compile(GL_VERTEX_SHADER, it.readAllBytes().decodeToString()))
            }

        javaClass.classLoader.getResourceAsStream("$PATH/core/$shader/${shader}_FP.glsl")?.use {
                glAttachShader(program, compile(GL_FRAGMENT_SHADER, it.readAllBytes().decodeToString()))
            }

        glLinkProgram(program)

        val isLinked = glGetProgrami(program, GL_LINK_STATUS)

        if (isLinked == 0)
            Schizoid.logger.error(glGetProgramInfoLog(program, glGetProgrami(program, GL_INFO_LOG_LENGTH)), IllegalStateException("Shader failed to link"))
    }

    companion object {

        /**
         * The path to the shaders.
         */
        private const val PATH = "assets/${Schizoid.MOD_ID}/shaders"

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
