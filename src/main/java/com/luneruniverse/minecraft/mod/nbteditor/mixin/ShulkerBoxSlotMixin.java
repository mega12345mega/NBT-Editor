package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.server.ServerMixinLink;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.ShulkerBoxSlot;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(ShulkerBoxSlot.class)
public class ShulkerBoxSlotMixin {
	@Inject(method = "<init>", at = @At("RETURN"))
	private void init(Inventory inventory, int index, int x, int y, CallbackInfo info) {
		PlayerEntity owner = ServerMixinLink.SCREEN_HANDLER_OWNER.get(Thread.currentThread());
		if (owner == null)
			return;
		ServerMixinLink.SLOT_OWNER.put((ShulkerBoxSlot) (Object) this, owner);
	}
	@Inject(method = "canInsert(Lnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
	private void canInsert(ItemStack item, CallbackInfoReturnable<Boolean> info) {
		PlayerEntity owner = ServerMixinLink.SLOT_OWNER.get((ShulkerBoxSlot) (Object) this);
		if (owner == null)
			return;
		if (owner instanceof ServerPlayerEntity) {
			if (owner.hasPermissionLevel(2))
				info.setReturnValue(true);
		} else {
			if (NBTEditorClient.SERVER_CONN.isEditingExpanded())
				info.setReturnValue(true);
		}
	}
}
