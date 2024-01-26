package com.luneruniverse.minecraft.mod.nbteditor.commands.get;

import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.argument;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.StatusEffectArgumentType;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class GetSoupCommand extends ClientCommand {
	
	@Override
	public String getName() {
		return "soup";
	}
	
	@Override
	public String getExtremeAlias() {
		return "s";
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder, String path) {
		Command<FabricClientCommandSource> getSoup = context -> {
			int duration = getDefaultArg(context, "duration", 5, Integer.class);
			
			ItemStack item = new ItemStack(Items.SUSPICIOUS_STEW, 1);
			MVMisc.addEffectToStew(item, context.getArgument("effect", StatusEffect.class), duration * 20);
			MainUtil.getWithMessage(item);
			return Command.SINGLE_SUCCESS;
		};
		
		builder.then(argument("effect", StatusEffectArgumentType.statusEffect())
				.then(argument("duration", IntegerArgumentType.integer(0)).executes(getSoup)).executes(getSoup));
	}
	
}
