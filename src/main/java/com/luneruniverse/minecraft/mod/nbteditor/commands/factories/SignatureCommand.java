package com.luneruniverse.minecraft.mod.nbteditor.commands.factories;

import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.argument;
import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.literal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.google.gson.JsonParseException;
import com.luneruniverse.minecraft.mod.nbteditor.NBTEditor;
import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.commands.arguments.FancyTextArgumentType;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.util.Lore;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class SignatureCommand extends ClientCommand {
	
	private static final File SIGNATURE_FILE = new File(NBTEditorClient.SETTINGS_FOLDER, "signature.json");
	private static Text signature;
	static {
		if (!SIGNATURE_FILE.exists())
			signature = TextInst.translatable("nbteditor.sign.default");
		else {
			try {
				signature = TextInst.fromJson(new String(Files.readAllBytes(SIGNATURE_FILE.toPath())));
			} catch (IOException | JsonParseException e) {
				NBTEditor.LOGGER.error("Error while loading signature", e);
				signature = TextInst.translatable("nbteditor.sign.load_error");
			}
		}
	}
	
	@Override
	public String getName() {
		return "signature";
	}
	
	@Override
	public String getExtremeAlias() {
		return null;
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder, String path) {
		Command<FabricClientCommandSource> addSignature = context -> {
			ItemReference ref = ItemReference.getHeldItem();
			ItemStack item = ref.getItem();
			
			Lore lore = new Lore(item);
			if (!hasSignature(lore))
				lore.addLine(signature);
			else {
				context.getSource().sendFeedback(TextInst.translatable("nbteditor.sign.already_added"));
				return Command.SINGLE_SUCCESS;
			}
			
			ref.saveItem(item, TextInst.translatable("nbteditor.sign.added"));
			
			return Command.SINGLE_SUCCESS;
		};
		
		builder.executes(addSignature)
				.then(literal("add").executes(addSignature))
				.then(literal("remove").executes(context -> {
					ItemReference ref = ItemReference.getHeldItem();
					ItemStack item = ref.getItem();
					
					Lore lore = new Lore(item);
					if (!hasSignature(lore)) {
						context.getSource().sendFeedback(TextInst.translatable("nbteditor.sign.not_added"));
						return Command.SINGLE_SUCCESS;
					}
					
					lore.removeLine(-1);
					ref.saveItem(item, TextInst.translatable("nbteditor.sign.removed"));
					
					return Command.SINGLE_SUCCESS;
				}))
				.then(literal("edit").then(argument("signature", FancyTextArgumentType.fancyText()).executes(context -> {
					Text oldSignature = signature;
					
					try {
						signature = context.getArgument("signature", Text.class);
					} catch (IllegalArgumentException e) {
						throw new SimpleCommandExceptionType(TextInst.translatable("nbteditor.sign.new.missing_arg")).create();
					}
					try {
						Files.write(SIGNATURE_FILE.toPath(), TextInst.toJsonString(signature).getBytes());
					} catch (IOException e) {
						NBTEditor.LOGGER.error("Error while saving signature", e);
						throw new SimpleCommandExceptionType(TextInst.translatable("nbteditor.sign.save_error")).create();
					}
					
					ItemReference ref = ItemReference.getHeldItem();
					ItemStack item = ref.getItem();
					
					Lore lore = new Lore(item);
					if (hasSignature(lore, oldSignature)) {
						lore.setLine(signature, -1);
						ref.saveItem(item, TextInst.translatable("nbteditor.sign.edited"));
					}
					
					return Command.SINGLE_SUCCESS;
				})));
	}
	
	private static boolean hasSignature(Lore lore, Text signature) {
		if (lore.isEmpty())
			return false;
		return lore.getLine(-1).getString().equals(signature.getString());
	}
	private static boolean hasSignature(Lore lore) {
		return hasSignature(lore, signature);
	}
	
}
