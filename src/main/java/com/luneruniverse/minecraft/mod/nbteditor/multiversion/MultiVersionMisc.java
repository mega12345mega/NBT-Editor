package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.lang.invoke.MethodType;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandRegistrationCallback;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

public class MultiVersionMisc {
	
	private static final Supplier<Reflection.MethodInvoker> Registry_getEntries =
			Reflection.getOptionalMethod(Registry.class, "method_29722", MethodType.methodType(Set.class));
	public static <T> Set<Map.Entry<RegistryKey<T>, T>> getEntrySet(Registry<T> registry) {
		return switch (Version.get()) {
			case v1_19 -> registry.getEntrySet();
			case v1_18 -> Registry_getEntries.get().invoke(registry); // registry.getEntries()
		};
	}
	
	public static KeyBinding getInventoryKey(GameOptions options) {
		return switch (Version.get()) {
			case v1_19 -> options.inventoryKey;
			case v1_18 -> Reflection.getField(GameOptions.class, options, "field_1822", "Lnet/minecraft/class_304;"); // options.keyInventory
		};
	}
	
	private static final Supplier<Reflection.MethodInvoker> Text_shallowCopy =
			Reflection.getOptionalMethod(Text.class, "method_27661", MethodType.methodType(MutableText.class));
	public static EditableText copyText(Text text) {
		return new EditableText(switch (Version.get()) {
			case v1_19 -> text.copy();
			case v1_18 -> Text_shallowCopy.get().invoke(text); // text.shallowCopy()
		});
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T ifOptional(Object optional, Function<Optional<T>, T> getter) {
		if (optional instanceof Optional)
			return getter.apply((Optional<T>) optional);
		return (T) optional;
	}
	
	private static final Supplier<Reflection.MethodInvoker> ItemStackArgumentType_itemStack_registryAccess =
			Reflection.getOptionalMethod(() -> ItemStackArgumentType.class, () -> "method_9776",
			() -> MethodType.methodType(ItemStackArgumentType.class, Reflection.getClass("net.minecraft.class_7157")));
	private static final Supplier<Reflection.MethodInvoker> ItemStackArgumentType_itemStack =
			Reflection.getOptionalMethod(ItemStackArgumentType.class, "method_9776", MethodType.methodType(ItemStackArgumentType.class));
	public static Object registryAccess;
	public static ItemStackArgumentType getItemStackArg() {
		return switch (Version.get()) {
			case v1_19 -> ItemStackArgumentType_itemStack_registryAccess.get().invoke(null, registryAccess); // ItemStackArgumentType.itemStack(registryAccess)
			case v1_18 -> ItemStackArgumentType_itemStack.get().invoke(null); // ItemStackArgumentType.itemStack()
		};
	}
	
	public static void registerCommands(Consumer<CommandDispatcher<FabricClientCommandSource>> callback) {
		switch (Version.get()) {
			case v1_19 -> ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> {
				registryAccess = access;
				callback.accept(dispatcher);
			});
			case v1_18 -> ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> {
				callback.accept(dispatcher);
			});
		}
	}
	
}
