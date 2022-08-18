package com.luneruniverse.minecraft.mod.nbteditor.commands;

import com.luneruniverse.minecraft.mod.nbteditor.commands.factories.FactoryCommand;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

public class CommandHandler {
	
	public static void registerCommands() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, cmdReg) -> {
			new NBTCommand().registerAll(dispatcher, cmdReg);
			new ClientChestCommand().registerAll(dispatcher, cmdReg);
			new ItemsCommand().registerAll(dispatcher, cmdReg);
			new SignatureCommand().registerAll(dispatcher, cmdReg);
			new GetCommand().registerAll(dispatcher, cmdReg);
			new FactoryCommand().registerAll(dispatcher, cmdReg);
		});
	}
	
}
