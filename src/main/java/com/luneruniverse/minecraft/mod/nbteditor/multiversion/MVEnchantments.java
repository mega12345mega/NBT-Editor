package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.lang.invoke.MethodType;
import java.util.function.Supplier;

import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.ItemTagReferences;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.Enchants;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;

public class MVEnchantments {
	
	public static final boolean DATA_PACK_ENCHANTMENTS = Version.<Boolean>newSwitch()
			.range("1.21.0", null, true)
			.range(null, "1.20.6", false)
			.get();
	
	@SuppressWarnings("unchecked")
	private static Enchantment getEnchantment(String field) {
		Object output = Reflection.getField(Enchantments.class, field,
				DATA_PACK_ENCHANTMENTS ? "Lnet/minecraft/class_5321;" : "Lnet/minecraft/class_1887;").get(null);
		if (DATA_PACK_ENCHANTMENTS)
			return MVRegistry.getEnchantmentRegistry().get(((RegistryKey<Enchantment>) output).getValue());
		return (Enchantment) output;
	}
	
	public static final Enchantment LOYALTY = getEnchantment("field_9120");
	public static final Enchantment FIRE_ASPECT = getEnchantment("field_9124");
	
	private static final Supplier<Reflection.MethodInvoker> Enchantment_isCursed =
			Reflection.getOptionalMethod(Enchantment.class, "method_8195", MethodType.methodType(boolean.class));
	public static boolean isCursed(Enchantment enchant) {
		return Version.<Boolean>newSwitch()
				.range("1.21.0", null, () -> MVRegistry.getEnchantmentRegistry().getInternalValue().getEntry(enchant).isIn(EnchantmentTags.CURSE))
				.range(null, "1.20.6", () -> Enchantment_isCursed.get().invoke(enchant))
				.get();
	}
	
	public static void addEnchantment(ItemStack item, Enchantment enchant, int level) {
		Enchants enchants = ItemTagReferences.ENCHANTMENTS.get(item);
		enchants.addEnchant(enchant, level);
		ItemTagReferences.ENCHANTMENTS.set(item, enchants);
	}
	
	private static final Supplier<Reflection.MethodInvoker> Enchantment_getTranslationKey =
			Reflection.getOptionalMethod(Enchantment.class, "method_8184", MethodType.methodType(String.class));
	public static Text getEnchantmentName(Enchantment enchant) {
		Formatting color = (isCursed(enchant) ? Formatting.RED : Formatting.GRAY);
		return Version.<Text>newSwitch()
				.range("1.21.0", null, () -> {
					MutableText output = enchant.description().copy();
					Texts.setStyleIfAbsent(output, Style.EMPTY.withColor(color));
					return output;
				})
				.range(null, "1.20.6", () -> {
					EditableText output = TextInst.translatable(Enchantment_getTranslationKey.get().invoke(enchant));
					output.formatted(color);
					return output;
				})
				.get();
	}
	
}
