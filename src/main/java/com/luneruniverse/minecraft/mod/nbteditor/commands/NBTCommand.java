package com.luneruniverse.minecraft.mod.nbteditor.commands;

import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.argument;
import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.literal;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.function.Function;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditor;
import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
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
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.text.ClickEvent;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;

public class NBTCommand extends ClientCommand {
	
	private static final File exportDir = new File(NBTEditorClient.SETTINGS_FOLDER, "exported");
	
	public enum ExportType {
		GIVE(item -> "/give @p " + getCmdStr(item)),
		GET(item -> "/get item " + getCmdStr(item)),
		CMDBLOCK(item -> {
			ItemStack cmdBlock = new ItemStack(Items.COMMAND_BLOCK);
			cmdBlock.getOrCreateSubNbt("BlockEntityTag").putString("Command", GIVE.export.apply(item));
			MainUtil.getWithMessage(cmdBlock);
			return null;
		}),
		FILE(item -> {
			File output;
			String time = MainUtil.getFormattedCurrentTime();
			int i = 0;
			do {
				output = new File(exportDir, time + (++i == 1 ? "" : "_" + i) + ".nbt");
			} while (output.exists());
			final File finalOutput = output;
			try {
				if (!exportDir.exists())
					Files.createDirectory(exportDir.toPath());
				NbtIo.writeCompressed(item.writeNbt(new NbtCompound()), output);
				MainUtil.client.player.sendMessage(TextInst.translatable("nbteditor.nbt.export.file.success",
						TextInst.literal(output.getName()).formatted(Formatting.UNDERLINE).styled(style ->
						style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, finalOutput.getAbsolutePath())))), false);
			} catch (Exception e) {
				NBTEditor.LOGGER.error("Error while exporting item", e);
				MainUtil.client.player.sendMessage(TextInst.translatable("nbteditor.nbt.export.file.error", e.getMessage()), false);
			}
			return null;
		});
		
		private final Function<ItemStack, String> export;
		private ExportType(Function<ItemStack, String> export) {
			this.export = export;
		}
		public void export(ItemStack item) {
			String output = export.apply(item);
			if (output != null) {
				MainUtil.client.keyboard.setClipboard(output);
				MainUtil.client.player.sendMessage(TextInst.translatable("nbteditor.nbt.export.copied"), false);
			}
		}
		private static String getCmdStr(ItemStack item) {
			return MultiVersionRegistry.ITEM.getId(item.getItem()).toString() +
					(item.getNbt() == null ? "" : item.getNbt().asString()) + " " + item.getCount();
		}
	}
	
	public static final NBTCommand INSTANCE = new NBTCommand();
	
	private NBTCommand() {
		
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
