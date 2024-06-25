package com.luneruniverse.minecraft.mod.nbteditor.commands.factories;

import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.argument;
import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.literal;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistry;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.util.Enchants;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.item.ItemStack;

public class MaxCommand extends ClientCommand {
	
	@Override
	public String getName() {
		return "max";
	}
	
	@Override
	public String getExtremeAlias() {
		return "m";
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder, String path) {
		builder
				.then(literal("cursed")
						.then(literal("all")
							.then(argument("level", IntegerArgumentType.integer(1, 32767)).executes(context -> max(context, context.getArgument("level", Integer.class), true, true)))
							.executes(context -> max(context, -1, true, true)))
						.then(argument("level", IntegerArgumentType.integer(1, 32767)).executes(context -> max(context, context.getArgument("level", Integer.class), false, true)))
						.executes(context -> max(context, -1, false, true)))
				.then(literal("all")
						.then(argument("level", IntegerArgumentType.integer(1, 32767)).executes(context -> max(context, context.getArgument("level", Integer.class), true, false)))
						.executes(context -> max(context, -1, true, false)))
				.then(argument("level", IntegerArgumentType.integer(1, 32767)).executes(context -> max(context, context.getArgument("level", Integer.class), false, false)))
				.executes(context -> max(context, -1, false, false));
	}
	private int max(CommandContext<FabricClientCommandSource> context, int enchantLevel, boolean allEnchants, boolean cursed) throws CommandSyntaxException {
		ItemReference ref = ItemReference.getHeldItem();
		ItemStack item = ref.getItem();
		
		Enchants enchants = new Enchants(item);
		enchants.removeDuplicates();
		MVRegistry.ENCHANTMENT.forEach(enchant -> {
			if ((allEnchants || enchant.isAcceptableItem(item)) && (cursed || !enchant.isCursed()))
				enchants.setEnchant(enchant, enchantLevel == -1 ? enchant.getMaxLevel() : enchantLevel, true);
		});
		
		ref.saveItem(item, TextInst.translatable("nbteditor.maxed"));
		
		return Command.SINGLE_SUCCESS;
	}
	
}
