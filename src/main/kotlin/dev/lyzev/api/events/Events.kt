/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.events

import dev.lyzev.api.setting.settings.OptionEnum
import dev.lyzev.schizoid.Schizoid
import net.minecraft.block.Block
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.entity.model.EntityModel
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.packet.Packet

/**
 * This event is triggered during the startup phase of the application.
 */
object EventStartup : Event

/**
 * This event is triggered during the shutdown phase of the application.
 */
object EventShutdown : Event {

    override fun fire() {
        Schizoid.logger.info("Shutting down the client...")
        super.fire()
    }
}

/**
 * This event is triggered when GLFW is initialized.
 */
class EventGlfwInit(val handle: Long) : Event

/**
 * This event is triggered when ImGui starts rendering.
 */
object EventSwapBuffers : Event

/**
 * This event is triggered when ImGui is rendering its content.
 */
object EventRenderImGuiContent : Event

/**
 * This event is triggered when a packet is sent.
 */
class EventPacket(val packet: Packet<*>, val type: Type) : CancellableEvent() {

    enum class Type(override val key: String) : OptionEnum {
        S2C("S2C"),
        C2S("C2S")
    }
}

/**
 * This event is triggered when a key is pressed.
 */
class EventKeystroke(val window: Long, val key: Int, val scancode: Int, val action: Int, val modifiers: Int) : Event

/**
 * This event is triggered when a mouse button is clicked.
 */
class EventMouseClick(val window: Long, val button: Int, val action: Int, val mods: Int) : Event

/**
 * This event is triggered when [dev.lyzev.api.setting.settings.SettingClientKeybinds] is requesting a keybind.
 */
object EventKeybindsRequest : Event

/**
 * This event is triggered when [dev.lyzev.schizoid.feature.features.gui.guis.ImGuiScreenFeature] is responding with a keybind.
 */
class EventKeybindsResponse(val key: Int) : Event

/**
 * This event is triggered when the window is resized.
 */
object EventWindowResize : Event

/**
 * This event is triggered when the player ticks.
 */
class EventClientPlayerEntityTick(val player: ClientPlayerEntity) : Event

/**
 * This event is triggered when the world is rendered.
 */
class EventRenderWorld(val tickDelta: Float, val limitTime: Long, val matrices: MatrixStack) : Event

/**
 * This event is triggered when [net.minecraft.client.render.LightmapTextureManager] updates.
 */
class EventGamma(var gamma: Float) : Event

/**
 * This event is triggered when the camera clips to space.
 */
class EventClipToSpace(var desiredCameraDistance: Double) : Event

/**
 * This event is triggered when [net.minecraft.entity.Entity.isInvisibleTo] is called.
 */
class EventIsInvisibleTo(var isInvisible: Boolean) : Event

/**
 * This event is triggered when an entity is rendered.
 */
class EventRenderModel(
    val instance: EntityModel<*>,
    val matrixStack: MatrixStack,
    val vertexConsumer: VertexConsumer,
    var light: Int,
    var overlay: Int,
    var red: Float,
    var green: Float,
    var blue: Float,
    var alpha: Float
) : Event

/**
 * This event is triggered when [net.minecraft.client.world.ClientWorld.getBlockParticle] is called.
 */
class EventBlockParticle(var block: Block?) : Event

/**
 * This event is triggered when [LivingEntity.hasStatusEffect] is called.
 */
class EventHasStatusEffect(val entity: LivingEntity, val effect: StatusEffect, var hasStatusEffect: Boolean) : Event

/**
 * This event is triggered when [net.minecraft.client.MinecraftClient.doItemUse] is called.
 */
class EventItemUse(var itemUseCooldown: Int) : Event

/**
 * This event is triggered when [net.minecraft.client.input.KeyboardInput.tick] or [ClientPlayerEntity.tickMovement] calls [KeyBinding.isPressed].
 */
class EventIsMovementKeyPressed(val keyBinding: KeyBinding) : Event {
    var isPressed = keyBinding.isPressed
}

class EventIsCursorLocked(var isCursorLocked: Boolean) : Event

object EventUpdateMouse : Event

class EventAttackEntity(val player: PlayerEntity, val entity: Entity) : Event
