package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import net.minecraft.util.Identifier;

public class IdentifierInst {
	
	public static Identifier of(String id) {
		return Version.<Identifier>newSwitch()
				.range("1.21.0", null, () -> Identifier.of(id))
				.range(null, "1.20.6", () -> Reflection.newInstance("net.minecraft.class_2960", new Class[] {String.class}, id))
				.get();
	}
	public static Identifier of(String namespace, String path) {
		return Version.<Identifier>newSwitch()
				.range("1.21.0", null, () -> Identifier.of(namespace, path))
				.range(null, "1.20.6", () -> Reflection.newInstance("net.minecraft.class_2960", new Class[] {String.class, String.class}, namespace, path))
				.get();
	}
	
}
