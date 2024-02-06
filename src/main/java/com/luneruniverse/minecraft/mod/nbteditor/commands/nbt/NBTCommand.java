package com.luneruniverse.minecraft.mod.nbteditor.commands.nbt;

import java.util.ArrayList;
import java.util.List;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommandGroup;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.NBTEditorScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

public class NBTCommand extends ClientCommandGroup {
	
	public static final NBTCommand INSTANCE = new NBTCommand();
	
	private NBTCommand() {
		super(new ArrayList<>(List.of(
				new NBTConfigCommand(),
				new NBTNewCommand(),
				new NBTExportCommand())));
	}
	
	@Override
	public String getName() {
		return "nbteditor";
	}
	
	@Override
	public String getExtremeAlias() {
		return "n";
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder, String path) {
		super.register(builder, path);
		builder.executes(context -> {
			MainUtil.client.setScreen(new NBTEditorScreen(
					ConfigScreen.isAirEditable() ? ItemReference.getHeldItemAirable() : ItemReference.getHeldItem()));
			return Command.SINGLE_SUCCESS;
		});
	}
	
	@Override
	public boolean allowShortcuts() {
		return true;
	}
	
}
