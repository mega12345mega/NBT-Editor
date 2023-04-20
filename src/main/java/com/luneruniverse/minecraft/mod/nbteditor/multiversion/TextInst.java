package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import net.minecraft.text.Text;

public class TextInst {
	
	public static Text of(String msg) {
		return Text.of(msg);
	}
	public static EditableText literal(String msg) {
		return new EditableText(switch (Version.get()) {
			case v1_19_4, v1_19_3, v1_19 -> Text.literal(msg);
			case v1_18 -> Reflection.newInstance("net.minecraft.class_2585", new Class[] {String.class}, msg); // new LiteralText(msg)
		});
	}
	public static EditableText translatable(String key, Object... args) {
		return new EditableText(switch (Version.get()) {
			case v1_19_4, v1_19_3, v1_19 -> Text.translatable(key, args);
			case v1_18 -> Reflection.newInstance("net.minecraft.class_2588", new Class[] {String.class, Object[].class}, key, args); // new TranslatableText(key, args)
		});
	}
	
}
