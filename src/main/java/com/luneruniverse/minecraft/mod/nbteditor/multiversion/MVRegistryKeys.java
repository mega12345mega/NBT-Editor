package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

public class MVRegistryKeys {
	
	private static final Class<?> REGISTRY_CLASS = Reflection.getClass("net.minecraft.class_2378");
	private static final Class<?> REGISTRY_KEYS_CLASS = Version.<Class<?>>newSwitch()
			.range("1.19.3", null, () -> Reflection.getClass("net.minecraft.class_7924"))
			.range(null, "1.19.2", () -> REGISTRY_CLASS)
			.get();
	private static <T> RegistryKey<T> getRegistryKey(String oldName, String newName) {
		return Reflection.getField(REGISTRY_KEYS_CLASS,
				Version.<String>newSwitch()
						.range("1.19.3", null, newName)
						.range(null, "1.19.2", oldName)
						.get(),
				"Lnet/minecraft/class_5321;").get(null);
	}
	
	public static final RegistryKey<Registry<World>> WORLD = getRegistryKey("field_25298", "field_41223");
	
}
