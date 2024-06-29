package com.luneruniverse.minecraft.mod.nbteditor.tagreferences;

import java.util.ArrayList;
import java.util.List;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.util.TextUtil;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;

@PrefixRefersTo(min = "1.20.5", prefix = "minecraft:written_book_content/")
public class WrittenBookTagReference extends TagReference {
	
	@RefersTo(min = "1.20.5", path = "title/raw")
	@RefersTo(max = "1.20.4", path = "title")
	public String title;
	
	@RefersTo(path = "author")
	public String author;
	
	@RefersTo(path = "generation")
	public int generation;
	
	@RefersTo(path = "pages")
	private NbtList pagesNbt;
	public List<Text> pages;
	
	public WrittenBookTagReference(int[] version) {
		super(version);
	}
	public WrittenBookTagReference() {
		super();
	}
	
	@Override
	public void load(NbtCompound nbt) {
		super.load(nbt);
		pages = new ArrayList<>();
		for (NbtElement pageNbt : pagesNbt) {
			if (pageNbt instanceof NbtString pageNbtStr)
				pages.add(TextUtil.fromJsonSafely(pageNbtStr.value));
			else if (pageNbt instanceof NbtCompound pageNbtCompound && pageNbtCompound.contains("raw", NbtElement.STRING_TYPE))
				pages.add(TextUtil.fromJsonSafely(pageNbtCompound.getString("raw")));
		}
	}
	
	@Override
	public void save(NbtCompound nbt) {
		pagesNbt = new NbtList();
		for (Text page : pages)
			pagesNbt.add(NbtString.of(TextInst.toJsonString(page)));
		super.save(nbt);
	}
	
}
