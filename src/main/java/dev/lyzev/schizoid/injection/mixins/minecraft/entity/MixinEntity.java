/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.injection.mixins.minecraft.entity;

import dev.lyzev.api.events.EventIsInvisibleTo;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * This class provides a mixin for the Entity class in the Minecraft entity package.
 * It modifies the behavior of the isInvisibleTo method of the Entity class.
 */
@Mixin(Entity.class)
public class MixinEntity {

    /**
     * This method is a redirect for the isInvisibleTo method of the Entity class.
     * It creates and fires an EventIsInvisibleTo event before the invisibility status is checked.
     * The invisibility status is then determined by the result of the event.
     *
     * @param instance The instance of the Entity whose invisibility status is being checked.
     * @return The invisibility status of the Entity, as determined by the EventIsInvisibleTo event.
     */
    @Redirect(method = "isInvisibleTo", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isInvisible()Z"))
    private boolean onIsInvisibleTo(Entity instance) {
        EventIsInvisibleTo event = new EventIsInvisibleTo(instance.isInvisible());
        event.fire();
        return event.isInvisible();
    }
}
