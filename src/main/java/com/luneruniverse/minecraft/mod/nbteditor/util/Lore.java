package com.luneruniverse.minecraft.mod.nbteditor.util;

import java.util.Collection;
import java.util.List;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;

public class Lore {
	
	private final NbtList lore;
	
	public Lore(ItemStack item) {
		
		NbtCompound nbt = item.getOrCreateNbt();
		if (!nbt.contains("display", NbtType.COMPOUND))
			nbt.put("display", new NbtCompound());
		NbtCompound display = nbt.getCompound("display");
		this.lore = display.getList("Lore", NbtType.STRING);
		display.put("Lore", this.lore);
		
	}
	
	public List<Text> getLore() {
		return lore.stream().map(element -> (Text) Text.Serializer.fromJson(((NbtString) element).asString())).toList();
	}
	public Text getLine(int pos) {
		if (pos < 0)
			pos = lore.size() + pos;
		
		return Text.Serializer.fromJson(((NbtString) lore.get(pos)).asString());
	}
	public int size() {
		return lore.size();
	}
	public boolean isEmpty() {
		return size() == 0;
	}
	
	public void addLine(Text text, int pos) {
		NbtString line = NbtString.of(Text.Serializer.toJson(text));
		if (pos < 0)
			lore.add(line);
		else
			lore.add(pos, line);
	}
	public void addLine(Text text) {
		addLine(text, -1);
	}
	
	public void removeLine(int pos) {
		if (pos < 0)
			lore.remove(lore.size() + pos);
		else
			lore.remove(pos);
	}
	
	public void setLine(Text text, int pos) {
		NbtString line = NbtString.of(Text.Serializer.toJson(text));
		if (pos < 0)
			lore.set(lore.size() + pos, line);
		else
			lore.set(pos, line);
	}
	
	public void setAllLines(Collection<Text> lines) {
		clearLore();
		lines.forEach(this::addLine);
	}
	public void clearLore() {
		lore.clear();
	}
	
}
