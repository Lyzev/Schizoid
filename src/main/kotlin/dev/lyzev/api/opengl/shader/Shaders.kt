/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.opengl.shader

import org.joml.Vector2f
import org.joml.Vector3f

object ShaderDualKawaseDown : ShaderDualKawase("Down")
object ShaderDualKawaseUp : ShaderDualKawase("Up")

abstract class ShaderDualKawase(shader: String) : Shader("DualKawase$shader") {

    fun setUniforms(offset: Float, halfPixelSize: Vector2f, alpha: Boolean) {
        this["uTexture"] = 0
        this["uHalfTexelSize"] = halfPixelSize
        this["uAlpha"] = alpha
        this["uOffset"] = offset
    }
}

object ShaderKawase : Shader("Kawase") {

    fun setUniforms(pixelSize: Vector2f, size: Float, alpha: Boolean) {
        this["uTexture"] = 0
        this["uTexelSize"] = pixelSize
        this["uAlpha"] = alpha
        this["uSize"] = size
    }
}

object ShaderBox : Shader("Box") {

    fun setUniforms(direction: Vector2f, pixelSize: Vector2f, alpha: Boolean, size: Int) {
        this["uTexture"] = 0
        this["uDirection"] = direction
        this["uTexelSize"] = pixelSize
        this["uAlpha"] = alpha
        this["uSize"] = size
    }
}

object ShaderGaussian : Shader("Gaussian") {

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

object ShaderAcrylic : Shader("Acrylic")
object ShaderTint : Shader("Tint")

object ShaderMask : Shader("Mask")
object ShaderAdd : Shader("Add")
object ShaderPassThrough : Shader("PassThrough")

object ShaderDepth : Shader("Depth")

object ShaderThreshold : Shader("Threshold")
object ShaderBlend : Shader("Blend")
object ShaderFlip : Shader("Flip")
