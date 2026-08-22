package com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.elementio;

import net.minecraft.nbt.AbstractNbtNumber;

/**
 * Allows abbreviating <code>ElementIOs.NUMBER.byteValue(number)</code> to <code>MVN.byteValue(number)</code>
 */
public class MVN {
	
	public static byte byteValue(AbstractNbtNumber number) {
		return ElementIOs.NUMBER.byteValue(number);
	}
	
	public static short shortValue(AbstractNbtNumber number) {
		return ElementIOs.NUMBER.shortValue(number);
	}
	
	public static int intValue(AbstractNbtNumber number) {
		return ElementIOs.NUMBER.intValue(number);
	}
	
	public static long longValue(AbstractNbtNumber number) {
		return ElementIOs.NUMBER.longValue(number);
	}
	
	public static float floatValue(AbstractNbtNumber number) {
		return ElementIOs.NUMBER.floatValue(number);
	}
	
	public static double doubleValue(AbstractNbtNumber number) {
		return ElementIOs.NUMBER.doubleValue(number);
	}
	
	public static Number numberValue(AbstractNbtNumber number) {
		return ElementIOs.NUMBER.numberValue(number);
	}
	
}
