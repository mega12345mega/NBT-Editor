package com.luneruniverse.minecraft.mod.nbteditor.commands.factories;

import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.literal;

import java.util.UUID;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.screens.AttributesScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

public class AttributesCommand extends ClientCommand {
	
	@Override
	public String getName() {
		return "attributes";
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
		builder.then(literal("newuuids").executes(context -> {
			ItemReference ref = MainUtil.getHeldItem();
			ItemStack item = ref.getItem();
			NbtList attributes = item.getOrCreateNbt().getList("AttributeModifiers", NbtElement.COMPOUND_TYPE);
			if (attributes.isEmpty())
				MainUtil.client.player.sendMessage(TextInst.translatable("nbteditor.attributes.new_uuids.no_attributes"), false);
			else {
				for (NbtElement attribute : attributes)
					((NbtCompound) attribute).putUuid("UUID", UUID.randomUUID());
				ref.saveItem(item, () -> MainUtil.client.player.sendMessage(TextInst.translatable("nbteditor.attributes.new_uuids.success"), false));
			}
			return Command.SINGLE_SUCCESS;
		})).executes(context -> {
			MainUtil.client.setScreen(new AttributesScreen(MainUtil.getHeldItem()));
			return Command.SINGLE_SUCCESS;
		});
	}
	
}
