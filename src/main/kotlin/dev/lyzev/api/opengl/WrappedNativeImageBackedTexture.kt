/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.opengl

import com.mojang.logging.LogUtils
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.texture.NativeImageBackedTexture

/**
 * A simple wrapper around the [NativeImageBackedTexture] class.
 *
 * @param image The [NativeImage] to wrap.
 * @see NativeImageBackedTexture
 */
class WrappedNativeImageBackedTexture(image: NativeImage) : NativeImageBackedTexture(image) {

    private val logger = LogUtils.getLogger()

    /**
     * Override upload method to upload the texture with blur = true to get [GlConst.GL_LINEAR] as the texture filter.
     */
    override fun upload() {
        if (image != null) {
            bindTexture()
            image!!.upload(0, 0, 0, 0, 0, image!!.width, image!!.height, true, false, false, false)
        } else {
            logger.warn("Trying to upload disposed texture {}", getGlId())
        }
    }
}
