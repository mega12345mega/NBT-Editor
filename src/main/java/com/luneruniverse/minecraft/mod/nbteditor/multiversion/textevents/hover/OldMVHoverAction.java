package com.luneruniverse.minecraft.mod.nbteditor.multiversion.textevents.hover;

import java.lang.invoke.MethodType;
import java.util.Optional;
import java.util.function.Supplier;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.mojang.serialization.JsonOps;

import net.minecraft.item.ItemStack;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;

public class OldMVHoverAction<V> implements MVHoverAction<HoverEvent, V> {
	
	public static final OldMVHoverAction<Text> SHOW_TEXT = new OldMVHoverAction<>("show_text", HoverEvent.Action.SHOW_TEXT);
	public static final OldMVHoverAction<ItemStack> SHOW_ITEM = new OldMVHoverAction<>("show_item", HoverEvent.Action.SHOW_ITEM);
	public static final OldMVHoverAction<HoverEvent.EntityContent> SHOW_ENTITY = new OldMVHoverAction<>("show_entity", HoverEvent.Action.SHOW_ENTITY);
	
	private final String name;
	private final HoverEvent.Action action;
	
	public OldMVHoverAction(String name, HoverEvent.Action action) {
		this.name = name;
		this.action = action;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public Optional<V> parseValue(String valueStr) {
		return newEventWithParse(valueStr).map(this::getValue);
	}
	
	private static final Class<?> HoverEvent$ItemStackContent =
			Reflection.getClass("net.minecraft.class_2568$class_5249");
	
	private static final Reflection.MethodInvoker HoverEvent_getValue =
			Reflection.getMethod(HoverEvent.class, "method_10891", MethodType.methodType(Object.class, HoverEvent.Action.class));
	private static final Reflection.MethodInvoker HoverEvent$ItemStackContent_asStack =
			Reflection.getMethod(HoverEvent$ItemStackContent, "method_27683", MethodType.methodType(ItemStack.class));
	@SuppressWarnings("unchecked")
	@Override
	public V getValue(HoverEvent event) {
		Object value = HoverEvent_getValue.invoke(event);
		if (this == SHOW_ITEM)
			return HoverEvent$ItemStackContent_asStack.invoke(value);
		return (V) value;
	}
	private static final Supplier<Reflection.MethodInvoker> HoverEvent_toJson =
			Reflection.getOptionalMethod(HoverEvent.class, "method_27665", MethodType.methodType(JsonObject.class));
	@Override
	public String getStringifiedValue(HoverEvent event) {
		JsonObject json = Version.<JsonObject>newSwitch()
				.range("1.20.3", "1.21.4", () -> MVMisc.result(HoverEvent.CODEC.encodeStart(JsonOps.INSTANCE, event)).orElseThrow().getAsJsonObject())
				.range(null, "1.20.2", () -> HoverEvent_toJson.get().invoke(event))
				.get();
		
		return json.get("contents").toString();
	}
	
	@Override
	public HoverEvent newEvent(V value) {
		return Reflection.newInstance(HoverEvent.class,
				new Class<?>[] {HoverEvent.Action.class, Object.class}, action,
				this == SHOW_ITEM ? Reflection.newInstance(HoverEvent$ItemStackContent, new Class<?>[] {ItemStack.class}, value) : value);
	}
	private static final Supplier<Reflection.MethodInvoker> HoverEvent_fromJson =
			Reflection.getOptionalMethod(HoverEvent.class, "method_27664", MethodType.methodType(HoverEvent.class, JsonObject.class));
	@Override
	public Optional<HoverEvent> newEventWithParse(String valueStr) {
		JsonElement valueJson;
		try {
			valueJson = new Gson().fromJson(valueStr, JsonElement.class);
		} catch (JsonSyntaxException e) {
			return Optional.empty();
		}
		
		JsonObject json = new JsonObject();
		json.addProperty("action", name);
		json.add("contents", valueJson);
		
		return Version.<Optional<HoverEvent>>newSwitch()
				.range("1.20.3", "1.21.4", () -> MVMisc.result(HoverEvent.CODEC.parse(JsonOps.INSTANCE, json)))
				.range(null, "1.20.2", () -> {
					try {
						return Optional.ofNullable(HoverEvent_fromJson.get().invoke(null, json));
					} catch (RuntimeException e) {
						return Optional.empty();
					}
				})
				.get();
	}
	
}
