package com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.elementio.number;

import net.minecraft.nbt.AbstractNbtNumber;

public interface AbstractNbtNumberIO {
	public byte byteValue(AbstractNbtNumber number);
	public short shortValue(AbstractNbtNumber number);
	public int intValue(AbstractNbtNumber number);
	public long longValue(AbstractNbtNumber number);
	public float floatValue(AbstractNbtNumber number);
	public double doubleValue(AbstractNbtNumber number);
	public Number numberValue(AbstractNbtNumber number);
}
