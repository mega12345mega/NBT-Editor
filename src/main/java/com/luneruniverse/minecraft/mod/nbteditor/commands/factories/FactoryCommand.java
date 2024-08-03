package com.luneruniverse.minecraft.mod.nbteditor.commands.factories;

import java.util.ArrayList;
import java.util.List;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommandGroup;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.NBTReference;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.NBTReferenceFilter;
import com.luneruniverse.minecraft.mod.nbteditor.screens.factories.LocalFactoryScreen;
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
				new RandomUUIDCommand(),
				new SignatureCommand(),
				new SignboardCommand(),
				new UnbindSkullCommand(),
				new UnbreakableCommand())));
		
		Version.newSwitch()
				.range("1.20.5", null, () -> getChildren().add(new MaxStackSizeCommand()))
				.range(null, "1.20.4", () -> {})
				.run();
	}
	
	@Override
	public String getName() {
		return "factory";
	}
	
	@Override
	public String getExtremeAlias() {
		return "f";
	}
	
	@Override
	public boolean allowShortcuts() {
		return true;
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder, String path) {
		super.register(builder, path);
		builder.executes(context -> {
			NBTReference.getReference(NBTReferenceFilter.ANY, false,
					ref -> MainUtil.client.setScreen(new LocalFactoryScreen<>(ref)));
			return Command.SINGLE_SUCCESS;
		});
	}
	
}
