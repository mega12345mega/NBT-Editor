package com.luneruniverse.minecraft.mod.nbteditor.commands.factories;

import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.argument;
import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.literal;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionRegistry;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.util.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

public class MaxCommand extends ClientCommand {
	
	@Override
	public String getName() {
		return "max";
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
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
		ItemReference heldItem = MainUtil.getHeldItem();
		Hand hand = heldItem.getHand();
		ItemStack item = heldItem.getItem();
		
		NbtList enchants = item.getEnchantments();
		MultiVersionRegistry.ENCHANTMENT.forEach(enchant -> {
			if ((allEnchants || enchant.isAcceptableItem(item)) && (cursed || !enchant.isCursed())) {
				Identifier id = EnchantmentHelper.getEnchantmentId(enchant);
				String idStr = id.toString();
				enchants.removeIf(element -> ((NbtCompound) element).getString("id").equals(idStr));
		        enchants.add(EnchantmentHelper.createNbt(id, enchantLevel == -1 ? enchant.getMaxLevel() : enchantLevel));
			}
		});
		item.setSubNbt(ItemStack.ENCHANTMENTS_KEY, enchants);
		
		MainUtil.saveItem(hand, item);
		context.getSource().sendFeedback(TextInst.translatable("nbteditor.maxed"));
		
		return Command.SINGLE_SUCCESS;
	}
	
}
