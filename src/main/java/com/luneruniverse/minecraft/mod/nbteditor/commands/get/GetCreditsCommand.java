package com.luneruniverse.minecraft.mod.nbteditor.commands.get;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.util.TextUtil;
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
			context.getSource().sendFeedback(TextUtil.getLongTranslatableText("nbteditor.credits"));
			return Command.SINGLE_SUCCESS;
		});
	}
	
}
