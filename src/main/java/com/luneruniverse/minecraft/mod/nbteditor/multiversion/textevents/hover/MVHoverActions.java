package com.luneruniverse.minecraft.mod.nbteditor.multiversion.textevents.hover;

import java.lang.invoke.MethodType;
import java.util.Optional;
import java.util.function.Supplier;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.textevents.MVTextEventAction;

import net.minecraft.item.ItemStack;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;

public class MVHoverActions {
	
	public static final MVHoverAction<?, Text> SHOW_TEXT;
	public static final MVHoverAction<?, ItemStack> SHOW_ITEM;
	public static final MVHoverAction<?, HoverEvent.EntityContent> SHOW_ENTITY;
	
	static {
		if (MVTextEventAction.EVENTS_REFACTORED) {
			SHOW_TEXT = NewMVHoverAction.SHOW_TEXT;
			SHOW_ITEM = NewMVHoverAction.SHOW_ITEM;
			SHOW_ENTITY = NewMVHoverAction.SHOW_ENTITY;
		} else {
			SHOW_TEXT = OldMVHoverAction.SHOW_TEXT;
			SHOW_ITEM = OldMVHoverAction.SHOW_ITEM;
			SHOW_ENTITY = OldMVHoverAction.SHOW_ENTITY;
		}
	}
	
	public static final MVHoverAction<?, ?>[] VALUES = new MVHoverAction<?, ?>[] {SHOW_TEXT, SHOW_ITEM, SHOW_ENTITY};
	
	public static MVHoverAction<?, ?> fromName(String name) {
		for (MVHoverAction<?, ?> action : VALUES) {
			if (action.getName().equals(name))
				return action;
		}
		throw new IllegalArgumentException("Invalid MVHoverAction name: " + name);
	}
	
	private static final Supplier<Reflection.MethodInvoker> HoverEvent_getAction =
			Reflection.getOptionalMethod(HoverEvent.class, "method_10892", MethodType.methodType(HoverEvent.Action.class));
	@SuppressWarnings("unchecked")
	public static <E extends HoverEvent> MVHoverAction<? extends E, ?> getAction(E event) {
		HoverEvent.Action action = Version.<HoverEvent.Action>newSwitch()
				.range("1.21.5", null, () -> event.getAction())
				.range(null, "1.21.4", () -> HoverEvent_getAction.get().invoke(event))
				.get();
		if (action == HoverEvent.Action.SHOW_TEXT)
			return (MVHoverAction<? extends E, ?>) SHOW_TEXT;
		if (action == HoverEvent.Action.SHOW_ITEM)
			return (MVHoverAction<? extends E, ?>) SHOW_ITEM;
		if (action == HoverEvent.Action.SHOW_ENTITY)
			return (MVHoverAction<? extends E, ?>) SHOW_ENTITY;
		throw new IllegalArgumentException("Unknown HoverEvent$Action: " + action.name());
	}
	
	@SuppressWarnings("unchecked")
	public static Object getValue(HoverEvent event) {
		return ((MVHoverAction<HoverEvent, ?>) getAction(event)).getValue(event);
	}
	@SuppressWarnings("unchecked")
	public static <V> Optional<V> getValue(MVHoverAction<?, V> action, HoverEvent event) {
		MVHoverAction<?, ?> actualAction = getAction(event);
		if (action != actualAction)
			return Optional.empty();
		return Optional.of(((MVHoverAction<HoverEvent, V>) actualAction).getValue(event));
	}
	
	@SuppressWarnings("unchecked")
	public static String getStringifiedValue(HoverEvent event) {
		return ((MVHoverAction<HoverEvent, ?>) getAction(event)).getStringifiedValue(event);
	}
	
}
