package com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.elementio.number;

import java.lang.invoke.MethodType;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;

import net.minecraft.nbt.AbstractNbtNumber;

public class OldAbstractNbtNumberIO implements AbstractNbtNumberIO {
	
	private static final Reflection.MethodInvoker AbstractNbtNumber_byteValue =
			Reflection.getMethod(AbstractNbtNumber.class, "method_10698", MethodType.methodType(byte.class));
	@Override
	public byte byteValue(AbstractNbtNumber number) {
		return AbstractNbtNumber_byteValue.invoke(number);
	}
	
	private static final Reflection.MethodInvoker AbstractNbtNumber_shortValue =
			Reflection.getMethod(AbstractNbtNumber.class, "method_10696", MethodType.methodType(short.class));
	@Override
	public short shortValue(AbstractNbtNumber number) {
		return AbstractNbtNumber_shortValue.invoke(number);
	}
	
	private static final Reflection.MethodInvoker AbstractNbtNumber_intValue =
			Reflection.getMethod(AbstractNbtNumber.class, "method_10701", MethodType.methodType(int.class));
	@Override
	public int intValue(AbstractNbtNumber number) {
		return AbstractNbtNumber_intValue.invoke(number);
	}
	
	private static final Reflection.MethodInvoker AbstractNbtNumber_longValue =
			Reflection.getMethod(AbstractNbtNumber.class, "method_10699", MethodType.methodType(long.class));
	@Override
	public long longValue(AbstractNbtNumber number) {
		return AbstractNbtNumber_longValue.invoke(number);
	}
	
	private static final Reflection.MethodInvoker AbstractNbtNumber_floatValue =
			Reflection.getMethod(AbstractNbtNumber.class, "method_10700", MethodType.methodType(float.class));
	@Override
	public float floatValue(AbstractNbtNumber number) {
		return AbstractNbtNumber_floatValue.invoke(number);
	}
	
	private static final Reflection.MethodInvoker AbstractNbtNumber_doubleValue =
			Reflection.getMethod(AbstractNbtNumber.class, "method_10697", MethodType.methodType(double.class));
	@Override
	public double doubleValue(AbstractNbtNumber number) {
		return AbstractNbtNumber_doubleValue.invoke(number);
	}
	
	private static final Reflection.MethodInvoker AbstractNbtNumber_numberValue =
			Reflection.getMethod(AbstractNbtNumber.class, "method_10702", MethodType.methodType(Number.class));
	@Override
	public Number numberValue(AbstractNbtNumber number) {
		return AbstractNbtNumber_numberValue.invoke(number);
	}
	
}
