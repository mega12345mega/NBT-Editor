package com.luneruniverse.minecraft.mod.nbteditor.commands.factories;

import java.util.List;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommandGroup;

public class FactoryCommand extends ClientCommandGroup {
	
	public FactoryCommand() {
		super(List.of(new AttributesCommand(),
				new BlockStatesCommand(),
				new EnchantmentsCommand(),
				new HideFlagsCommand(),
				new LoreCommand(),
				new MaxCommand(),
				new NameCommand(),
				new SignatureCommand(),
				new UnbindSkullCommand(),
				new UnstackableCommand()));
	}
	
	@Override
	public String getName() {
		return "itemfactory";
	}
	
	@Override
	public boolean allowShortcuts() {
		return true;
	}
	
}
