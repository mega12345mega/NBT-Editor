package com.luneruniverse.minecraft.mod.nbteditor.mixin.toggled;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.server.ServerMVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.server.ServerMixinLink;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.HorseScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

@Mixin(targets = {"net.minecraft.screen.HorseScreenHandler$2"})
public class HorseScreenHandler2Mixin_1_21_4 {
	@Inject(method = "<init>(Lnet/minecraft/class_1724;Lnet/minecraft/class_1263;Lnet/minecraft/class_1309;Lnet/minecraft/class_1304;IIILnet/minecraft/class_2960;Lnet/minecraft/class_1496;)V", at = @At("RETURN"), remap = false, require = 0)
	@SuppressWarnings("target")
	private void init(HorseScreenHandler handler, Inventory inventory, LivingEntity entity, EquipmentSlot slot, int index, int x, int y, Identifier backgroundSprite, AbstractHorseEntity horse, CallbackInfo info) {
		PlayerEntity owner = ServerMixinLink.SCREEN_HANDLER_OWNER.get(Thread.currentThread());
		if (owner == null)
			return;
		ServerMixinLink.SLOT_OWNER.put((Slot) (Object) this, owner);
	}
	@Inject(method = "method_7680(Lnet/minecraft/class_1799;)Z", at = @At("HEAD"), cancellable = true, remap = false)
	@SuppressWarnings("target")
	private void canInsert(ItemStack item, CallbackInfoReturnable<Boolean> info) {
		PlayerEntity owner = ServerMixinLink.SLOT_OWNER.get((Slot) (Object) this);
		if (owner == null)
			return;
		if (!((Slot) (Object) this).isEnabled()) {
			info.setReturnValue(false);
			return;
		}
		if (owner instanceof ServerPlayerEntity) {
			if (ServerMVMisc.hasPermissionLevel(owner, 2) && ServerMixinLink.NO_SLOT_RESTRICTIONS_PLAYERS.getOrDefault(owner, false))
				info.setReturnValue(true);
		} else {
			if (NBTEditorClient.SERVER_CONN.isEditingExpanded() && ConfigScreen.isNoSlotRestrictions())
				info.setReturnValue(true);
		}
	}
}
