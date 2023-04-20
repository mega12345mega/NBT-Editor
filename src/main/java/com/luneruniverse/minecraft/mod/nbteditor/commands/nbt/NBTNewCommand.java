package com.luneruniverse.minecraft.mod.nbteditor.commands.nbt;

import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.argument;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.screens.NBTEditorScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class NBTNewCommand extends ClientCommand {
	
	@Override
	public String getName() {
		return "new";
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
		builder.then(argument("item", MultiVersionMisc.getItemStackArg()).executes(context -> {
			ItemStack newItem = context.getArgument("item", ItemStackArgument.class).createStack(1, true);
			Hand hand;
			if (MainUtil.client.player.getMainHandStack().isEmpty())
				hand = Hand.MAIN_HAND;
			else if (MainUtil.client.player.getOffHandStack().isEmpty())
				hand = Hand.OFF_HAND;
			else {
				MainUtil.client.player.sendMessage(TextInst.translatable("nbteditor.no_hand.all_item"), false);
				return Command.SINGLE_SUCCESS;
			}
			MainUtil.saveItem(hand, newItem);
			MainUtil.client.setScreen(new NBTEditorScreen(new ItemReference(hand)));
			return Command.SINGLE_SUCCESS;
		}));
	}
	
}
