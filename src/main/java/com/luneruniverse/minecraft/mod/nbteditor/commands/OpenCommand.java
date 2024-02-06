package com.luneruniverse.minecraft.mod.nbteditor.commands;

import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.literal;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.containers.ContainerIO;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.packets.OpenEnderChestC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.screens.containers.ContainerScreen;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class OpenCommand extends ClientCommand {
	
	public static final OpenCommand INSTANCE = new OpenCommand();
	
	private OpenCommand() {
		
	}
	
	@Override
	public String getName() {
		return "open";
	}
	
	@Override
	public String getExtremeAlias() {
		return "o";
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder, String path) {
		builder.then(literal("echest").executes(context -> {
			if (NBTEditorClient.SERVER_CONN.isEditingExpanded())
				ClientPlayNetworking.send(new OpenEnderChestC2SPacket());
			else
				throw new SimpleCommandExceptionType(TextInst.translatable("nbteditor.requires_server")).create();
			return Command.SINGLE_SUCCESS;
		})).executes(context -> {
			ContainerScreen.show(ItemReference.getHeldItem(ContainerIO::isContainer, TextInst.translatable("nbteditor.no_hand.no_item.container")));
			return Command.SINGLE_SUCCESS;
		});
	}
	
}
