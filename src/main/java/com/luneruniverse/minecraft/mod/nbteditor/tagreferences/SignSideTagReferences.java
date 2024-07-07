package com.luneruniverse.minecraft.mod.nbteditor.tagreferences;

import java.util.List;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general.ArraySplitTagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general.NBTTagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general.TagReference;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

public class SignSideTagReferences {
	
	public static final TagReference<Boolean, NbtCompound> GLOWING = Version.<TagReference<Boolean, NbtCompound>>newSwitch()
			.range("1.20.0", null, () -> new NBTTagReference<>(Boolean.class, "has_glowing_text"))
			.range(null, "1.19.4", () -> new NBTTagReference<>(Boolean.class, "GlowingText"))
			.get();
	
	public static final TagReference<String, NbtCompound> COLOR = Version.<TagReference<String, NbtCompound>>newSwitch()
			.range("1.20.0", null, () -> new NBTTagReference<>(String.class, "color"))
			.range(null, "1.19.4", () -> new NBTTagReference<>(String.class, "Color"))
			.get();
	
	public static final TagReference<List<Text>, NbtCompound> TEXT = Version.<TagReference<List<Text>, NbtCompound>>newSwitch()
			.range("1.20.0", null, () -> TagReference.forLists(Text.class, new NBTTagReference<>(Text[].class, "messages")))
			.range(null, "1.19.4", () -> ArraySplitTagReference.forNBT(() -> TextInst.of(""), Text.class, "Text1", "Text2", "Text3", "Text4"))
			.get();
	
}
