package com.luneruniverse.minecraft.mod.nbteditor.commands.nbt;

import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.argument;
import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.literal;

import java.io.File;
import java.nio.file.Files;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditor;
import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistry;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.luneruniverse.minecraft.mod.nbteditor.util.TextUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.ClickEvent;
import net.minecraft.util.Formatting;
import net.minecraft.util.PathUtil;

public class NBTExportCommand extends ClientCommand {
	
	private static final File exportDir = new File(NBTEditorClient.SETTINGS_FOLDER, "exported");
	
	private static ItemStack getItemToExport() throws CommandSyntaxException {
		return ItemReference.getHeldItem(item -> true, TextInst.translatable("nbteditor.no_hand.no_item.to_export")).getItem();
	}
	
	private static String getItemToExportStr() throws CommandSyntaxException {
		ItemStack item = getItemToExport();
		return MVRegistry.ITEM.getId(item.getItem()).toString() +
				(item.getNbt() == null ? "" : item.getNbt().asString()) + " " + item.getCount();
	}
	private static void exportToClipboard(String str) {
		MainUtil.client.keyboard.setClipboard(str);
		MainUtil.client.player.sendMessage(TextInst.translatable("nbteditor.nbt.export.copied"), false);
	}
	
	private static void exportToFile(ItemStack item, String name) {
		try {
			if (!exportDir.exists())
				Files.createDirectory(exportDir.toPath());
			File output = new File(exportDir, PathUtil.getNextUniqueName(exportDir.toPath(), name, ".nbt"));
			MVMisc.writeCompressedNbt(item.writeNbt(new NbtCompound()), output);
			MainUtil.client.player.sendMessage(TextUtil.attachFileTextOptions(TextInst.translatable("nbteditor.nbt.export.file.success",
					TextInst.literal(output.getName()).formatted(Formatting.UNDERLINE).styled(style ->
					style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, output.getAbsolutePath())))), output), false);
		} catch (Exception e) {
			NBTEditor.LOGGER.error("Error while exporting item", e);
			MainUtil.client.player.sendMessage(TextInst.translatable("nbteditor.nbt.export.file.error", e.getMessage()), false);
		}
	}
	
	@Override
	public String getName() {
		return "export";
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
		builder.then(
			literal("give").executes(context -> {
				exportToClipboard("/give @p " + getItemToExportStr());
				return Command.SINGLE_SUCCESS;
			})).then(literal("get").executes(context -> {
				exportToClipboard("/get item " + getItemToExportStr());
				return Command.SINGLE_SUCCESS;
			})).then(literal("cmdblock").executes(context -> {
				ItemStack cmdBlock = new ItemStack(Items.COMMAND_BLOCK);
				cmdBlock.getOrCreateSubNbt("BlockEntityTag").putString("Command", "/give @p " + getItemToExportStr());
				MainUtil.getWithMessage(cmdBlock);
				return Command.SINGLE_SUCCESS;
			})).then(literal("file").then(argument("name", StringArgumentType.greedyString()).executes(context -> {
				exportToFile(getItemToExport(), context.getArgument("name", String.class));
				return Command.SINGLE_SUCCESS;
			})).executes(context -> {
				ItemStack item = getItemToExport();
				exportToFile(item, item.getName().getString() + "_" + MainUtil.getFormattedCurrentTime());
				return Command.SINGLE_SUCCESS;
			}));
	}
	
}
