package com.luneruniverse.minecraft.mod.nbteditor.commands.factories;

import java.util.Optional;
import java.util.UUID;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalBlock;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.BlockReference;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.NBTReference;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.NBTReferenceFilter;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.BlockTagReferences;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.ItemTagReferences;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Formatting;

public class UnbindSkullCommand extends ClientCommand {
	
	public static final NBTReferenceFilter SKULL_FILTER = NBTReferenceFilter.create(
			ref -> ref.getItem().getItem() == Items.PLAYER_HEAD,
			ref -> ref.getBlock() == Blocks.PLAYER_HEAD || ref.getBlock() == Blocks.PLAYER_WALL_HEAD,
			null,
			TextInst.translatable("nbteditor.no_ref.skull"),
			TextInst.translatable("nbteditor.no_hand.no_item.skull"));
	
	@Override
	public String getName() {
		return "unbindskull";
	}
	
	@Override
	public String getExtremeAlias() {
		return null;
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder, String path) {
		builder.executes(context -> {
			NBTReference.getReference(SKULL_FILTER, false, ref -> {
				Optional<GameProfile> profile = (ref instanceof ItemReference itemRef ?
						ItemTagReferences.PROFILE.get(itemRef.getItem()) :
						BlockTagReferences.PROFILE.get((LocalBlock) ref.getLocalNBT()));
				if (profile.isEmpty() || profile.get().getProperties().isEmpty()) {
					MainUtil.client.player.sendMessage(TextInst.translatable("nbteditor.unbind_skull.no_textures"), false);
					return;
				}
				GameProfile newProfile = new GameProfile(new UUID(0L, 0L), "Unbound Player");
				newProfile.getProperties().putAll(profile.get().getProperties());
				if (ref instanceof ItemReference itemRef) {
					ItemStack item = itemRef.getItem();
					ItemTagReferences.PROFILE.set(item, Optional.of(newProfile));
					if (!item.manager$hasCustomName()) {
						item.manager$setCustomName(TextInst.translatable("block.minecraft.player_head.named", profile.get().getName())
								.styled(style -> style.withItalic(false).withColor(Formatting.YELLOW)));
					}
					itemRef.saveItem(item, TextInst.translatable("nbteditor.unbind_skull.unbound"));
				} else {
					BlockReference blockRef = (BlockReference) ref;
					LocalBlock block = blockRef.getLocalNBT();
					BlockTagReferences.PROFILE.set(block, Optional.of(newProfile));
					blockRef.saveLocalNBT(block, TextInst.translatable("nbteditor.unbind_skull.unbound"));
				}
			});
			return Command.SINGLE_SUCCESS;
		});
	}
	
}
