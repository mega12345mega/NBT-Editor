package com.luneruniverse.minecraft.mod.nbteditor.mixin.toggled;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.llamalad7.mixinextras.sugar.Local;
import com.luneruniverse.minecraft.mod.nbteditor.server.ServerMixinLink;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BundleItem;
import net.minecraft.item.Item;

@Mixin(BundleItem.class)
public class BundleItemMixin_1_21_1 {
	@Redirect(method = "method_31565(Lnet/minecraft/class_1799;Lnet/minecraft/class_1735;Lnet/minecraft/class_5536;Lnet/minecraft/class_1657;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1792;method_31568()Z"), remap = false)
	private boolean onStackClicked_canBeNested(Item item, @Local PlayerEntity player) {
		if (ServerMixinLink.isNoSlotRestrictions(player, false))
			return true;
		return item.canBeNested();
	}
}
