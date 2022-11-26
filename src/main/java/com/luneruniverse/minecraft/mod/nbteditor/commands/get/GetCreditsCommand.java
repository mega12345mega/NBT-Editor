package com.luneruniverse.minecraft.mod.nbteditor.commands.get;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

public class GetCreditsCommand extends ClientCommand {
	
	@Override
	public String getName() {
		return "credits";
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
		builder.executes(context -> {
			context.getSource().sendFeedback(MainUtil.getLongTranslatableText("nbteditor.credits"));
			return Command.SINGLE_SUCCESS;
		});
	}
	
}
