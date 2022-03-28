package com.luneruniverse.minecraft.mod.nbteditor.commands;

public class CommandHandler {
	
	public static void registerCommands() {
		new NBTCommand().register(null, false);
		new ClientChestCommand().register(null, false);
		new ItemsCommand().register(null, false);
		new HideFlagsCommand().register(null, false);
		new SignatureCommand().register(null, false);
		new LoreCommand().register(null, false);
		new GetCommand().register(null, false);
		new UnbindSkullCommand().register(null, false);
		new MaxCommand().register(null, false);
	}
	
}
