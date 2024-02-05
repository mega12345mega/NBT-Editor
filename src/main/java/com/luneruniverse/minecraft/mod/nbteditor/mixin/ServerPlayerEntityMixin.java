package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import java.util.OptionalInt;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.packets.ContainerScreenS2CPacket;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.vehicle.VehicleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
	@Inject(method = "openHandledScreen", at = @At("HEAD"))
	private void openHandledScreen(NamedScreenHandlerFactory factory, CallbackInfoReturnable<OptionalInt> info) {
		if (factory instanceof LockableContainerBlockEntity || factory instanceof VehicleInventory)
			ServerPlayNetworking.send((ServerPlayerEntity) (Object) this, new ContainerScreenS2CPacket());
	}
	@Inject(method = "openHorseInventory", at = @At("HEAD"))
	private void openHorseInventory(AbstractHorseEntity horse, Inventory inventory, CallbackInfo info) {
		ServerPlayNetworking.send((ServerPlayerEntity) (Object) this, new ContainerScreenS2CPacket());
	}
}
