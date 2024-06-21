package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
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
	@Group(name = "render_ItemStack", min = 1)
	private ItemStack render_ItemStack(ItemStack item) {
		this.item = item;
		return item;
	}
	@ModifyVariable(method = "method_3166(Lnet/minecraft/class_1799;Lnet/minecraft/class_809$class_811;Lnet/minecraft/class_4587;Lnet/minecraft/class_4597;II)V", at = @At("HEAD"), ordinal = 0, remap = false)
	@Group(name = "render_ItemStack", min = 1)
	@SuppressWarnings("target")
	private ItemStack render_ItemStack_old(ItemStack item) {
		this.item = item;
		return item;
	}
	
	@ModifyVariable(method = "render", at = @At("HEAD"), ordinal = 0)
	@Group(name = "render_VertexConsumerProvider", min = 1)
	private VertexConsumerProvider render_VertexConsumerProvider(VertexConsumerProvider provider) {
		return render_VertexConsumerProvider_impl(provider);
	}
	@ModifyVariable(method = "method_3166(Lnet/minecraft/class_1799;Lnet/minecraft/class_809$class_811;Lnet/minecraft/class_4587;Lnet/minecraft/class_4597;II)V", at = @At("HEAD"), ordinal = 0, remap = false)
	@Group(name = "render_VertexConsumerProvider", min = 1)
	@SuppressWarnings("target")
	private VertexConsumerProvider render_VertexConsumerProvider_old(VertexConsumerProvider provider) {
		return render_VertexConsumerProvider_impl(provider);
	}
	
	private VertexConsumerProvider render_VertexConsumerProvider_impl(VertexConsumerProvider provider) {
		if (!(item.getItem() instanceof BlockItem) ||
				(!MixinLink.ENCHANT_GLINT_FIX.contains(item) && !ConfigScreen.isEnchantGlintFix()))
			return provider;
		return (layer) -> ItemRenderer.getDirectItemGlintConsumer(provider, layer, true, item.hasGlint());
	}
}
