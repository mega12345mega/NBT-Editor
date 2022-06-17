package com.luneruniverse.minecraft.mod.nbteditor.commands;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.util.Lore;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

public class SignatureCommand implements ClientCommand {
	
	private static final File SIGNATURE_FILE = new File(NBTEditorClient.SETTINGS_FOLDER, "signature.json");
	private static Text signature;
	static {
		if (!SIGNATURE_FILE.exists())
			signature = Text.translatable("nbteditor.signature_default");
		else {
			try {
				signature = Text.Serializer.fromJson(new String(Files.readAllBytes(SIGNATURE_FILE.toPath())));
			} catch (IOException e) {
				e.printStackTrace();
				signature = Text.translatable("nbteditor.signature_load_error");
			}
		}
	}
	
	
	
	@Override
	public LiteralCommandNode<FabricClientCommandSource> register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess cmdReg) {
		Command<FabricClientCommandSource> addSignature = context -> {
			ClientPlayerEntity player = context.getSource().getPlayer();
			Map.Entry<Hand, ItemStack> heldItem = MainUtil.getHeldItem(player);
			Hand hand = heldItem.getKey();
			ItemStack item = heldItem.getValue();
			
			Lore lore = new Lore(item);
			if (lore.size() == 0 || !lore.getLine(-1).equals(signature))
				lore.addLine(signature);
			else {
				context.getSource().sendFeedback(Text.translatable("nbteditor.signature_already_added"));
				return Command.SINGLE_SUCCESS;
			}
			
			MainUtil.saveItem(hand, item);
			context.getSource().sendFeedback(Text.translatable("nbteditor.signature_added"));
			
			return Command.SINGLE_SUCCESS;
		};
		
		return dispatcher.register(
				literal("signature").executes(addSignature)
					.then(literal("add").executes(addSignature))
					.then(literal("remove").executes(context -> {
						ClientPlayerEntity player = context.getSource().getPlayer();
						Map.Entry<Hand, ItemStack> heldItem = MainUtil.getHeldItem(player);
						Hand hand = heldItem.getKey();
						ItemStack item = heldItem.getValue();
						
						Lore lore = new Lore(item);
						if (lore.size() == 0 || !lore.getLine(-1).equals(signature)) {
							context.getSource().sendFeedback(Text.translatable("nbteditor.signature_not_found"));
							return Command.SINGLE_SUCCESS;
						}
						
						lore.removeLine(-1);
						MainUtil.saveItem(hand, item);
						context.getSource().sendFeedback(Text.translatable("nbteditor.signature_removed"));
						
						return Command.SINGLE_SUCCESS;
					}))
					.then(literal("edit").then(argument("signature", TextArgumentType.text()).executes(context -> {
						Text oldSignature = signature;
						
						try {
							signature = context.getArgument("signature", Text.class);
						} catch (IllegalArgumentException e) {
							throw new SimpleCommandExceptionType(Text.translatable("nbteditor.signature_arg_missing")).create();
						}
						try {
							Files.write(SIGNATURE_FILE.toPath(), Text.Serializer.toJson(signature).getBytes());
						} catch (IOException e) {
							e.printStackTrace();
							throw new SimpleCommandExceptionType(Text.translatable("nbteditor.signature_save_error")).create();
						}
						
						ClientPlayerEntity player = context.getSource().getPlayer();
						Map.Entry<Hand, ItemStack> heldItem = MainUtil.getHeldItem(player);
						Hand hand = heldItem.getKey();
						ItemStack item = heldItem.getValue();
						
						Lore lore = new Lore(item);
						if (lore.size() != 0 && lore.getLine(-1).equals(oldSignature)) {
							lore.setLine(signature, -1);
							MainUtil.saveItem(hand, item);
						}
						
						context.getSource().sendFeedback(Text.translatable("nbteditor.signature_edited"));
						
						return Command.SINGLE_SUCCESS;
					})))
			);
	}
	
	@Override
	public List<String> getAliases() {
		return Arrays.asList("sign");
	}
	
}
