package com.luneruniverse.minecraft.mod.nbteditor.util;

import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.EditableText;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;

public class Lore {
	
	public interface LoreConsumer {
		public static LoreConsumer createAppend(ItemStack item) {
			Lore lore = new Lore(item);
			return new LoreConsumer() {
				@Override
				public Lore getLore() {
					return lore;
				}
				@Override
				public int getPos() {
					return -1;
				}
				@Override
				public void onLoreEdit(Lore lore) {}
			};
		}
		public static LoreConsumer createReceiveItem(ItemStack item) {
			return new LoreConsumer() {
				@Override
				public Lore getLore() {
					return new Lore(item);
				}
				@Override
				public int getPos() {
					return -1;
				}
				@Override
				public void onLoreEdit(Lore lore) {
					MainUtil.getWithMessage(item);
				}
			};
		}
		public static LoreConsumer createAppendPage(Text page, Consumer<Text> newPage) {
			Lore lore = new Lore(new ItemStack(Items.BOOK));
			lore.setAllLines(TextUtil.splitText(page));
			return new LoreConsumer() {
				@Override
				public Lore getLore() {
					return lore;
				}
				@Override
				public int getPos() {
					return -1;
				}
				@Override
				public void onLoreEdit(Lore lore) {
					newPage.accept(TextUtil.joinLines(lore.getLore()));
				}
			};
		}
		
		public Lore getLore();
		public int getPos();
		public void onLoreEdit(Lore lore);
	}
	
	private final ItemStack item;
	private final NbtList lore;
	
	public Lore(ItemStack item) {
		this.item = item;
		this.lore = new NbtList();
		if (item.hasNbt()) {
			if (item.getNbt().contains("display", NbtElement.COMPOUND_TYPE))
				lore.addAll(item.getNbt().getCompound("display").getList("Lore", NbtElement.STRING_TYPE));
		}
	}
	
	public ItemStack getItem() {
		return item;
	}
	
	private void save() {
		if (lore.isEmpty())
			item.getOrCreateSubNbt("display").remove("Lore");
		else
			item.getOrCreateSubNbt("display").put("Lore", lore.copy());
	}
	
	public int size() {
		return lore.size();
	}
	public boolean isEmpty() {
		return size() == 0;
	}
	
	public List<Text> getLore() {
		return lore.stream().map(element -> TextUtil.fromJsonSafely(((NbtString) element).asString()))
				.collect(Collectors.toList());
	}
	public Text getLine(int pos) {
		if (pos < 0)
			pos = lore.size() + pos;
		
		return TextUtil.fromJsonSafely(((NbtString) lore.get(pos)).asString());
	}
	
	public void addLine(Text text, int pos) {
		NbtString line = NbtString.of(TextInst.toJsonString(text));
		if (pos < 0)
			lore.add(lore.size() + pos + 1, line);
		else
			lore.add(pos, line);
		save();
	}
	public void addLine(Text text) {
		addLine(text, -1);
	}
	
	public void removeLine(int pos) {
		if (pos < 0)
			lore.remove(lore.size() + pos);
		else
			lore.remove(pos);
		save();
	}
	
	public void setLine(Text text, int pos) {
		NbtString line = NbtString.of(TextInst.toJsonString(text));
		if (pos < 0)
			lore.set(lore.size() + pos, line);
		else
			lore.set(pos, line);
		save();
	}
	
	public void setAllLines(Collection<Text> lines) {
		lore.clear();
		if (lines.isEmpty())
			save();
		else
			lines.forEach(this::addLine);
	}
	public void clearLore() {
		lore.clear();
		save();
	}
	
	
	public void addImage(BufferedImage img, int width, int height, int pos) {
		if (pos < 0)
			pos = lore.size() + pos + 1;
		
		img = MainUtil.scaleImage(img, width, height);
		for (int line = 0; line < height; line++) {
			EditableText lineText = TextInst.literal("").styled(style -> style.withItalic(false));
			for (int i = 0; i < width; i++) {
				final int color = img.getRGB(i, line) & 0xFFFFFF;
				lineText.append(TextInst.literal("█").styled(style -> style.withColor(color)));
			}
			addLine(lineText, pos + line);
		}
	}
	public void addImage(BufferedImage img, int width, int height) {
		addImage(img, width, height, -1);
	}
	
}
