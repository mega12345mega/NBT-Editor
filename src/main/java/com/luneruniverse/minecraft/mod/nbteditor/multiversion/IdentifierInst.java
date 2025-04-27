package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;

public class IdentifierInst {
	
	public static Identifier of(String id) throws InvalidIdentifierException {
		return Version.<Identifier>newSwitch()
				.range("1.21.0", null, () -> Identifier.of(id))
				.range(null, "1.20.6", () -> Reflection.newInstanceThrowable(InvalidIdentifierException.class, "net.minecraft.class_2960", new Class[] {String.class}, id))
				.get();
	}
	public static Identifier of(String namespace, String path) throws InvalidIdentifierException {
		return Version.<Identifier>newSwitch()
				.range("1.21.0", null, () -> Identifier.of(namespace, path))
				.range(null, "1.20.6", () -> Reflection.newInstanceThrowable(InvalidIdentifierException.class, "net.minecraft.class_2960", new Class[] {String.class, String.class}, namespace, path))
				.get();
	}
	
	public static boolean isValid(String id) {
		try {
			of(id);
			return true;
		} catch (InvalidIdentifierException e) {
			return false;
		}
	}
	public static boolean isValid(String namespace, String path) {
		try {
			of(namespace, path);
			return true;
		} catch (InvalidIdentifierException e) {
			return false;
		}
	}
	
}
