package com.luneruniverse.minecraft.mod.nbteditor.commands;

import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.argument;
import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.literal;

import java.util.List;
import java.util.function.Function;

import com.luneruniverse.minecraft.mod.nbteditor.commands.arguments.EnumArgumentType;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionRegistry;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.NBTEditorScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class NBTCommand extends ClientCommand {
	
	public enum ExportType {
		GIVE(item -> "/give @p " + item),
		GET(item -> "/get item " + item),
		CMDBLOCK(item -> {
			ItemStack cmdBlock = new ItemStack(Items.COMMAND_BLOCK);
			cmdBlock.getOrCreateSubNbt("BlockEntityTag").putString("Command", ExportType.GIVE.export.apply(item));
			MainUtil.getWithMessage(cmdBlock);
			return null;
		});
		
		private final Function<String, String> export;
		private ExportType(Function<String, String> export) {
			this.export = export;
		}
		public void export(ItemStack item) {
			String output = export.apply(MultiVersionRegistry.ITEM.getId(item.getItem()).toString() +
					(item.getNbt() == null ? "" : item.getNbt().asString()) + " " + item.getCount());
			if (output != null) {
				MainUtil.client.keyboard.setClipboard(output);
				MainUtil.client.player.sendMessage(TextInst.translatable("nbteditor.nbt.export.copied"), false);
			}
		}
	}
	
	@Override
	public String getName() {
		return "nbteditor";
	}
	
	@Override
	public List<String> getAliases() {
		return List.of("nbt");
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
		builder.then(literal("config").executes(context -> {
			MainUtil.client.setScreen(new ConfigScreen(null));
			return Command.SINGLE_SUCCESS;
		})).then(literal("new").then(argument("item", MultiVersionMisc.getItemStackArg()).executes(context -> {
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
		}))).then(literal("export").then(argument("type", EnumArgumentType.options(ExportType.class)).executes(context -> {
			context.getArgument("type", ExportType.class).export(MainUtil.getHeldItem(item -> true, TextInst.translatable("nbteditor.no_hand.no_item.to_export")).getItem());
			return Command.SINGLE_SUCCESS;
		}))).executes(context -> {
			MainUtil.client.setScreen(new NBTEditorScreen(ConfigScreen.isAirEditable() ? MainUtil.getHeldItemAirable() : MainUtil.getHeldItem()));
			return Command.SINGLE_SUCCESS;
		});
	}
	
}
