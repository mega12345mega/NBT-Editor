package com.luneruniverse.minecraft.mod.nbteditor.server;

public class ServerMainUtil {
	
	public static Class<?> getRootEnclosingClass(Class<?> clazz) {
		Class<?> prevClass = clazz;
		while ((clazz = clazz.getEnclosingClass()) != null)
			prevClass = clazz;
		return prevClass;
	}
	
}
