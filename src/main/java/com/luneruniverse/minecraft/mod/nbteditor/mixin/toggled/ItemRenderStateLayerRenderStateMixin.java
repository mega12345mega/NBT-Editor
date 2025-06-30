package com.luneruniverse.minecraft.mod.nbteditor.mixin.toggled;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.luneruniverse.minecraft.mod.nbteditor.MC_1_17_Link.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.MC_1_17_Link.MixinLink;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.ItemRenderState.Glint;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;

@Mixin(ItemRenderState.LayerRenderState.class)
public class ItemRenderStateLayerRenderStateMixin {
	
	@Shadow
	private ItemRenderState.Glint glint;
	
	@ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/model/special/SpecialModelRenderer;render(Ljava/lang/Object;Lnet/minecraft/item/ModelTransformationMode;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IIZ)V"))
	private VertexConsumerProvider render(VertexConsumerProvider provider) {
		ItemStack item = com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink.ITEM_BEING_RENDERED.remove(Thread.currentThread());
		if (item == null)
			return provider;
		
		if (!(item.getItem() instanceof BlockItem) ||
				(!MixinLink.ENCHANT_GLINT_FIX.contains(item) && !ConfigScreen.isEnchantGlintFix()))
			return provider;
		return layer -> ItemRenderer.getItemGlintConsumer(provider, layer, true, glint != Glint.NONE);
	}
	
}
