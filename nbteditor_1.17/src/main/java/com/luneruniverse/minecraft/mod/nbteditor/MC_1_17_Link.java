package com.luneruniverse.minecraft.mod.nbteditor;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.item.ItemStack;

public class MC_1_17_Link {
	
	public static class MixinLink {
		public static List<ItemStack> ENCHANT_GLINT_FIX;
	}
	
	public static class ConfigScreen {
		public static Supplier<Boolean> isEnchantGlintFix_impl;
		public static boolean isEnchantGlintFix() {
			return isEnchantGlintFix_impl.get();
		}
	}
	
}
