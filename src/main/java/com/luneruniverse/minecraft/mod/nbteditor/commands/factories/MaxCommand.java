package com.luneruniverse.minecraft.mod.nbteditor.commands.factories;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import com.luneruniverse.minecraft.mod.nbteditor.commands.SubCommand;
import com.luneruniverse.minecraft.mod.nbteditor.util.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class MaxCommand implements SubCommand {
	
	@Override
	public LiteralArgumentBuilder<FabricClientCommandSource> register(LiteralArgumentBuilder<FabricClientCommandSource> parent, CommandRegistryAccess cmdReg) {
		return parent.then(literal("max")
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
				.executes(context -> max(context, -1, false, false)
			));
	}
	private int max(CommandContext<FabricClientCommandSource> context, int enchantLevel, boolean allEnchants, boolean cursed) throws CommandSyntaxException {
		ItemReference heldItem = MainUtil.getHeldItem();
		Hand hand = heldItem.getHand();
		ItemStack item = heldItem.getItem();
		
		NbtList enchants = item.getEnchantments();
		Registry.ENCHANTMENT.forEach(enchant -> {
			if ((allEnchants || enchant.isAcceptableItem(item)) && (cursed || !enchant.isCursed())) {
				Identifier id = EnchantmentHelper.getEnchantmentId(enchant);
				String idStr = id.toString();
				enchants.removeIf(element -> ((NbtCompound) element).getString("id").equals(idStr));
		        enchants.add(EnchantmentHelper.createNbt(id, enchantLevel == -1 ? enchant.getMaxLevel() : enchantLevel));
			}
		});
		item.setSubNbt(ItemStack.ENCHANTMENTS_KEY, enchants);
		
		MainUtil.saveItem(hand, item);
		context.getSource().sendFeedback(Text.translatable("nbteditor.maxed"));
		
		return Command.SINGLE_SUCCESS;
	}
	
}
