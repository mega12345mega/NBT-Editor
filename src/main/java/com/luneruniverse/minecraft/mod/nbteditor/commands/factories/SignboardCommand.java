package com.luneruniverse.minecraft.mod.nbteditor.commands.factories;

import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.argument;
import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.literal;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.commands.arguments.SignboardArgumentType;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistry;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.NBTReference;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.NBTReferenceFilter;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.screens.factories.SignboardScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.block.AbstractSignBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SignItem;

public class SignboardCommand extends ClientCommand {
	
	public static final NBTReferenceFilter SIGNBOARD_FILTER = NBTReferenceFilter.create(
			ref -> ref.getItem().getItem() instanceof SignItem,
			ref -> MVRegistry.BLOCK.get(ref.getId()) instanceof AbstractSignBlock,
			null,
			TextInst.translatable("nbteditor.no_ref.signboard"),
			TextInst.translatable("nbteditor.no_hand.no_item.signboard"));
	
	@Override
	public String getName() {
		return "signboard";
	}
	
	@Override
	public String getExtremeAlias() {
		return "s";
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder, String path) {
		builder.then(literal("new").then(argument("sign", SignboardArgumentType.signboard()).executes(context -> {
			ItemReference ref = ItemReference.getHeldAir();
			ref.saveItem(new ItemStack(context.getArgument("sign", SignItem.class)));
			MainUtil.client.setScreen(new SignboardScreen<>(ref));
			return Command.SINGLE_SUCCESS;
		}))).then(literal("import").executes(context -> {
			SignboardScreen.importSign(ItemReference.getHeldItem(
					item -> item.getItem() instanceof SignItem, TextInst.translatable("nbteditor.no_hand.no_item.signboard")));
			return Command.SINGLE_SUCCESS;
		})).executes(context -> {
			NBTReference.getReference(SIGNBOARD_FILTER, false, ref -> MainUtil.client.setScreen(new SignboardScreen<>(ref)));
			return Command.SINGLE_SUCCESS;
		});
	}
	
}
