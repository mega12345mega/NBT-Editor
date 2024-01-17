package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import java.awt.Color;
import java.util.List;
import java.util.OptionalInt;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.async.ItemSize;
import com.luneruniverse.minecraft.mod.nbteditor.containers.ContainerIO;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistry;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.containers.ClientChestScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.containers.ContainerScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.Enchants;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

@Mixin(ItemStack.class)
public class ItemStackMixin {
	@Inject(at = @At("RETURN"), method = "getTooltip", cancellable = true)
	private void getTooltip(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> info) {
		ItemStack source = (ItemStack) (Object) this;
		
		ConfigScreen.ItemSizeFormat sizeConfig = ConfigScreen.getItemSizeFormat();
		if (sizeConfig != ConfigScreen.ItemSizeFormat.HIDDEN) {
			OptionalInt loadingSize = ItemSize.getItemSize(source, sizeConfig.isCompressed());
			String displaySize;
			Formatting sizeFormat;
			if (loadingSize.isEmpty()) {
				displaySize = "...";
				sizeFormat = Formatting.GRAY;
			} else {
				int size = loadingSize.getAsInt();
				int magnitude = sizeConfig.getMagnitude();
				if (magnitude == 0) {
					if (size < 1000)
						magnitude = 1;
					else if (size < 1000000)
						magnitude = 1000;
					else if (size < 1000000000)
						magnitude = 1000000;
					else
						magnitude = 1000000000;
				}
				if (magnitude == 1)
					displaySize = "" + size;
				else
					displaySize = String.format("%.1f", (double) size / magnitude);
				switch (magnitude) {
					case 1 -> {
						displaySize += "B";
						sizeFormat = Formatting.GREEN;
					}
					case 1000 -> {
						displaySize += "KB";
						sizeFormat = Formatting.YELLOW;
					}
					case 1000000 -> {
						displaySize += "MB";
						sizeFormat = Formatting.RED;
					}
					case 1000000000 -> {
						displaySize += "GB";
						sizeFormat = null;
					}
					default -> throw new IllegalStateException("Invalid magnitude!");
				}
			}
			TextColor sizeColor = (sizeFormat != null ? TextColor.fromFormatting(sizeFormat) :
				TextColor.fromRgb(Color.HSBtoRGB((System.currentTimeMillis() % 1000) / 1000.0f, 1, 1)));
			info.getReturnValue().add(TextInst.translatable("nbteditor.item_size." + (sizeConfig.isCompressed() ? "compressed" : "uncompressed"),
					TextInst.literal(displaySize).styled(style -> style.withColor(sizeColor))));
		}
		
		if (!ConfigScreen.isKeybindsHidden()) {
			// Checking slots in your hotbar vs item selection is difficult, so the lore is just disabled in non-inventory tabs
			boolean creativeInv = MVMisc.isCreativeInventoryTabSelected();
			
			if (creativeInv || MainUtil.client.currentScreen instanceof ClientChestScreen || MainUtil.client.currentScreen instanceof ContainerScreen) {
				info.getReturnValue().add(TextInst.translatable("nbteditor.keybind.edit"));
				info.getReturnValue().add(TextInst.translatable("nbteditor.keybind.item_factory"));
				if (ContainerIO.isContainer(source))
					info.getReturnValue().add(TextInst.translatable("nbteditor.keybind.container"));
				if (source.getItem() == Items.ENCHANTED_BOOK)
					info.getReturnValue().add(TextInst.translatable("nbteditor.keybind.enchant"));
			}
		}
	}
	
	@Inject(at = @At("HEAD"), method = "appendEnchantments", cancellable = true)
	private static void appendEnchantments(List<Text> tooltip, NbtList enchantments, CallbackInfo info) {
		if (Enchants.checkingCap)
			return;
		
		info.cancel();
		
		for (int i = 0; i < enchantments.size(); ++i) {
			NbtCompound nbtCompound = enchantments.getCompound(i);
			MVRegistry.ENCHANTMENT.getOrEmpty(Identifier.tryParse(nbtCompound.getString("id")))
					.ifPresent(e -> tooltip.add(ConfigScreen.getEnchantNameWithMax(e, Enchants.applyCap(nbtCompound.getInt("lvl")))));
		}
	}
}
