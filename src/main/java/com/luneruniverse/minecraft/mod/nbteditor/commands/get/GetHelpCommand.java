package com.luneruniverse.minecraft.mod.nbteditor.commands.get;

import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.argument;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.commands.arguments.EnumArgumentType;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.util.TextUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

public class GetHelpCommand extends ClientCommand {
	
	public enum HelpType {
		NBTEDITOR("nbteditor.help.nbt"),
		CLIENTCHEST("nbteditor.help.client_chest"),
		OPEN("nbteditor.help.open"),
		GET("nbteditor.help.get"),
		FACTORIES("nbteditor.help.factories"),
		TEXTFORMAT("nbteditor.help.text_format");
		
		private final String msgKey;
		
		private HelpType(String msgKey) {
			this.msgKey = msgKey;
		}
	}
	
	@Override
	public String getName() {
		return "help";
	}
	
	@Override
	public String getExtremeAlias() {
		return null;
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder, String path) {
		builder.then(argument("feature", EnumArgumentType.options(HelpType.class)).executes(context -> {
			context.getSource().sendFeedback(TextUtil.getLongTranslatableText(context.getArgument("feature", HelpType.class).msgKey));
			return Command.SINGLE_SUCCESS;
		})).executes(context -> {
			context.getSource().sendFeedback(TextUtil.getLongTranslatableText("nbteditor.help"));
			return Command.SINGLE_SUCCESS;
		});
	}
	
}
