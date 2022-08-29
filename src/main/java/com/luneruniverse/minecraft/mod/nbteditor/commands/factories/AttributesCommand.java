package com.luneruniverse.minecraft.mod.nbteditor.commands.factories;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import java.util.UUID;

import com.luneruniverse.minecraft.mod.nbteditor.commands.SubCommand;
import com.luneruniverse.minecraft.mod.nbteditor.screens.AttributesScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;

public class AttributesCommand implements SubCommand {
	
	@Override
	public LiteralArgumentBuilder<FabricClientCommandSource> register(LiteralArgumentBuilder<FabricClientCommandSource> parent, CommandRegistryAccess cmdReg) {
		return parent.then(literal("attributes").then(literal("newuuids").executes(context -> {
			ItemReference ref = MainUtil.getHeldItem();
			ItemStack item = ref.getItem();
			NbtList attributes = item.getOrCreateNbt().getList("AttributeModifiers", NbtType.COMPOUND);
			if (attributes.isEmpty())
				MainUtil.client.player.sendMessage(Text.translatable("nbteditor.attributes.newuuids.none"));
			else {
				for (NbtElement attribute : attributes)
					((NbtCompound) attribute).putUuid("UUID", UUID.randomUUID());
				ref.saveItem(item, () -> MainUtil.client.player.sendMessage(Text.translatable("nbteditor.attributes.newuuids.success")));
			}
			return Command.SINGLE_SUCCESS;
		})).executes(context -> {
			MainUtil.client.setScreen(new AttributesScreen(MainUtil.getHeldItem()));
			return Command.SINGLE_SUCCESS;
		}));
	}
	
}
