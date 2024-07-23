package com.luneruniverse.minecraft.mod.nbteditor.commands.factories;

import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.argument;
import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.literal;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Formatting;

public class MaxStackSizeCommand extends ClientCommand {
	
	@Override
	public String getName() {
		return "maxstacksize";
	}
	
	@Override
	public String getExtremeAlias() {
		return "mss";
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder, String path) {
		builder.then(literal("default").executes(context -> {
			ItemReference ref = ItemReference.getHeldItem();
			ItemStack item = ref.getItem();
			if (item.getComponentChanges().get(DataComponentTypes.MAX_STACK_SIZE).isEmpty()) {
				MainUtil.client.player.sendMessage(TextInst.translatable("nbteditor.max_stack_size.already_removed"), false);
			} else if (item.contains(DataComponentTypes.MAX_DAMAGE) &&
					item.getDefaultComponents().getOrDefault(DataComponentTypes.MAX_STACK_SIZE, 1) > 1) {
				MainUtil.client.player.sendMessage(TextInst.translatable("nbteditor.max_stack_size.invalid_state"), false);
			} else {
				item.set(DataComponentTypes.MAX_STACK_SIZE, item.getDefaultComponents().get(DataComponentTypes.MAX_STACK_SIZE));
				ref.saveItem(item, TextInst.translatable("nbteditor.max_stack_size.removed"));
			}
			return Command.SINGLE_SUCCESS;
		})).then(argument("size", IntegerArgumentType.integer(1, 99)).executes(context -> {
			int size = context.getArgument("size", Integer.class);
			ItemReference ref = ItemReference.getHeldItem();
			ItemStack item = ref.getItem();
			if (item.contains(DataComponentTypes.MAX_DAMAGE) && size > 1)
				MainUtil.client.player.sendMessage(TextInst.translatable("nbteditor.max_stack_size.invalid_state"), false);
			else {
				item.set(DataComponentTypes.MAX_STACK_SIZE, size);
				ref.saveItem(item, TextInst.translatable("nbteditor.max_stack_size.added",
						TextInst.literal(size + "").formatted(Formatting.GOLD)));
			}
			return Command.SINGLE_SUCCESS;
		}));
	}
	
}
