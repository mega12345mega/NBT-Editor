package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.lang.invoke.MethodType;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.JsonOps;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;

public class MVTextEvents {
	
	public static class ClickAction<T> {
		private static final Function<String, Optional<URI>> parseUri = valueStr -> {
			try {
				return Optional.of(new URI(valueStr));
			} catch (URISyntaxException e) {
				return Optional.empty();
			}
		};
		private static final Function<String, Optional<String>> parseStr = Optional::of;
		private static final Function<String, Optional<String>> parseCmd = valueStr -> {
			return valueStr.chars().allMatch(c -> MVMisc.isValidChar((char) c)) ? Optional.of(valueStr) : Optional.empty();
		};
		private static final Function<String, Optional<Integer>> parsePage = valueStr -> {
			try {
				int page = Integer.parseInt(valueStr);
				if (page >= 1)
					return Optional.of(page);
			} catch (NumberFormatException e) {}
			return Optional.empty();
		};
		
		public static final ClickAction<URI> OPEN_URL = new ClickAction<>("open_url", ClickEvent.Action.OPEN_URL, parseUri, ClickEvent.OpenUrl::uri, ClickEvent.OpenUrl::new);
		public static final ClickAction<String> OPEN_FILE = new ClickAction<>("open_file", ClickEvent.Action.OPEN_FILE, parseStr, ClickEvent.OpenFile::path, ClickEvent.OpenFile::new);
		public static final ClickAction<String> RUN_COMMAND = new ClickAction<>("run_command", ClickEvent.Action.RUN_COMMAND, parseCmd, ClickEvent.RunCommand::command, ClickEvent.RunCommand::new);
		public static final ClickAction<String> SUGGEST_COMMAND = new ClickAction<>("suggest_command", ClickEvent.Action.SUGGEST_COMMAND, parseCmd, ClickEvent.SuggestCommand::command, ClickEvent.SuggestCommand::new);
		public static final ClickAction<Integer> CHANGE_PAGE = new ClickAction<>("change_page", ClickEvent.Action.CHANGE_PAGE, parsePage, ClickEvent.ChangePage::page, ClickEvent.ChangePage::new);
		public static final ClickAction<String> COPY_TO_CLIPBOARD = new ClickAction<>("copy_to_clipboard", ClickEvent.Action.COPY_TO_CLIPBOARD, parseStr, ClickEvent.CopyToClipboard::value, ClickEvent.CopyToClipboard::new);
		public static final ClickAction<?>[] VALUES = new ClickAction<?>[] {OPEN_URL, OPEN_FILE, RUN_COMMAND, SUGGEST_COMMAND, CHANGE_PAGE, COPY_TO_CLIPBOARD};
		
		public static ClickAction<?> fromName(String name) {
			for (ClickAction<?> action : VALUES) {
				if (action.getName().equals(name))
					return action;
			}
			throw new IllegalArgumentException("Invalid ClickAction name: " + name);
		}
		
		private static final Supplier<Reflection.MethodInvoker> ClickEvent_getAction =
				Reflection.getOptionalMethod(ClickEvent.class, "method_10845", MethodType.methodType(ClickEvent.Action.class));
		public static ClickAction<?> getAction(ClickEvent event) {
			return switch (Version.<ClickEvent.Action>newSwitch()
					.range("1.21.5", null, () -> event.getAction())
					.range(null, "1.21.4", () -> ClickEvent_getAction.get().invoke(event))
					.get()) {
				case OPEN_URL -> OPEN_URL;
				case OPEN_FILE -> OPEN_FILE;
				case RUN_COMMAND -> RUN_COMMAND;
				case SUGGEST_COMMAND -> SUGGEST_COMMAND;
				case CHANGE_PAGE -> CHANGE_PAGE;
				case COPY_TO_CLIPBOARD -> COPY_TO_CLIPBOARD;
			};
		}
		
		private final String name;
		private final ClickEvent.Action action;
		private final Function<String, Optional<T>> parser;
		private final Function<ClickEvent, T> getter;
		private final Function<T, ClickEvent> constructor;
		
		@SuppressWarnings("unchecked")
		private <E extends ClickEvent> ClickAction(String name, ClickEvent.Action action, Function<String, Optional<T>> parser, Function<E, T> getter, Function<T, ClickEvent> constructor) {
			this.name = name;
			this.action = action;
			this.parser = parser;
			this.getter = (Function<ClickEvent, T>) getter;
			this.constructor = constructor;
		}
		
		public String getName() {
			return name;
		}
		
		public Optional<T> parseValue(String valueStr) {
			return parser.apply(valueStr);
		}
		
		private static final Supplier<Reflection.MethodInvoker> ClickEvent_getValue =
				Reflection.getOptionalMethod(ClickEvent.class, "method_10844", MethodType.methodType(String.class));
		public String getStringifiedValue(ClickEvent event) {
			return Version.<String>newSwitch()
					.range("1.21.5", null, () -> getter.apply(event).toString())
					.range(null, "1.21.4", () -> ClickEvent_getValue.get().invoke(event))
					.get();
		}
		public Optional<T> getValue(ClickEvent event) {
			return parseValue(getStringifiedValue(event));
		}
		
		public ClickEvent newEvent(T value) {
			return Version.<ClickEvent>newSwitch()
					.range("1.21.5", null, () -> constructor.apply(value))
					.range(null, "1.21.4", () -> Reflection.newInstance(ClickEvent.class,
							new Class<?>[] {ClickEvent.Action.class, String.class}, action, value.toString()))
					.get();
		}
		public Optional<ClickEvent> newEventParse(String valueStr) {
			return parseValue(valueStr).map(this::newEvent);
		}
	}
	
	public static class HoverAction<T> {
		public static final HoverAction<Text> SHOW_TEXT = new HoverAction<>("show_text", HoverEvent.Action.SHOW_TEXT, HoverEvent.ShowText::value, HoverEvent.ShowText::new);
		public static final HoverAction<ItemStack> SHOW_ITEM = new HoverAction<>("show_item", HoverEvent.Action.SHOW_ITEM, HoverEvent.ShowItem::item, HoverEvent.ShowItem::new);
		public static final HoverAction<HoverEvent.EntityContent> SHOW_ENTITY = new HoverAction<>("show_entity", HoverEvent.Action.SHOW_ENTITY, HoverEvent.ShowEntity::entity, HoverEvent.ShowEntity::new);
		public static final HoverAction<?>[] VALUES = new HoverAction<?>[] {SHOW_TEXT, SHOW_ITEM, SHOW_ENTITY};
		
		public static HoverAction<?> fromName(String name) {
			for (HoverAction<?> action : VALUES) {
				if (action.getName().equals(name))
					return action;
			}
			throw new IllegalArgumentException("Invalid HoverAction name: " + name);
		}
		
		private static final Supplier<Reflection.MethodInvoker> HoverEvent_getAction =
				Reflection.getOptionalMethod(HoverEvent.class, "method_10892", MethodType.methodType(HoverEvent.Action.class));
		public static HoverAction<?> getAction(HoverEvent event) {
			return switch (Version.<HoverEvent.Action>newSwitch()
					.range("1.21.5", null, () -> event.getAction())
					.range(null, "1.21.4", () -> HoverEvent_getAction.get().invoke(event))
					.get()) {
				case SHOW_TEXT -> SHOW_TEXT;
				case SHOW_ITEM -> SHOW_ITEM;
				case SHOW_ENTITY -> SHOW_ENTITY;
			};
		}
		
		private final String name;
		private final HoverEvent.Action action;
		private final Function<HoverEvent, T> getter;
		private final Function<T, HoverEvent> constructor;
		
		@SuppressWarnings("unchecked")
		private <E extends HoverEvent> HoverAction(String name, HoverEvent.Action action, Function<E, T> getter, Function<T, HoverEvent> constructor) {
			this.name = name;
			this.action = action;
			this.getter = (Function<HoverEvent, T>) getter;
			this.constructor = constructor;
		}
		
		public String getName() {
			return name;
		}
		
		public Optional<T> parseValue(String valueStr) {
			return newEventParse(valueStr).map(this::getValue);
		}
		
		private static final Supplier<Class<?>> HoverEvent$ItemStackContent =
				Reflection.getOptionalClass("net.minecraft.class_2568$class_5249");
		
		private static final Supplier<Reflection.MethodInvoker> HoverEvent_getValue =
				Reflection.getOptionalMethod(HoverEvent.class, "", MethodType.methodType(Object.class, HoverEvent.Action.class));
		private static final Supplier<Reflection.MethodInvoker> HoverEvent$ItemStackContent_asStack =
				Reflection.getOptionalMethod(HoverEvent$ItemStackContent, () -> "method_27683", () -> MethodType.methodType(ItemStack.class));
		@SuppressWarnings("unchecked")
		public T getValue(HoverEvent event) {
			return Version.<T>newSwitch()
					.range("1.21.5", null, () -> getter.apply(event))
					.range(null, "1.21.4", () -> {
						Object value = HoverEvent_getValue.get().invoke(event);
						if (this == SHOW_ITEM)
							return HoverEvent$ItemStackContent_asStack.get().invoke(value);
						return (T) value;
					})
					.get();
		}
		private static final Supplier<Reflection.MethodInvoker> HoverEvent$Action_contentsToJson =
				Reflection.getOptionalMethod(HoverEvent.Action.class, "method_27669", MethodType.methodType(JsonElement.class, Object.class));
		public String getStringifiedValue(HoverEvent event) {
			return Version.<String>newSwitch()
					.range("1.21.5", null, () -> {
						NbtCompound nbt = (NbtCompound) MVMisc.result(HoverEvent.CODEC.encodeStart(NbtOps.INSTANCE, event)).orElseThrow();
						if (this == SHOW_TEXT)
							return nbt.get("value").toString();
						nbt.remove("action");
						return nbt.toString();
					})
					.range("1.20.3", "1.21.4", () -> MVMisc.result(HoverEvent.CODEC.encodeStart(JsonOps.INSTANCE, event)).orElseThrow().getAsJsonObject().get("contents").toString())
					.range(null, "1.20.2", () -> HoverEvent$Action_contentsToJson.get().invoke(action, getValue(event)).toString())
					.get();
		}
		
		public HoverEvent newEvent(T value) {
			return Version.<HoverEvent>newSwitch()
					.range("1.21.5", null, () -> constructor.apply(value))
					.range(null, "1.21.4", () -> Reflection.newInstance(HoverEvent.class,
							new Class<?>[] {HoverEvent.Action.class, Object.class}, action,
							this == SHOW_ITEM ? Reflection.newInstance(HoverEvent$ItemStackContent.get(), new Class<?>[] {ItemStack.class}, value) : value))
					.get();
		}
		private static final Supplier<Reflection.MethodInvoker> HoverEvent_fromJson =
				Reflection.getOptionalMethod(HoverEvent.class, "method_27664", MethodType.methodType(HoverEvent.class, JsonObject.class));
		public Optional<HoverEvent> newEventParse(String valueStr) {
			return Version.<Optional<HoverEvent>>newSwitch()
					.range("1.21.5", null, () -> {
						NbtElement valueNbt;
						try {
							valueNbt = StringNbtReader.fromOps(NbtOps.INSTANCE).read(valueStr);
						} catch (CommandSyntaxException e) {
							return Optional.empty();
						}
						
						NbtCompound nbt = new NbtCompound();
						nbt.putString("action", name);
						if (this == SHOW_TEXT)
							nbt.put("value", valueNbt);
						else if (valueNbt instanceof NbtCompound valueNbtCompound)
							nbt.copyFrom(valueNbtCompound);
						else
							return Optional.empty();
						
						return MVMisc.result(HoverEvent.CODEC.parse(NbtOps.INSTANCE, nbt));
					})
					.range(null, "1.21.4", () -> {
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
								.range("1.20.3", null, () -> MVMisc.result(HoverEvent.CODEC.parse(JsonOps.INSTANCE, json)))
								.range(null, "1.20.2", () -> Optional.ofNullable(HoverEvent_fromJson.get().invoke(null, json)))
								.get();
					})
					.get();
		}
	}
	
}
