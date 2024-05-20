package com.luneruniverse.minecraft.mod.nbteditor.commands.factories;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistry;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.NBTReference;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.NBTReferenceFilter;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Formatting;

public class UnbindSkullCommand extends ClientCommand {
	
	public static final NBTReferenceFilter SKULL_FILTER = NBTReferenceFilter.create(
			ref -> ref.getItem().getItem() == Items.PLAYER_HEAD,
			ref -> {
				Block block = MVRegistry.BLOCK.get(ref.getId());
				return block == Blocks.PLAYER_HEAD || block == Blocks.PLAYER_WALL_HEAD;
			},
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
				NbtCompound nbt = ref.getNBT();
				if (nbt.contains("SkullOwner", NbtElement.COMPOUND_TYPE)) {
					NbtCompound owner = nbt.getCompound("SkullOwner");
					owner.putIntArray("Id", new int[] {0, 0, 0, 0});
					String name = owner.getString("Name");
					owner.putString("Name", "Unbound Player");
					if (ref instanceof ItemReference itemRef) {
						ItemStack item = itemRef.getItem();
						item.setNbt(nbt);
						if (!item.hasCustomName()) {
							item.setCustomName(TextInst.translatable("block.minecraft.player_head.named", name)
									.styled(style -> style.withItalic(false).withColor(Formatting.YELLOW)));
						}
					}
				} else {
					MainUtil.client.player.sendMessage(TextInst.translatable("nbteditor.unbind_skull.no_textures"), false);
					return;
				}
				
				ref.saveNBT(ref.getId(), nbt, TextInst.translatable("nbteditor.unbind_skull.unbound"));
			});
			return Command.SINGLE_SUCCESS;
		});
	}
	
}
