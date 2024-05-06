package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;

@Mixin(BuiltinModelItemRenderer.class)
public class BuiltinModelItemRendererMixin {
	private ItemStack item;
	@ModifyVariable(method = "render", at = @At("HEAD"), ordinal = 0)
	private ItemStack render_ItemStack(ItemStack item) {
		this.item = item;
		return item;
	}
	@ModifyVariable(method = "render", at = @At("HEAD"), ordinal = 0)
	private VertexConsumerProvider render_VertexConsumerProvider(VertexConsumerProvider provider) {
		if (!(item.getItem() instanceof BlockItem) ||
				(!MixinLink.ENCHANT_GLINT_FIX.contains(item) && !ConfigScreen.isEnchantGlintFix()))
			return provider;
		return (layer) -> ItemRenderer.getDirectItemGlintConsumer(provider, layer, true, item.hasGlint());
	}
}
