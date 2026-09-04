package com.luneruniverse.minecraft.mod.nbteditor.mixin.toggled;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.luneruniverse.minecraft.mod.nbteditor.server.ServerMixinLink;

import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.item.Item;

@Mixin(BundleContentsComponent.Builder.class)
public class BundleContentsComponentBuilderMixin_1_20_5 {
	@Redirect(method = "method_57432(Lnet/minecraft/class_1799;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1792;method_31568()Z"), remap = false)
	@SuppressWarnings("target")
	private boolean add_canBeNested(Item item) {
		if (ServerMixinLink.NO_SLOT_RESTRICTIONS_BUNDLES.getOrDefault((BundleContentsComponent.Builder) (Object) this, false))
			return true;
		return item.canBeNested();
	}
}
