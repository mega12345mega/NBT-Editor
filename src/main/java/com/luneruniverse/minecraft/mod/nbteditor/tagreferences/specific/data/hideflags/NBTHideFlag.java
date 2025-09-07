package com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.hideflags;

import java.util.LinkedHashMap;
import java.util.Map;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;

import net.minecraft.text.Text;

public class NBTHideFlag extends HideFlag {
	
	public static final Map<Integer, HideFlag> FLAGS = new LinkedHashMap<>();
	
	private static HideFlag register(String name, int bit) {
		HideFlag flag = new NBTHideFlag(TextInst.translatable("nbteditor.hide_flags." + name), bit);
		FLAGS.put(bit, flag);
		return flag;
	}
	
	public static final HideFlag ENCHANTMENTS = register("enchantments", 1);
	public static final HideFlag ATTRIBUTE_MODIFIERS = register("attribute_modifiers", 2);
	public static final HideFlag UNBREAKABLE = register("unbreakable", 4);
	public static final HideFlag CAN_DESTROY = register("can_destroy", 8);
	public static final HideFlag CAN_PLACE_ON = register("can_place_on", 16);
	public static final HideFlag MISC = register("misc", 32);
	public static final HideFlag DYED_COLOR = register("dyed_color", 64);
	
	private final Text name;
	private final int bit;
	
	private NBTHideFlag(Text name, int bit) {
		this.name = name;
		this.bit = bit;
	}
	
	@Override
	public Text getName() {
		return name;
	}
	
	public int getBit() {
		return bit;
	}
	
}
