/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.combat

import dev.lyzev.api.events.EventListener
import dev.lyzev.api.events.EventUpdateCrosshairTargetTick
import dev.lyzev.api.events.on
import dev.lyzev.api.math.Clicker
import dev.lyzev.api.setting.settings.slider
import dev.lyzev.api.setting.settings.switch
import dev.lyzev.api.settings.Setting.Companion.neq
import dev.lyzev.schizoid.feature.IFeature
import dev.lyzev.schizoid.feature.features.module.ModuleToggleable
import net.minecraft.client.option.KeyBinding
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.projectile.ProjectileUtil
import net.minecraft.util.math.MathHelper

object ModuleToggleableTriggerBot : ModuleToggleable(
    "Trigger Bot", "Automatically attacks entities in the player's crosshair.", category = IFeature.Category.COMBAT
), EventListener {

    private var lastHit = 0L

    val clicker = Clicker(this)
    val miss by switch("Miss", "Hits in the air to simulate a miss.", false)
    val reachExtension by slider(
        "Reach Extension",
        "The distance the bot hits the air before the entity.",
        1.8f,
        0f,
        5f,
        1,
        "blocks",
        hide = ::miss neq true
    )
    val reactionTime by slider(
        "Reaction Time",
        "The time the bot hits the air after the entity is in the crosshair.",
        200,
        0,
        1000,
        "ms",
        hide = ::miss neq true
    )

    private fun click(amount: Int) {
        mc.options.attackKey.timesPressed += amount
    }

    override val shouldHandleEvents
        get() = isEnabled && mc.player != null && mc.crosshairTarget != null

    init {
        // Attack the entity in the player's crosshair.
        on<EventUpdateCrosshairTargetTick> {
            if (mc.targetedEntity is LivingEntity) {
                lastHit = System.currentTimeMillis()
            }
            if (mc.targetedEntity != null) {
                val click =
                    clicker.tick(mc.targetedEntity is LivingEntity && (mc.targetedEntity as LivingEntity).hurtTime == 0)
                if (click > 0) {
                    click(click)
                }
            } else if (miss) {
                if (System.currentTimeMillis() - lastHit <= reactionTime) {
                    val click =
                        clicker.tick(false)
                    if (click > 0) {
                        click(click)
                    }
                } else if (mc.cameraEntity != null) {
                    val reach = mc.player!!.entityInteractionRange + reachExtension
                    val camVec = mc.cameraEntity!!.getCameraPosVec(1f)
                    val lookVec = mc.cameraEntity!!.getRotationVec(1f)
                    val hitVec = lookVec.multiply(reach).add(camVec)
                    val box = mc.cameraEntity!!.boundingBox.stretch(lookVec.multiply(reach)).expand(1.0, 1.0, 1.0)
                    val entityHitResult = ProjectileUtil.raycast(
                        mc.cameraEntity,
                        camVec,
                        hitVec,
                        box,
                        { entity: Entity -> !entity.isSpectator && entity.canHit() },
                        MathHelper.square(reach)
                    )
                    if (entityHitResult != null) {
                        val click =
                            clicker.tick(false)
                        if (click > 0) {
                            click(click)
                        }
                    } else {
                        clicker.regenerate()
                    }
                }
            } else {
                clicker.regenerate()
            }
        }
    }
}
