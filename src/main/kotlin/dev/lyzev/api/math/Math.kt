/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.math

import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import kotlin.math.atan2
import kotlin.math.sqrt

operator fun Vec3d.get(end: Vec3d): Vec2f {
    val delta = end.subtract(this)
    val yaw = atan2(delta.z, delta.x) * (180 / Math.PI) + 90
    val pitch = atan2(delta.y, sqrt(delta.x * delta.x + delta.z * delta.z)) * (180 / Math.PI)
    return Vec2f(pitch.toFloat(), yaw.toFloat())
}
