package com.luneruniverse.minecraft.mod.nbteditor.commands.factories;

import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.argument;
import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.literal;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.commands.arguments.SignboardArgumentType;
import com.luneruniverse.minecraft.mod.nbteditor.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.screens.factories.SignboardScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.item.ItemStack;
import net.minecraft.item.SignItem;

public class SignboardCommand extends ClientCommand {
	
	@Override
	public String getName() {
		return "signboard";
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
		builder.then(literal("new").then(argument("sign", SignboardArgumentType.signboard()).executes(context -> {
			ItemReference ref = MainUtil.getHeldAir();
			ref.saveItem(new ItemStack(context.getArgument("sign", SignItem.class)));
			MainUtil.client.setScreen(new SignboardScreen(ref));
			return Command.SINGLE_SUCCESS;
		}))).then(literal("import").executes(context -> {
			SignboardScreen.importSign(MainUtil.getHeldItem(
					item -> item.getItem() instanceof SignItem, TextInst.translatable("nbteditor.no_hand.no_item.signboard")));
			return Command.SINGLE_SUCCESS;
		})).executes(context -> {
			MainUtil.client.setScreen(new SignboardScreen(MainUtil.getHeldItem(
					item -> item.getItem() instanceof SignItem, TextInst.translatable("nbteditor.no_hand.no_item.signboard"))));
			return Command.SINGLE_SUCCESS;
		});
	}
	
}
