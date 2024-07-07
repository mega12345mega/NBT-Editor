package com.luneruniverse.minecraft.mod.nbteditor.tagreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general.ComponentTagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general.NBTTagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general.TagReference;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.Text;

public class WrittenBookTagReferences {
	
	private static WrittenBookContentComponent getComponent(WrittenBookContentComponent content,
			Supplier<String> title, Supplier<String> author, Supplier<Integer> generation, Supplier<List<Text>> pages) {
		if (content == null)
			content = new WrittenBookContentComponent(RawFilteredPair.of(""), "", 0, List.of(), false);
		return new WrittenBookContentComponent(
				title == null ? content.title() : RawFilteredPair.of(title.get()),
				author == null ? content.author() : author.get(),
				generation == null ? content.generation() : generation.get(),
				pages == null ? content.pages() : pages.get().stream().map(RawFilteredPair::of).toList(),
				content.resolved());
	}
	
	public static final TagReference<String, ItemStack> TITLE = Version.<TagReference<String, ItemStack>>newSwitch()
			.range("1.20.5", null, () -> new ComponentTagReference<>(DataComponentTypes.WRITTEN_BOOK_CONTENT,
					null,
					content -> content == null ? "" : content.title().raw(),
					(content, value) -> getComponent(content, () -> value, null, null, null)))
			.range(null, "1.20.4", () -> TagReference.forItems(() -> "", new NBTTagReference<>(String.class, "title")))
			.get();
	
	public static final TagReference<String, ItemStack> AUTHOR = Version.<TagReference<String, ItemStack>>newSwitch()
			.range("1.20.5", null, () -> new ComponentTagReference<>(DataComponentTypes.WRITTEN_BOOK_CONTENT,
					null,
					content -> content == null ? "" : content.author(),
					(content, value) -> getComponent(content, null, () -> value, null, null)))
			.range(null, "1.20.4", () -> TagReference.forItems(() -> "", new NBTTagReference<>(String.class, "author")))
			.get();
	
	public static final TagReference<Integer, ItemStack> GENERATION = Version.<TagReference<Integer, ItemStack>>newSwitch()
			.range("1.20.5", null, () -> new ComponentTagReference<>(DataComponentTypes.WRITTEN_BOOK_CONTENT,
					null,
					content -> content == null ? 0 : content.generation(),
					(content, value) -> getComponent(content, null, null, () -> value, null)))
			.range(null, "1.20.4", () -> TagReference.forItems(() -> 0, new NBTTagReference<>(Integer.class, "generation")))
			.get();
	
	public static final TagReference<List<Text>, ItemStack> PAGES = Version.<TagReference<List<Text>, ItemStack>>newSwitch()
			.range("1.20.5", null, () -> new ComponentTagReference<>(DataComponentTypes.WRITTEN_BOOK_CONTENT,
					null,
					content -> content == null ? new ArrayList<>() : content.pages().stream().map(RawFilteredPair::raw).collect(Collectors.toList()),
					(content, value) -> getComponent(content, null, null, null, () -> value)))
			.range(null, "1.20.4", () -> TagReference.forItems(ArrayList::new, TagReference.forLists(Text.class, new NBTTagReference<>(Text[].class, "pages"))))
			.get();
	
}
