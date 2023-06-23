package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class TextInst {
	
	public static Text of(String msg) {
		return Text.of(msg);
	}
	public static EditableText literal(String msg) {
		return new EditableText(Version.<MutableText>newSwitch()
				.range("1.19.0", null, () -> Text.literal(msg))
				.range(null, "1.18.2", () -> Reflection.newInstance("net.minecraft.class_2585", new Class[] {String.class}, msg)) // new LiteralText(msg)
				.get());
	}
	public static EditableText translatable(String key, Object... args) {
		return new EditableText(Version.<MutableText>newSwitch()
				.range("1.19.0", null, () -> Text.translatable(key, args))
				.range(null, "1.18.2", () -> Reflection.newInstance("net.minecraft.class_2588", new Class[] {String.class, Object[].class}, key, args)) // new TranslatableText(key, args)
				.get());
	}
	
	public static EditableText copy(Text text) {
		return new EditableText(text.copy());
	}
	
}
