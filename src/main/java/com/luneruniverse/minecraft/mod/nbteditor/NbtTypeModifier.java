package com.luneruniverse.minecraft.mod.nbteditor;

import java.lang.reflect.Proxy;

import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.NbtType;
import net.minecraft.nbt.NbtTypes;

public class NbtTypeModifier {
	
	@SuppressWarnings("unchecked")
	public static final NbtType<NbtString> NBT_STRING_TYPE = (NbtType<NbtString>) Proxy.newProxyInstance(NBTEditor.class.getClassLoader(), new Class[] { NbtType.class }, (obj, method, args) -> {
		if (method.getName().equals("isImmutable"))
			return false;
		
		return method.invoke(NbtString.TYPE, args);
	});
	
	
	private static NbtType<?>[] VALUES = NbtTypes.VALUES;
	
	
	public static void modify() {
		for (int i = 0; i < VALUES.length; i++) {
			if (VALUES[i] == NbtString.TYPE)
				VALUES[i] = NBT_STRING_TYPE;
		}
	}
	
}
