package com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.elementio.number;

import net.minecraft.nbt.AbstractNbtNumber;

public class NewAbstractNbtNumberIO implements AbstractNbtNumberIO {
	
	@Override
	public byte byteValue(AbstractNbtNumber number) {
		return number.byteValue();
	}
	
	@Override
	public short shortValue(AbstractNbtNumber number) {
		return number.shortValue();
	}
	
	@Override
	public int intValue(AbstractNbtNumber number) {
		return number.intValue();
	}
	
	@Override
	public long longValue(AbstractNbtNumber number) {
		return number.longValue();
	}
	
	@Override
	public float floatValue(AbstractNbtNumber number) {
		return number.floatValue();
	}
	
	@Override
	public double doubleValue(AbstractNbtNumber number) {
		return number.doubleValue();
	}
	
	@Override
	public Number numberValue(AbstractNbtNumber number) {
		return number.numberValue();
	}
	
}
