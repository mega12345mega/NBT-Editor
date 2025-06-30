package com.luneruniverse.minecraft.mod.nbteditor.commands.nbt;

import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.argument;
import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.literal;

import java.io.File;
import java.nio.file.Files;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditor;
import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalBlock;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalEntity;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalItem;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalNBT;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistry;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.NBTManagers;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.NBTReference;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.NBTReferenceFilter;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.TagNames;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.luneruniverse.minecraft.mod.nbteditor.util.TextUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.ClickEvent;
import net.minecraft.util.Formatting;
import net.minecraft.util.PathUtil;

public class NBTExportCommand extends ClientCommand {
	
	public static final NBTReferenceFilter EXPORT_FILTER = NBTReferenceFilter.create(
			ref -> true,
			ref -> true,
			ref -> true,
			TextInst.translatable("nbteditor.no_ref.to_export"),
			TextInst.translatable("nbteditor.no_hand.no_item.to_export"));
	
	public static final NBTReferenceFilter EXPORT_ITEM_FILTER = NBTReferenceFilter.create(
			null,
			ref -> true,
			ref -> true,
			TextInst.translatable("nbteditor.no_ref.to_export_item"),
			TextInst.translatable("nbteditor.requires_server"));
	
	private static final File exportDir = new File(NBTEditorClient.SETTINGS_FOLDER, "exported");
	
	private static LocalEntity stripEntityTags(LocalEntity entity, String... tags) {
		LocalEntity output = entity.copy();
		stripEntityTags(output.getNBT(), tags);
		return output;
	}
	private static void stripEntityTags(NbtCompound nbt, String... tags) {
		for (String tag : tags)
			nbt.remove(tag);
		for (NbtElement passenger : nbt.getList("Passengers", NbtElement.COMPOUND_TYPE))
			stripEntityTags((NbtCompound) passenger, tags);
	}
	
	private static String getItemArgs(ItemStack item) {
		return MVRegistry.ITEM.getId(item.getItem()).toString() + NBTManagers.ITEM.getNbtString(item) + " " + item.getCount();
	}
	private static String getBlockArgs(LocalBlock block) {
		return block.getId().toString() + block.getState().toString() + (block.getNBT() == null ? "" : block.getNBT().asString());
	}
	private static String getEntityArgs(LocalEntity entity) {
		return entity.getId().toString() + " ~ ~ ~" + (entity.getNBT() == null ? "" : " " + entity.getNBT().asString());
	}
	
	private static String getCommand(String itemPrefix, String blockPrefix, String entityPrefix, LocalNBT nbt, boolean stripEntityUUIDs) {
		if (nbt instanceof LocalItem item)
			return itemPrefix + getItemArgs(item.getReadableItem());
		else if (nbt instanceof LocalBlock block)
			return blockPrefix + getBlockArgs(block);
		else if (nbt instanceof LocalEntity entity)
			return entityPrefix + getEntityArgs(stripEntityUUIDs ? stripEntityTags(entity, "UUID") : entity);
		else
			throw new IllegalArgumentException("Cannot export " + nbt.getClass().getName());
	}
	private static String getVanillaCommand(NBTReference<?> ref) {
		return getCommand("/give @p ", "/setblock ~ ~ ~ ", "/summon ", ref.getLocalNBT(), true);
	}
	private static String getGetCommand(NBTReference<?> ref) {
		return getCommand("/get item ", "/get block ~ ~ ~ ", "/get entity ", ref.getLocalNBT(), false);
	}
	
	private static void exportToClipboard(String str) {
		MainUtil.client.keyboard.setClipboard(str);
		MainUtil.client.player.sendMessage(TextInst.translatable("nbteditor.nbt.export.copied"), false);
	}
	
	private static void exportToFile(NbtCompound nbt, String name) {
		try {
			if (!exportDir.exists())
				Files.createDirectory(exportDir.toPath());
			File output = new File(exportDir, PathUtil.getNextUniqueName(exportDir.toPath(), name, ".nbt"));
			nbt.putInt("DataVersion", Version.getDataVersion());
			MVMisc.writeCompressedNbt(nbt, output);
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
	public String getExtremeAlias() {
		return "x";
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder, String path) {
		builder.then(
			literal("cmd").executes(context -> {
				NBTReference.getReference(EXPORT_FILTER, false, ref -> exportToClipboard(getVanillaCommand(ref)));
				return Command.SINGLE_SUCCESS;
			})).then(literal("cmdblock").executes(context -> {
				NBTReference.getReference(EXPORT_FILTER, false, ref -> {
					ItemStack cmdBlock = new ItemStack(Items.COMMAND_BLOCK);
					cmdBlock.manager$modifySubNbt(TagNames.BLOCK_ENTITY_TAG,
							nbt -> MainUtil.fillId(nbt, "minecraft:command_block").putString("Command", getVanillaCommand(ref)));
					MainUtil.getWithMessage(cmdBlock);
				});
				return Command.SINGLE_SUCCESS;
			})).then(literal("get").executes(context -> {
				NBTReference.getReference(EXPORT_FILTER, false, ref -> exportToClipboard(getGetCommand(ref)));
				return Command.SINGLE_SUCCESS;
			})).then(literal("item").executes(context -> {
				NBTReference.getReference(EXPORT_ITEM_FILTER, false, ref -> {
					LocalNBT localNBT = ref.getLocalNBT();
					if (localNBT instanceof LocalEntity localEntity)
						localNBT = stripEntityTags(localEntity, "UUID", "Pos");
					localNBT.toItem().ifPresentOrElse(MainUtil::getWithMessage,
							() -> MainUtil.client.player.sendMessage(TextInst.translatable("nbteditor.nbt.export.item.error"), false));
				});
				return Command.SINGLE_SUCCESS;
			})).then(literal("file").then(argument("name", StringArgumentType.greedyString()).executes(context -> {
				NBTReference.getReference(EXPORT_FILTER, false, ref -> exportToFile(ref.getLocalNBT().serialize(),
						context.getArgument("name", String.class)));
				return Command.SINGLE_SUCCESS;
			})).executes(context -> {
				NBTReference.getReference(EXPORT_FILTER, false, ref -> exportToFile(ref.getLocalNBT().serialize(),
						ref.getLocalNBT().getName().getString() + "_" + MainUtil.getFormattedCurrentTime()));
				return Command.SINGLE_SUCCESS;
			}));
	}
	
}
