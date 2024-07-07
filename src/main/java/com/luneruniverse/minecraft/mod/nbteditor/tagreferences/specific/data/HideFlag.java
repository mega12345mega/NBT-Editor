package com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;

import net.minecraft.text.Text;

public enum HideFlag {
	ENCHANTMENTS(TextInst.translatable("nbteditor.hide_flags.enchantments"), 1),
	ATTRIBUTE_MODIFIERS(TextInst.translatable("nbteditor.hide_flags.attribute_modifiers"), 2),
	UNBREAKABLE(TextInst.translatable("nbteditor.hide_flags.unbreakable"), 4),
	CAN_DESTORY(TextInst.translatable("nbteditor.hide_flags.can_destroy"), 8),
	CAN_PLACE_ON(TextInst.translatable("nbteditor.hide_flags.can_place_on"), 16),
	MISC(TextInst.translatable("nbteditor.hide_flags.misc"), 32),
	DYED_COLOR(TextInst.translatable("nbteditor.hide_flags.dyed_color"), 64);
	
	private final Text text;
	private final int code;
	
	private HideFlag(Text text, int code) {
		this.text = text;
		this.code = code;
	}
	
	public Text getText() {
		return text;
	}
	
	public boolean isEnabled(int code) {
		return (code & this.code) != 0;
	}
	public int set(int code, boolean enabled) {
		return enabled ? (code | this.code) : (code & ~this.code);
	}
	public int toggle(int code) {
		return (code & ~this.code) | (~code & this.code);
	}
}