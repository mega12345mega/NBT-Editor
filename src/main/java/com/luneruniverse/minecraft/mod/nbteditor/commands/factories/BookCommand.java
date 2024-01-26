package com.luneruniverse.minecraft.mod.nbteditor.commands.factories;

import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.literal;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.screens.factories.BookScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtList;

public class BookCommand extends ClientCommand {
	
	@Override
	public String getName() {
		return "book";
	}
	
	@Override
	public String getExtremeAlias() {
		return "b";
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder, String path) {
		builder.then(literal("new").executes(context -> {
			ItemReference ref = ItemReference.getHeldAir();
			ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
			book.getOrCreateNbt().putString("title", "");
			book.getNbt().putString("author", "");
			book.getNbt().put("pages", new NbtList());
			ref.saveItem(book);
			MainUtil.client.setScreen(new BookScreen(ref));
			return Command.SINGLE_SUCCESS;
		})).executes(context -> {
			MainUtil.client.setScreen(new BookScreen(ItemReference.getHeldItem(
					item -> item.getItem() == Items.WRITTEN_BOOK, TextInst.translatable("nbteditor.no_hand.no_item.book"))));
			return Command.SINGLE_SUCCESS;
		});
	}
	
}
