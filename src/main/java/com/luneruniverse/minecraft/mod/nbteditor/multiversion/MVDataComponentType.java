package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.util.function.Supplier;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.NBTManagers;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.BlockStateComponent;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.component.type.SuspiciousStewEffectsComponent;
import net.minecraft.component.type.UnbreakableComponent;
import net.minecraft.component.type.WritableBookContentComponent;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.item.BlockPredicatesChecker;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.text.Text;
import net.minecraft.util.Unit;

public class MVDataComponentType<T> {
	
	public static final MVDataComponentType<AttributeModifiersComponent> ATTRIBUTE_MODIFIERS =
			new MVDataComponentType<>(() -> DataComponentTypes.ATTRIBUTE_MODIFIERS);
	public static final MVDataComponentType<NbtComponent> BLOCK_ENTITY_DATA =
			new MVDataComponentType<>(() -> DataComponentTypes.BLOCK_ENTITY_DATA);
	public static final MVDataComponentType<BlockStateComponent> BLOCK_STATE =
			new MVDataComponentType<>(() -> DataComponentTypes.BLOCK_STATE);
	public static final MVDataComponentType<BlockPredicatesChecker> CAN_BREAK =
			new MVDataComponentType<>(() -> DataComponentTypes.CAN_BREAK);
	public static final MVDataComponentType<BlockPredicatesChecker> CAN_PLACE_ON =
			new MVDataComponentType<>(() -> DataComponentTypes.CAN_PLACE_ON);
	public static final MVDataComponentType<NbtComponent> CUSTOM_DATA =
			new MVDataComponentType<>(() -> DataComponentTypes.CUSTOM_DATA);
	public static final MVDataComponentType<Text> CUSTOM_NAME =
			new MVDataComponentType<>(() -> DataComponentTypes.CUSTOM_NAME);
	public static final MVDataComponentType<DyedColorComponent> DYED_COLOR =
			new MVDataComponentType<>(() -> DataComponentTypes.DYED_COLOR);
	public static final MVDataComponentType<ItemEnchantmentsComponent> ENCHANTMENTS =
			new MVDataComponentType<>(() -> DataComponentTypes.ENCHANTMENTS);
	public static final MVDataComponentType<NbtComponent> ENTITY_DATA =
			new MVDataComponentType<>(() -> DataComponentTypes.ENTITY_DATA);
	public static final MVDataComponentType<Unit> HIDE_ADDITIONAL_TOOLTIP =
			new MVDataComponentType<>(() -> DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP);
	public static final MVDataComponentType<Unit> HIDE_TOOLTIP =
			new MVDataComponentType<>(() -> DataComponentTypes.HIDE_TOOLTIP);
	public static final MVDataComponentType<Text> ITEM_NAME =
			new MVDataComponentType<>(() -> DataComponentTypes.ITEM_NAME);
	public static final MVDataComponentType<LoreComponent> LORE =
			new MVDataComponentType<>(() -> DataComponentTypes.LORE);
	public static final MVDataComponentType<Integer> MAX_DAMAGE =
			new MVDataComponentType<>(() -> DataComponentTypes.MAX_DAMAGE);
	public static final MVDataComponentType<Integer> MAX_STACK_SIZE =
			new MVDataComponentType<>(() -> DataComponentTypes.MAX_STACK_SIZE);
	public static final MVDataComponentType<PotionContentsComponent> POTION_CONTENTS =
			new MVDataComponentType<>(() -> DataComponentTypes.POTION_CONTENTS);
	public static final MVDataComponentType<ProfileComponent> PROFILE =
			new MVDataComponentType<>(() -> DataComponentTypes.PROFILE);
	public static final MVDataComponentType<ItemEnchantmentsComponent> STORED_ENCHANTMENTS =
			new MVDataComponentType<>(() -> DataComponentTypes.STORED_ENCHANTMENTS);
	public static final MVDataComponentType<SuspiciousStewEffectsComponent> SUSPICIOUS_STEW_EFFECTS =
			new MVDataComponentType<>(() -> DataComponentTypes.SUSPICIOUS_STEW_EFFECTS);
	public static final MVDataComponentType<ArmorTrim> TRIM =
			new MVDataComponentType<>(() -> DataComponentTypes.TRIM);
	public static final MVDataComponentType<UnbreakableComponent> UNBREAKABLE =
			new MVDataComponentType<>(() -> DataComponentTypes.UNBREAKABLE);
	public static final MVDataComponentType<WritableBookContentComponent> WRITABLE_BOOK_CONTENT =
			new MVDataComponentType<>(() -> DataComponentTypes.WRITABLE_BOOK_CONTENT);
	public static final MVDataComponentType<WrittenBookContentComponent> WRITTEN_BOOK_CONTENT =
			new MVDataComponentType<>(() -> DataComponentTypes.WRITTEN_BOOK_CONTENT);
	
	private final Object component;
	
	public MVDataComponentType(Supplier<Object> component) {
		this.component = (NBTManagers.COMPONENTS_EXIST ? component.get() : null);
	}
	
	public Object getInternalValue() {
		if (component == null)
			throw new IllegalStateException("Components aren't in this version!");
		return component;
	}
	
}
