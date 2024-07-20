package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import java.awt.Color;
import java.util.List;
import java.util.OptionalLong;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.async.ItemSize;
import com.luneruniverse.minecraft.mod.nbteditor.containers.ContainerIO;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.NBTManagers;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.item.TooltipType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

@Mixin(ItemStack.class)
public class ItemStackMixin {
	@Inject(at = @At("RETURN"), method = "getTooltip")
	@Group(name = "getTooltip", min = 1)
	private void getTooltip(Item.TooltipContext context, PlayerEntity player, TooltipType type, CallbackInfoReturnable<List<Text>> info) {
		modifyTooltip(info.getReturnValue());
	}
	@Inject(at = @At("RETURN"), method = "method_7950(Lnet/minecraft/class_1657;Lnet/minecraft/class_1836;)Ljava/util/List;", remap = false)
	@Group(name = "getTooltip", min = 1)
	@SuppressWarnings("target")
	private void getTooltip(PlayerEntity player, TooltipType context, CallbackInfoReturnable<List<Text>> info) {
		modifyTooltip(info.getReturnValue());
	}
	private void modifyTooltip(List<Text> tooltip) {
		// Tooltips are requested for all items when GameJoinS2CPacket is received to setup the creative inventory's search
		// The world doesn't exist yet, so this causes the game to freeze when an exception from this mixin breaks everything
		if (MainUtil.client.world == null)
			return;
		
		ItemStack source = (ItemStack) (Object) this;
		
		if (NBTManagers.COMPONENTS_EXIST && source.contains(DataComponentTypes.HIDE_TOOLTIP))
			return;
		
		ConfigScreen.ItemSizeFormat sizeConfig = ConfigScreen.getItemSizeFormat();
		if (sizeConfig != ConfigScreen.ItemSizeFormat.HIDDEN) {
			OptionalLong loadingSize = ItemSize.getItemSize(source, sizeConfig.isCompressed());
			String displaySize;
			Formatting sizeFormat;
			if (loadingSize.isEmpty()) {
				displaySize = "...";
				sizeFormat = Formatting.GRAY;
			} else {
				long size = loadingSize.getAsLong();
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
			tooltip.add(TextInst.translatable("nbteditor.item_size." + (sizeConfig.isCompressed() ? "compressed" : "uncompressed"),
					TextInst.literal(displaySize).styled(style -> style.withColor(sizeColor))));
		}
		
		if (!ConfigScreen.isKeybindsHidden()) {
			// Checking slots in your hotbar vs item selection is difficult, so the lore is just disabled in non-inventory tabs
			boolean creativeInv = MVMisc.isCreativeInventoryTabSelected();
			
			if (creativeInv || (!(MainUtil.client.currentScreen instanceof CreativeInventoryScreen) &&
					NBTEditorClient.SERVER_CONN.isScreenEditable())) {
				tooltip.add(TextInst.translatable("nbteditor.keybind.edit"));
				tooltip.add(TextInst.translatable("nbteditor.keybind.factory"));
				if (ContainerIO.isContainer(source))
					tooltip.add(TextInst.translatable("nbteditor.keybind.container"));
				if (source.getItem() == Items.ENCHANTED_BOOK)
					tooltip.add(TextInst.translatable("nbteditor.keybind.enchant"));
			}
		}
	}
}
