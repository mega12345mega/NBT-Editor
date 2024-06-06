package com.luneruniverse.minecraft.mod.nbteditor.commands.factories;

import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.literal;

import java.util.function.Consumer;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.containers.ContainerIO;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalNBT;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.BlockReference;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.NBTReference;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.NBTReferenceFilter;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ContainerItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.screens.factories.BookScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.luneruniverse.minecraft.mod.nbteditor.util.TextUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;

public class BookCommand extends ClientCommand {
	
	public static final NBTReferenceFilter BOOK_FILTER = NBTReferenceFilter.create(
			ref -> ref.getItem().getItem() == Items.WRITTEN_BOOK,
			ref -> {
				if (ref.getBlock() != Blocks.LECTERN)
					return false;
				LocalNBT nbt = ref.getLocalNBT();
				if (!ContainerIO.isContainer(nbt))
					return false;
				ItemStack[] contents = ContainerIO.read(nbt);
				return contents.length == 1 && contents[0].getItem() == Items.WRITTEN_BOOK;
			},
			null,
			TextInst.translatable("nbteditor.no_ref.book"),
			TextInst.translatable("nbteditor.no_hand.no_item.book"));
	
	public static void convertBookToWritable(ItemReference ref) {
		ItemStack item = MainUtil.setType(Items.WRITABLE_BOOK, ref.getItem(), 1);
		boolean formatted = false;
		if (item.hasNbt() && item.getNbt().contains("pages", NbtElement.LIST_TYPE)) {
			NbtList convertedPages = new NbtList();
			for (NbtElement page : item.getNbt().getList("pages", NbtElement.STRING_TYPE)) {
				Text text = TextUtil.fromJsonSafely(((NbtString) page).value);
				if (!formatted && TextUtil.isTextFormatted(text, true))
					formatted = true;
				convertedPages.add(NbtString.of(text.getString()));
			}
			item.getNbt().put("pages", convertedPages);
		}
		if (formatted) {
			MainUtil.client.player.sendMessage(TextInst.translatable("nbteditor.book.convert.formatting_saved"), false);
			MainUtil.get(item, true);
		} else
			ref.saveItem(item, TextInst.translatable("nbteditor.book.convert.success"));
	}
	
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
		builder.then(literal("make_writable").executes(context -> {
			getReference(BookCommand::convertBookToWritable);
			return Command.SINGLE_SUCCESS;
		})).then(literal("new").executes(context -> {
			ItemReference ref = ItemReference.getHeldAir();
			ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
			book.getOrCreateNbt().putString("title", "");
			book.getNbt().putString("author", "");
			book.getNbt().put("pages", new NbtList());
			ref.saveItem(book);
			MainUtil.client.setScreen(new BookScreen(ref));
			return Command.SINGLE_SUCCESS;
		})).executes(context -> {
			getReference(ref -> MainUtil.client.setScreen(new BookScreen(ref)));
			return Command.SINGLE_SUCCESS;
		});
	}
	
	private void getReference(Consumer<ItemReference> consumer) {
		NBTReference.getReference(BOOK_FILTER, false, ref -> {
			if (ref instanceof ItemReference itemRef)
				consumer.accept(itemRef);
			else if (ref instanceof BlockReference blockRef)
				consumer.accept(new ContainerItemReference<>(blockRef, 0));
		});
	}
	
}
