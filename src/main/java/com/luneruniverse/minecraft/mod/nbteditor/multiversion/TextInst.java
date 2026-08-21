package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.lang.invoke.MethodType;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonParseException;
import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.registry.DynamicRegistryManagerHolder;
import com.luneruniverse.minecraft.mod.nbteditor.util.TextUtil;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

public class TextInst {
	
	public static Text of(String msg) {
		return Text.of(msg);
	}
	public static EditableText literal(String msg) {
		return new EditableText(Version.<MutableText>newSwitch()
				.range("1.19.0", null, () -> Text.literal(msg))
				.range(null, "1.18.2", () -> Reflection.newInstance("net.minecraft.class_2585", new Class[] {String.class}, msg)) // new LiteralText(msg)
				.get());
	}
	public static EditableText translatable(String key, Object... args) {
		return new EditableText(Version.<MutableText>newSwitch()
				.range("1.20.3", null, () -> Text.stringifiedTranslatable(key, args))
				.range("1.19.0", "1.20.2", () -> Text.translatable(key, args))
				.range(null, "1.18.2", () -> Reflection.newInstance("net.minecraft.class_2588", new Class[] {String.class, Object[].class}, key, args)) // new TranslatableText(key, args)
				.get());
	}
	
	public static EditableText copy(Text text) {
		return new EditableText(text.copy());
	}
	public static EditableText copyContentOnly(Text text) {
		return new EditableText(text.copyContentOnly());
	}
	
	public static EditableText bracketed(Text text) {
		return translatable("chat.square_brackets", text);
	}
	
	
	/**
	 * <strong>CONSIDER USING {@link TextUtil#fromStringSafely(String, boolean)}</strong>
	 */
	public static @Nullable Text fromString(String str, boolean allowSpecialNumbers, boolean eitherFormat) throws IllegalArgumentException {
		return Version.<Text>newSwitch()
				.range("1.21.5", null, () -> {
					IllegalArgumentException wrapper;
					try {
						return fromSnbt(str, allowSpecialNumbers);
					} catch (CommandSyntaxException | Attempt.FailedException e) {
						wrapper = new IllegalArgumentException("Failed to parse text");
						wrapper.addSuppressed(e);
						if (!eitherFormat)
							throw wrapper;
					}
					
					try {
						return fromJson(str);
					} catch (JsonParseException e) {
						wrapper.addSuppressed(e);
						throw wrapper;
					}
				})
				.range(null, "1.21.4", () -> {
					IllegalArgumentException wrapper;
					try {
						return fromJson(str);
					} catch (JsonParseException e) {
						wrapper = new IllegalArgumentException("Failed to parse text");
						wrapper.addSuppressed(e);
						if (!eitherFormat)
							throw wrapper;
					}
					
					try {
						return fromSnbt(str, allowSpecialNumbers);
					} catch (CommandSyntaxException | Attempt.FailedException e) {
						wrapper.addSuppressed(e);
						throw wrapper;
					}
				})
				.get();
	}
	public static String toString(Text text) throws IllegalArgumentException {
		try {
			return Version.<String>newSwitch()
					.range("1.21.5", null, () -> toSnbt(text))
					.range(null, "1.21.4", () -> toJson(text))
					.get();
		} catch (Attempt.FailedException | JsonParseException e) {
			throw new IllegalArgumentException("Failed to stringify text", e);
		}
	}
	
	public static @Nullable Text fromMinecraft(NbtElement mc) throws IllegalArgumentException {
		try {
			return Version.<Text>newSwitch()
					.range("1.21.5", null, () -> fromNbt(mc))
					.range(null, "1.21.4", () -> {
						if (!(mc instanceof NbtString mcStr))
							throw new IllegalArgumentException("Failed to parse text: not a string");
						return fromJson(MVMisc.value(mcStr));
					})
					.get();
		} catch (Attempt.FailedException | JsonParseException e) {
			throw new IllegalArgumentException("Failed to parse text", e);
		}
	}
	public static NbtElement toMinecraft(Text text) throws IllegalArgumentException {
		try {
			return Version.<NbtElement>newSwitch()
					.range("1.21.5", null, () -> toNbt(text))
					.range(null, "1.21.4", () -> NbtString.of(toJson(text)))
					.get();
		} catch (Attempt.FailedException | JsonParseException e) {
			throw new IllegalArgumentException("Failed to stringify text", e);
		}
	}
	
	/**
	 * <strong>CONSIDER USING {@link TextUtil#fromSnbtSafely(String)}</strong>
	 */
	public static Text fromSnbt(String snbt, boolean allowSpecialNumbers) throws CommandSyntaxException, Attempt.FailedException {
		return fromNbt(MixinLink.parseSnbt(snbt, allowSpecialNumbers));
	}
	public static String toSnbt(Text text) throws Attempt.FailedException {
		return toNbt(text).toString();
	}
	
	private static final Supplier<Reflection.MethodInvoker> Text$Serialization_fromJson =
			Reflection.getOptionalMethod(Text.Serialization.class, "method_10877", MethodType.methodType(MutableText.class, String.class));
	/**
	 * <strong>CONSIDER USING {@link TextUtil#fromJsonSafely(String)}</strong>
	 */
	public static @Nullable Text fromJson(String json) throws JsonParseException {
		return Version.<Text>newSwitch()
				.range("1.20.5", null, () -> Text.Serialization.fromJson(json, DynamicRegistryManagerHolder.get()))
				.range(null, "1.20.4", () -> Text$Serialization_fromJson.get().invokeThrowable(JsonParseException.class, null, json))
				.get();
	}
	private static final Supplier<Reflection.MethodInvoker> Text$Serialization_toJsonString =
			Reflection.getOptionalMethod(Text.Serialization.class, "method_10867", MethodType.methodType(String.class, Text.class));
	public static String toJson(Text text) throws JsonParseException {
		return Version.<String>newSwitch()
				.range("1.20.5", null, () -> Text.Serialization.toJsonString(text, DynamicRegistryManagerHolder.get()))
				.range(null, "1.20.4", () -> Text$Serialization_toJsonString.get().invoke(null, text))
				.get();
	}
	
	public static Text fromNbt(NbtElement nbt) throws Attempt.FailedException {
		return Attempt.ofResult(TextCodecs.CODEC.parse(NbtOps.INSTANCE, nbt)).getSuccessOrThrow();
	}
	public static NbtElement toNbt(Text text) throws Attempt.FailedException {
		return Attempt.ofResult(TextCodecs.CODEC.encodeStart(NbtOps.INSTANCE, text)).getSuccessOrThrow();
	}
	
}
