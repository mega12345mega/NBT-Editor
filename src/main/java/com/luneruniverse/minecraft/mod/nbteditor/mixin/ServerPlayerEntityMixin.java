package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import java.util.OptionalInt;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVServerNetworking;
import com.luneruniverse.minecraft.mod.nbteditor.packets.ContainerScreenS2CPacket;
import com.luneruniverse.minecraft.mod.nbteditor.server.ServerMVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
	@Inject(method = "openHandledScreen", at = @At("HEAD"))
	private void openHandledScreen(NamedScreenHandlerFactory factory, CallbackInfoReturnable<OptionalInt> info) {
		if (factory instanceof LockableContainerBlockEntity ||
				MainUtil.getRootEnclosingClass(factory.getClass()) == ChestBlock.class || // Double chests
				ServerMVMisc.isInstanceOfVehicleInventory(factory))
			MVServerNetworking.send((ServerPlayerEntity) (Object) this, new ContainerScreenS2CPacket());
	}
	@ModifyVariable(method = "openHandledScreen", at = @At("STORE"), ordinal = 0)
	private ScreenHandler openHandledScreen_screenHandler(ScreenHandler screenHandler) {
		ServerPlayerEntity source = (ServerPlayerEntity) (Object) this;
		if (screenHandler instanceof GenericContainerScreenHandler generic && generic.getInventory() == source.getEnderChestInventory())
			MVServerNetworking.send(source, new ContainerScreenS2CPacket());
		return screenHandler;
	}
	@Inject(method = "openHorseInventory", at = @At("HEAD"))
	private void openHorseInventory(AbstractHorseEntity horse, Inventory inventory, CallbackInfo info) {
		MVServerNetworking.send((ServerPlayerEntity) (Object) this, new ContainerScreenS2CPacket());
	}
}
