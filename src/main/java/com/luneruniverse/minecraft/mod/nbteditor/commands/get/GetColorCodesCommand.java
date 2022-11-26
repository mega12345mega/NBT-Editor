package com.luneruniverse.minecraft.mod.nbteditor.commands.get;

import com.luneruniverse.minecraft.mod.nbteditor.InternalItems;
import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

public class GetColorCodesCommand extends ClientCommand {
	
	@Override
	public String getName() {
		return "colorcodes";
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
		builder.executes(context -> {
			MainUtil.getWithMessage(InternalItems.COLOR_CODES.copy());
			return Command.SINGLE_SUCCESS;
		});
	}
	
}
