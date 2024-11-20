package com.luneruniverse.minecraft.mod.nbteditor.commands;

import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.argument;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.commands.arguments.ClientChestPageNameArgumentType;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.screens.containers.ClientChestScreen;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

public class ClientChestCommand extends ClientCommand {
	
	public static final ClientChestCommand INSTANCE = new ClientChestCommand();
	
	private ClientChestCommand() {
		
	}
	
	@Override
	public String getName() {
		return "clientchest";
	}
	
	@Override
	public String getExtremeAlias() {
		return "cc";
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder, String path) {
		builder.then(argument("page", IntegerArgumentType.integer(1)).executes(context -> {
			ClientChestScreen.PAGE = Math.min(context.getArgument("page", Integer.class), NBTEditorClient.CLIENT_CHEST.getPageCount()) - 1;
			ClientChestScreen.show();
			return Command.SINGLE_SUCCESS;
		})).then(argument("name", ClientChestPageNameArgumentType.pageName()).executes(context -> {
			Integer page = NBTEditorClient.CLIENT_CHEST.getPageFromName(context.getArgument("name", String.class));
			if (page == null || page >= NBTEditorClient.CLIENT_CHEST.getPageCount())
				throw new SimpleCommandExceptionType(TextInst.translatable("nbteditor.client_chest.name_not_found")).create();
			ClientChestScreen.PAGE = page;
			ClientChestScreen.show();
			return Command.SINGLE_SUCCESS;
		})).executes(context -> {
			ClientChestScreen.show();
			return Command.SINGLE_SUCCESS;
		});
	}
	
}
