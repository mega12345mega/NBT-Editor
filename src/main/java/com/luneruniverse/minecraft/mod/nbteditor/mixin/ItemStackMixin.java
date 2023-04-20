package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.containers.ContainerIO;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionRegistry;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ClientChestScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ItemsScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;

@Mixin(ItemStack.class)
public class ItemStackMixin {
	@Inject(at = @At("RETURN"), method = "getTooltip", cancellable = true)
	private void getTooltip(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> info) {
		ItemStack source = (ItemStack) (Object) this;
		
		if (ConfigScreen.isKeybindsHidden())
			return;
		
		// Checking slots in your hotbar vs item selection is difficult, so the lore is just disabled in non-inventory tabs
		boolean creativeInv = MultiVersionMisc.isCreativeInventoryTabSelected();
		
		if (creativeInv || MainUtil.client.currentScreen instanceof ClientChestScreen || MainUtil.client.currentScreen instanceof ItemsScreen) {
			info.getReturnValue().add(TextInst.translatable("nbteditor.keybind.edit"));
			info.getReturnValue().add(TextInst.translatable("nbteditor.keybind.item_factory"));
			if (ContainerIO.isContainer(source))
				info.getReturnValue().add(TextInst.translatable("nbteditor.keybind.container"));
			if (source.getItem() == Items.ENCHANTED_BOOK)
				info.getReturnValue().add(TextInst.translatable("nbteditor.keybind.enchant"));
		}
	}
	
	@Inject(at = @At("HEAD"), method = "appendEnchantments", cancellable = true)
	private static void appendEnchantments(List<Text> tooltip, NbtList enchantments, CallbackInfo info) {
		info.cancel();
		
		for (int i = 0; i < enchantments.size(); ++i) {
			NbtCompound nbtCompound = enchantments.getCompound(i);
			MultiVersionRegistry.ENCHANTMENT.getOrEmpty(EnchantmentHelper.getIdFromNbt(nbtCompound))
					.ifPresent(e -> tooltip.add(ConfigScreen.getEnchantNameWithMax(e, EnchantmentHelper.getLevelFromNbt(nbtCompound))));
		}
	}
}
