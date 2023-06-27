package com.luneruniverse.minecraft.mod.nbteditor.commands.factories;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Formatting;

public class UnbindSkullCommand extends ClientCommand {
	
	@Override
	public String getName() {
		return "unbindskull";
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
		builder.executes(context -> {
			ItemReference ref = ItemReference.getHeldItem(item -> item.getItem() == Items.PLAYER_HEAD, TextInst.translatable("nbteditor.no_hand.no_item.skull"));
			ItemStack item = ref.getItem();
			
			NbtCompound nbt = item.getOrCreateNbt();
			if (nbt.contains("SkullOwner", NbtElement.COMPOUND_TYPE)) {
				NbtCompound owner = nbt.getCompound("SkullOwner");
				owner.putIntArray("Id", new int[] {0, 0, 0, 0});
				String name = owner.getString("Name");
				owner.putString("Name", "Unbound Player");
				if (!item.hasCustomName())
					item.setCustomName(TextInst.literal(name).formatted(Formatting.YELLOW));
			} else
				throw new SimpleCommandExceptionType(TextInst.translatable("nbteditor.unbind_skull.no_textures")).create();
			
			ref.saveItem(item, TextInst.translatable("nbteditor.unbind_skull.unbound"));
			return Command.SINGLE_SUCCESS;
		});
	}
	
}
