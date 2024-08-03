/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.math

import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Box
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import net.minecraft.world.RaycastContext
import kotlin.math.exp
import kotlin.math.sqrt

operator fun Vec3d.get(end: Vec3d): Vec2f {
    val delta = end.subtract(this)
    val yaw = MathHelper.atan2(delta.z, delta.x) * MathHelper.DEGREES_PER_RADIAN + 90f
    val pitch = -MathHelper.atan2(
        delta.y,
        sqrt(MathHelper.square(delta.x) + MathHelper.square(delta.z))
    ) * MathHelper.DEGREES_PER_RADIAN
    return Vec2f(pitch.toFloat(), yaw.toFloat())
}

operator fun Box.get(from: Vec3d): Vec3d {
    val x = MathHelper.clamp(from.x, minX, maxX)
    val y = MathHelper.clamp(from.y, minY, maxY)
    val z = MathHelper.clamp(from.z, minZ, maxZ)
    return Vec3d(x, y, z)
}

/**
 * 16384 is the squared distance of 128 blocks.
 * See [LivingEntity.canSee].
 */
fun Entity.canSee(vec: Vec3d): Boolean {
    if (world == null) {
        return false
    }
    val eye = eyePos
    return eye.squaredDistanceTo(vec) < 16384 && world.raycast(
        RaycastContext(
            eye,
            vec,
            RaycastContext.ShapeType.COLLIDER,
            RaycastContext.FluidHandling.NONE,
            this
        )
    ).type == HitResult.Type.MISS
}

fun relu(x: Double) = if (x > 0) x else 0.0

fun sigmoid(x: Double) = 1 / (1 + exp(-x))
