package com.luneruniverse.minecraft.mod.nbteditor.commands.factories;

import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.argument;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.commands.arguments.FancyTextArgumentType;
import com.luneruniverse.minecraft.mod.nbteditor.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.screens.factories.DisplayScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class NameCommand extends ClientCommand {
	
	@Override
	public String getName() {
		return "name";
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
		builder.then(argument("name", FancyTextArgumentType.fancyText()).executes(context -> {
			Text name = context.getArgument("name", Text.class);
			ItemReference ref = MainUtil.getHeldItem();
			ItemStack item = ref.getItem();
			item.setCustomName(name);
			ref.saveItem(item, TextInst.translatable("nbteditor.named").append(name));
			
			return Command.SINGLE_SUCCESS;
		})).executes(context -> {
			MainUtil.client.setScreen(new DisplayScreen(MainUtil.getHeldItem()));
			return Command.SINGLE_SUCCESS;
		});
	}
	
}
