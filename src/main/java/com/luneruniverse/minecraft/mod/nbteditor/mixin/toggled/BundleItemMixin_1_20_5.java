package com.luneruniverse.minecraft.mod.nbteditor.mixin.toggled;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.llamalad7.mixinextras.sugar.Local;
import com.luneruniverse.minecraft.mod.nbteditor.server.ServerMixinLink;

import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BundleItem;

@Mixin(BundleItem.class)
public class BundleItemMixin_1_20_5 {
	@ModifyVariable(method = {"onStackClicked", "onClicked"}, at = @At("STORE"), require = 2)
	private BundleContentsComponent.Builder newBundleContentsComponentBuilder(BundleContentsComponent.Builder builder, @Local PlayerEntity player) {
		if (ServerMixinLink.isNoSlotRestrictions(player, false))
			ServerMixinLink.NO_SLOT_RESTRICTIONS_BUNDLES.put(builder, true);
		return builder;
	}
}
