/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.opengl.shader

import com.mojang.blaze3d.systems.RenderSystem
import dev.lyzev.schizoid.Schizoid
import net.minecraft.client.render.BufferRenderer
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.opengl.GL20.*
import java.io.FileNotFoundException

abstract class Shader(shader: String) {

    private val program: Int = glCreateProgram()
    private val uniforms = HashMap<String, Int>()

    fun bind() = glUseProgram(program)

    fun unbind() = glUseProgram(0)

    operator fun set(name: String, value: Float) = glUniform1f(this[name], value)

    operator fun set(name: String, value: Int) = glUniform1i(this[name], value)

    operator fun set(name: String, value: IntArray) = glUniform1iv(this[name], value)

    operator fun set(name: String, value: Boolean) = glUniform1i(this[name], if (value) 1 else 0)

    operator fun set(name: String, value: Vector2f) = glUniform2f(this[name], value.x, value.y)

    operator fun set(name: String, value: Vector3f) = glUniform3f(this[name], value.x, value.y, value.z)

    operator fun set(name: String, value: Vector4f) = glUniform4f(this[name], value.x, value.y, value.z, value.w)

    operator fun set(name: String, value: FloatArray) = glUniform1fv(this[name], value)

    private operator fun get(name: String): Int = uniforms.getOrPut(name) { glGetUniformLocation(program, name) }

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

        private const val PATH = "assets/${Schizoid.MOD_ID}/shaders"

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
