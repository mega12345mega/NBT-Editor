package com.luneruniverse.minecraft.mod.nbteditor.mixin.toggled;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.server.ServerMVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.server.ServerMixinLink;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;

// Same as ArmorSlotMixin
@Mixin(targets = "net.minecraft.screen.PlayerScreenHandler$1")
public class PlayerScreenHandler1Mixin {
	@Inject(method = "method_7680(Lnet/minecraft/class_1799;)Z", at = @At("HEAD"), cancellable = true)
	@SuppressWarnings("target")
	private void canInsert(ItemStack stack, CallbackInfoReturnable<Boolean> info) {
		PlayerEntity owner = ((PlayerInventory) ((Slot) (Object) this).inventory).player;
		if (owner instanceof ServerPlayerEntity) {
			if (ServerMVMisc.hasPermissionLevel(owner, 2) && ServerMixinLink.NO_SLOT_RESTRICTIONS_PLAYERS.getOrDefault(owner, false))
				info.setReturnValue(true);
		} else {
			if (NBTEditorClient.SERVER_CONN.isEditingAllowed() && ConfigScreen.isNoSlotRestrictions())
				info.setReturnValue(true);
		}
	}
	@Inject(method = "method_7674(Lnet/minecraft/class_1657;)Z", at = @At("HEAD"), cancellable = true)
	@SuppressWarnings("target")
	private void canTakeItems(PlayerEntity player, CallbackInfoReturnable<Boolean> info) {
		if (player instanceof ServerPlayerEntity) {
			if (ServerMVMisc.hasPermissionLevel(player, 2) && ServerMixinLink.NO_SLOT_RESTRICTIONS_PLAYERS.getOrDefault(player, false))
				info.setReturnValue(true);
		} else {
			if (NBTEditorClient.SERVER_CONN.isEditingAllowed() && ConfigScreen.isNoSlotRestrictions())
				info.setReturnValue(true);
		}
	}
}
