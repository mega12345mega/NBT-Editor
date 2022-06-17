package com.luneruniverse.minecraft.mod.nbteditor.mixin.source;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.screens.ClientChestScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ItemsScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.registry.Registry;

@Mixin(ItemStack.class)
public class ItemStackMixin {
	@Inject(at = @At("RETURN"), method = "getTooltip", cancellable = true)
	private void getTooltip(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> info) {
		ItemStack source = (ItemStack) (Object) this;
		
		if (ConfigScreen.shouldHideKeybinds())
			return;
		
		// Checking slots in your hotbar vs item selection is difficult, so the lore is just disabled in non-inventory tabs
		boolean creativeInv = MainUtil.client.currentScreen instanceof CreativeInventoryScreen &&
				((CreativeInventoryScreen) MainUtil.client.currentScreen).getSelectedTab() == ItemGroup.INVENTORY.getIndex();
		if (creativeInv && source.getItem() == Items.ENCHANTED_BOOK)
			info.getReturnValue().add(Text.translatable("nbteditor.addenchant"));
		
		if (creativeInv || MainUtil.client.currentScreen instanceof ClientChestScreen || MainUtil.client.currentScreen instanceof ItemsScreen) {
			info.getReturnValue().add(Text.translatable("nbteditor.keybind_edit"));
			if (ItemsScreen.isContainer(source))
				info.getReturnValue().add(Text.translatable("nbteditor.keybind_container"));
		}
	}
	
	@Inject(at = @At("HEAD"), method = "appendEnchantments", cancellable = true)
	private static void appendEnchantments(List<Text> tooltip, NbtList enchantments, CallbackInfo info) {
		info.cancel();
		
		for (int i = 0; i < enchantments.size(); ++i) {
			NbtCompound nbtCompound = enchantments.getCompound(i);
			Registry.ENCHANTMENT.getOrEmpty(EnchantmentHelper.getIdFromNbt(nbtCompound))
					.ifPresent(e -> {
						int level = EnchantmentHelper.getLevelFromNbt(nbtCompound);
						Text text = ConfigScreen.getEnchantName(e, level);
						if (ConfigScreen.getMaxEnchantLevelDisplay().shouldShowMax(level, e.getMaxLevel())) {
							text = text.copy().append("/").append(
									ConfigScreen.isUseArabicEnchantLevels() ?
											Text.of("" + e.getMaxLevel()) :
											Text.translatable("enchantment.level." + e.getMaxLevel()));
						}
						tooltip.add(text);
					});
		}
	}
}
