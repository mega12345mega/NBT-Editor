package com.luneruniverse.minecraft.mod.nbteditor.commands;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

public class CommandHandler {
	
	public static void registerCommands() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, cmdReg) -> {
			new NBTCommand().registerAll(dispatcher, cmdReg);
			new ClientChestCommand().registerAll(dispatcher, cmdReg);
			new ItemsCommand().registerAll(dispatcher, cmdReg);
			new HideFlagsCommand().registerAll(dispatcher, cmdReg);
			new SignatureCommand().registerAll(dispatcher, cmdReg);
			new LoreCommand().registerAll(dispatcher, cmdReg);
			new GetCommand().registerAll(dispatcher, cmdReg);
			new UnbindSkullCommand().registerAll(dispatcher, cmdReg);
			new MaxCommand().registerAll(dispatcher, cmdReg);
		});
	}
	
}
