package com.luneruniverse.minecraft.mod.nbteditor.commands.factories;

import java.util.ArrayList;
import java.util.List;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommandGroup;
import com.luneruniverse.minecraft.mod.nbteditor.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.screens.factories.ItemFactoryScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

public class FactoryCommand extends ClientCommandGroup {
	
	public static final FactoryCommand INSTANCE = new FactoryCommand();
	
	private FactoryCommand() {
		super(new ArrayList<>(List.of(
				new AttributesCommand(),
				new BlockStatesCommand(),
				new BookCommand(),
				new DisplayCommand(),
				new EnchantmentsCommand(),
				new MaxCommand(),
				new SignatureCommand(),
				new SignboardCommand(),
				new UnbindSkullCommand(),
				new UnbreakableCommand(),
				new UnstackableCommand())));
	}
	
	@Override
	public String getName() {
		return "itemfactory";
	}
	
	@Override
	public String getExtremeAlias() {
		return "if";
	}
	
	@Override
	public boolean allowShortcuts() {
		return true;
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder, String path) {
		super.register(builder, path);
		builder.executes(context -> {
			MainUtil.client.setScreen(new ItemFactoryScreen(ItemReference.getHeldItem()));
			return Command.SINGLE_SUCCESS;
		});
	}
	
}
