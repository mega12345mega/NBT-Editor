package com.luneruniverse.minecraft.mod.nbteditor.commands.factories;

import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.argument;
import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.literal;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.commands.arguments.SignboardArgumentType;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class SignboardCommand extends ClientCommand {
	
	public static final NBTReferenceFilter SIGNBOARD_FILTER = NBTReferenceFilter.create(
			ref -> MVMisc.isSignItem(ref.getItem().getItem()),
			ref -> ref.getBlock() instanceof AbstractSignBlock,
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
			ref.saveItem(new ItemStack(context.getArgument("sign", Item.class)));
			MainUtil.client.setScreen(new SignboardScreen<>(ref));
			return Command.SINGLE_SUCCESS;
		}))).executes(context -> {
			NBTReference.getReference(SIGNBOARD_FILTER, false, ref -> MainUtil.client.setScreen(new SignboardScreen<>(ref)));
			return Command.SINGLE_SUCCESS;
		});
	}
	
}
