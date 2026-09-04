package com.luneruniverse.minecraft.mod.nbteditor.mixin.toggled;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.luneruniverse.minecraft.mod.nbteditor.server.ServerMixinLink;

import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.item.ItemStack;

@Mixin(BundleContentsComponent.Builder.class)
public class BundleContentsComponentBuilderMixin_1_21_2 {
	@Redirect(method = {"add(Lnet/minecraft/item/ItemStack;)I", "add(Lnet/minecraft/screen/slot/Slot;Lnet/minecraft/entity/player/PlayerEntity;)I"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/component/type/BundleContentsComponent;canBeBundled(Lnet/minecraft/item/ItemStack;)Z"), require = 2)
	private boolean add_canBeBundled(ItemStack item) {
		if (!item.isEmpty() && ServerMixinLink.NO_SLOT_RESTRICTIONS_BUNDLES
				.getOrDefault((BundleContentsComponent.Builder) (Object) this, false)) {
			return true;
		}
		return BundleContentsComponent.canBeBundled(item);
	}
}
