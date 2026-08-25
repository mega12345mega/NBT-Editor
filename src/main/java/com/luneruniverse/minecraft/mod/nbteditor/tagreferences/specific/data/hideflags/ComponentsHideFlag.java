package com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.hideflags;

import java.lang.invoke.MethodType;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVComponentType;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;

import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.JukeboxPlayableComponent;
import net.minecraft.item.BlockPredicatesChecker;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.trim.ArmorTrim;
import net.minecraft.text.Text;
import net.minecraft.util.Unit;

public class ComponentsHideFlag extends HideFlag {
	
	public static final Map<ComponentType<?>, HideFlag> FLAGS = new LinkedHashMap<>();
	
	private static HideFlag register(String name, ComponentType<?> component,
			Predicate<ItemStack> getShowInTooltip, BiConsumer<ItemStack, Boolean> setShowInTooltip) {
		HideFlag flag = new ComponentsHideFlag(
				TextInst.translatable("nbteditor.hide_flags." + name), component, getShowInTooltip, setShowInTooltip);
		FLAGS.put(component, flag);
		return flag;
	}
	@SuppressWarnings("unchecked")
	private static HideFlag register(String name, ComponentType<?> component,
			Predicate<Object> getShowInTooltip, BiFunction<Object, Boolean, Object> setShowInTooltip) {
		return register(name, component, item -> getShowInTooltip.test(item.nbte$get(component)),
				(item, showInTooltip) -> {
					Object value = item.nbte$get(component);
					if (value == null)
						return;
					item.set((ComponentType<Object>) component, setShowInTooltip.apply(value, showInTooltip));
				});
	}
	private static HideFlag register(String name, ComponentType<?> component,
			Predicate<Object> getShowInTooltip, Class<?> componentClass, String setterMethodName) {
		return register(name, component, getShowInTooltip, Reflection.getMethod(
				componentClass, setterMethodName, MethodType.methodType(componentClass, boolean.class))::invoke);
	}
	private static HideFlag registerFieldGetter(String name, ComponentType<?> component,
			Class<?> componentClass, String fieldName, String setterMethodName) {
		return register(name, component,
				Reflection.getField(componentClass, fieldName, "Z")::get, componentClass, setterMethodName);
	}
	private static HideFlag registerMethodGetter(String name, ComponentType<?> component,
			Class<?> componentClass, String getterMethodName, String setterMethodName) {
		return register(name, component,
				Reflection.getMethod(componentClass, getterMethodName, MethodType.methodType(boolean.class))::invoke,
				componentClass, setterMethodName);
	}
	
	public static final HideFlag ENCHANTMENTS = registerFieldGetter("enchantments",
			DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.class, "field_49390", "method_58449");
	public static final HideFlag ATTRIBUTE_MODIFIERS = registerMethodGetter("attribute_modifiers",
			DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.class, "comp_2394", "method_58423");
	public static final HideFlag UNBREAKABLE = registerMethodGetter("unbreakable",
			DataComponentTypes.UNBREAKABLE, Reflection.getClass("net.minecraft.class_9300"), "comp_2417", "method_58435");
	public static final HideFlag CAN_BREAK = registerMethodGetter("can_break",
			DataComponentTypes.CAN_BREAK, BlockPredicatesChecker.class, "method_57324", "method_58402");
	public static final HideFlag CAN_PLACE_ON = registerMethodGetter("can_place_on",
			DataComponentTypes.CAN_PLACE_ON, BlockPredicatesChecker.class, "method_57324", "method_58402");
	public static final HideFlag MISC = register("misc",
			(ComponentType<?>) MVComponentType.HIDE_ADDITIONAL_TOOLTIP_1_20_5_1_21_4.getInternalValue(),
			item -> item.contains(MVComponentType.HIDE_ADDITIONAL_TOOLTIP_1_20_5_1_21_4),
			(item, showInTooltip) -> {
				if (showInTooltip)
					item.remove(MVComponentType.HIDE_ADDITIONAL_TOOLTIP_1_20_5_1_21_4);
				else
					item.set(MVComponentType.HIDE_ADDITIONAL_TOOLTIP_1_20_5_1_21_4, Unit.INSTANCE);
			});
	public static final HideFlag DYED_COLOR = registerMethodGetter("dyed_color",
			DataComponentTypes.DYED_COLOR, DyedColorComponent.class, "comp_2385", "method_58422");
	
	// Was previously covered by MISC
	public static final HideFlag STORED_ENCHANTMENTS = registerFieldGetter("stored_enchantments",
			DataComponentTypes.STORED_ENCHANTMENTS, ItemEnchantmentsComponent.class, "field_49390", "method_58449");
	public static final HideFlag TRIM = Version.<HideFlag>newSwitch()
			.range("1.21.0", null, () -> registerMethodGetter("trim",
					DataComponentTypes.TRIM, ArmorTrim.class, "comp_3181", "method_58421"))
			.range("1.20.5", "1.20.6", () -> registerFieldGetter("trim",
					DataComponentTypes.TRIM, ArmorTrim.class, "field_49279", "method_58421"))
			.get();
	
	public static final HideFlag JUKEBOX_PLAYABLE = Version.<HideFlag>newSwitch()
			.range("1.21.0", "1.21.4", () -> registerMethodGetter("jukebox_playable",
					DataComponentTypes.JUKEBOX_PLAYABLE, JukeboxPlayableComponent.class, "comp_2834", "method_60749"))
			.range("1.20.5", "1.20.6", () -> null)
			.get();
	
	private final Text name;
	private final ComponentType<?> component;
	private final Predicate<ItemStack> getShowInTooltip;
	private final BiConsumer<ItemStack, Boolean> setShowInTooltip;
	
	private ComponentsHideFlag(Text name, ComponentType<?> component,
			Predicate<ItemStack> getShowInTooltip, BiConsumer<ItemStack, Boolean> setShowInTooltip) {
		this.name = name;
		this.component = component;
		this.getShowInTooltip = getShowInTooltip;
		this.setShowInTooltip = setShowInTooltip;
	}
	
	@Override
	public Text getName() {
		return name;
	}
	
	public ComponentType<?> getComponent() {
		return component;
	}
	
	public boolean getShowInTooltip(ItemStack item) {
		return getShowInTooltip.test(item);
	}
	
	public void setShowInTooltip(ItemStack item, boolean showInTooltip) {
		setShowInTooltip.accept(item, showInTooltip);
	}
	
}
