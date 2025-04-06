package com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data;

import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVComponentType;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.NBTManagers;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general.ComponentTagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general.TagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.HideFlagsNBTTagReference;

import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.JukeboxPlayableComponent;
import net.minecraft.component.type.UnbreakableComponent;
import net.minecraft.item.BlockPredicatesChecker;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.trim.ArmorTrim;
import net.minecraft.text.Text;

public enum HideFlag implements TagReference<Boolean, ItemStack> {
	ENCHANTMENTS(TextInst.translatable("nbteditor.hide_flags.enchantments"), 1,
			getComponent(MVComponentType.ENCHANTMENTS, () -> component -> component.showInTooltip, () -> ItemEnchantmentsComponent::withShowInTooltip)),
	ATTRIBUTE_MODIFIERS(TextInst.translatable("nbteditor.hide_flags.attribute_modifiers"), 2,
			getComponent(MVComponentType.ATTRIBUTE_MODIFIERS, () -> AttributeModifiersComponent::showInTooltip, () -> AttributeModifiersComponent::withShowInTooltip)),
	UNBREAKABLE(TextInst.translatable("nbteditor.hide_flags.unbreakable"), 4,
			getComponent(MVComponentType.UNBREAKABLE, () -> UnbreakableComponent::showInTooltip, () -> UnbreakableComponent::withShowInTooltip)),
	CAN_BREAK(TextInst.translatable("nbteditor.hide_flags.can_" + (NBTManagers.COMPONENTS_EXIST ? "break" : "destroy")), 8,
			getComponent(MVComponentType.CAN_BREAK, () -> BlockPredicatesChecker::showInTooltip, () -> BlockPredicatesChecker::withShowInTooltip)),
	CAN_PLACE_ON(TextInst.translatable("nbteditor.hide_flags.can_place_on"), 16,
			getComponent(MVComponentType.CAN_PLACE_ON, () -> BlockPredicatesChecker::showInTooltip, () -> BlockPredicatesChecker::withShowInTooltip)),
	MISC(TextInst.translatable("nbteditor.hide_flags.misc"), 32,
			() -> ComponentTagReference.forExistance(MVComponentType.HIDE_ADDITIONAL_TOOLTIP)),
	DYED_COLOR(TextInst.translatable("nbteditor.hide_flags.dyed_color"), 64,
			getComponent(MVComponentType.DYED_COLOR, () -> DyedColorComponent::showInTooltip, () -> DyedColorComponent::withShowInTooltip)),
	
	TOOLTIP(TextInst.translatable("nbteditor.hide_flags.tooltip"), -1,
			() -> ComponentTagReference.forExistance(MVComponentType.HIDE_TOOLTIP)),
	/**
	 * Was previously covered by MISC
	 */
	STORED_ENCHANTMENTS(TextInst.translatable("nbteditor.hide_flags.stored_enchantments"), -1,
			getComponent(MVComponentType.STORED_ENCHANTMENTS, () -> component -> component.showInTooltip, () -> ItemEnchantmentsComponent::withShowInTooltip)),
	TRIM(TextInst.translatable("nbteditor.hide_flags.trim"), -1,
			getComponent(MVComponentType.TRIM, () -> component -> component.showInTooltip, () -> ArmorTrim::withShowInTooltip)),
	JUKEBOX_PLAYABLE(TextInst.translatable("nbteditor.hide_flags.jukebox_playable"), -1,
			getComponent(MVComponentType.JUKEBOX_PLAYABLE, () -> JukeboxPlayableComponent::showInTooltip, () -> JukeboxPlayableComponent::withShowInTooltip));
	
	private static <C> Supplier<ComponentTagReference<Boolean, C>> getComponent(MVComponentType<C> component,
			Supplier<Predicate<C>> getter, Supplier<BiFunction<C, Boolean, C>> setter) {
		return () -> new ComponentTagReference<>(component,
				null,
				componentValue -> componentValue == null ? false : !getter.get().test(componentValue),
				(componentValue, value) -> componentValue == null ? null : setter.get().apply(componentValue, value == null ? true : !value))
				.passNullValue();
	}
	
	private final Text text;
	private final int code;
	private final TagReference<Boolean, ItemStack> tagRef;
	
	private <C> HideFlag(Text text, int code, Supplier<ComponentTagReference<Boolean, C>> compTagRef) {
		this.text = text;
		this.code = code;
		this.tagRef = NBTManagers.COMPONENTS_EXIST ? compTagRef.get() : new HideFlagsNBTTagReference(this);
	}
	
	public Text getText() {
		return text;
	}
	public boolean isOnlyForComponents() {
		return code <= 0;
	}
	public boolean isInThisVersion() {
		return code > 0 || NBTManagers.COMPONENTS_EXIST;
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
	
	@Override
	public Boolean get(ItemStack object) {
		return tagRef.get(object);
	}
	@Override
	public void set(ItemStack object, Boolean value) {
		tagRef.set(object, value);
	}
}