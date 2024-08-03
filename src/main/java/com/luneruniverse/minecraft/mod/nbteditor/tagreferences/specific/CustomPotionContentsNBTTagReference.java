package com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific;

import java.lang.invoke.MethodType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general.TagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.CustomPotionContents;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public class CustomPotionContentsNBTTagReference implements TagReference<CustomPotionContents, ItemStack> {
	
	private static final Supplier<Class<?>> PotionUtil = Reflection.getOptionalClass("net.minecraft.class_1844");
	
	private static final Supplier<Reflection.MethodInvoker> PotionUtil_getCustomPotionEffects =
			Reflection.getOptionalMethod(PotionUtil, () -> "method_8068", () -> MethodType.methodType(List.class, ItemStack.class));
	@Override
	public CustomPotionContents get(ItemStack object) {
		Integer color = null;
		if (object.manager$hasNbt()) {
			NbtCompound nbt = object.manager$getNbt();
			if (nbt.contains("CustomPotionColor", NbtElement.NUMBER_TYPE))
				color = nbt.getInt("CustomPotionColor");
		}
		List<StatusEffectInstance> effects = PotionUtil_getCustomPotionEffects.get().invoke(null, object);
		return new CustomPotionContents(Optional.ofNullable(color), effects);
	}
	
	private static final Supplier<Reflection.MethodInvoker> PotionUtil_setCustomPotionEffects =
			Reflection.getOptionalMethod(PotionUtil, () -> "method_8056", () -> MethodType.methodType(ItemStack.class, ItemStack.class, Collection.class));
	@Override
	public void set(ItemStack object, CustomPotionContents value) {
		if (value.color().isEmpty()) {
			if (object.manager$hasNbt())
				object.manager$modifyNbt(nbt -> nbt.remove("CustomPotionColor"));
		} else
			object.manager$modifyNbt(nbt -> nbt.putInt("CustomPotionColor", value.color().get()));
		PotionUtil_setCustomPotionEffects.get().invoke(null, object, value.effects());
	}
	
}
