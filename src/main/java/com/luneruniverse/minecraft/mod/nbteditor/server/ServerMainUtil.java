package com.luneruniverse.minecraft.mod.nbteditor.server;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;

public class ServerMainUtil {
	
	public static Class<?> getRootEnclosingClass(Class<?> clazz) {
		Class<?> prevClass = clazz;
		while ((clazz = clazz.getEnclosingClass()) != null)
			prevClass = clazz;
		return prevClass;
	}
	
	public static void setCursorStackSilently(ScreenHandler handler, ItemStack item) {
		handler.setCursorStack(item);
		ServerMVMisc.setPreviousCursorStack(handler, item);
	}
	
}
