/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.movement

import dev.lyzev.api.setting.settings.slider
import dev.lyzev.schizoid.feature.features.module.ModuleToggleable

object ModuleToggleableAirJump : ModuleToggleable("Air Jump", "Jump mid air.", category = Category.MOVEMENT) {

    val jumpCooldown by slider("Jump Cooldown", "The cooldown between jumps.", 5f, 0f, 10f, 0)
}
