package com.luneruniverse.minecraft.mod.nbteditor.mixin.source;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

@Mixin(ItemStack.class)
public class ItemStackMixin {
	@Inject(at = @At("RETURN"), method = "getTooltip", cancellable = true)
	private void getTooltip(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> info) {
		ItemStack source = (ItemStack) (Object) this;
		
		if (MainUtil.client.currentScreen instanceof CreativeInventoryScreen &&
				((CreativeInventoryScreen) MainUtil.client.currentScreen).getSelectedTab() == ItemGroup.INVENTORY.getIndex() &&
				source.getItem() == Items.ENCHANTED_BOOK)
			info.getReturnValue().add(new TranslatableText("nbteditor.addenchant"));
	}
}
