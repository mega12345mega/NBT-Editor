package com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt;

import java.lang.invoke.MethodType;
import java.util.function.Supplier;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;

import net.minecraft.nbt.AbstractNbtNumber;

public interface MVAbstractNbtNumberParent {
	
	static final Supplier<Reflection.MethodInvoker> AbstractNbtNumber_byteValue =
			Reflection.getOptionalMethod(AbstractNbtNumber.class, "method_10698", MethodType.methodType(byte.class));
	public default byte nbte$byteValue() {
		throw new RuntimeException("Missing implementation for MVAbstractNbtNumberParent#nbte$byteValue");
	}
	
	static final Supplier<Reflection.MethodInvoker> AbstractNbtNumber_shortValue =
			Reflection.getOptionalMethod(AbstractNbtNumber.class, "method_10696", MethodType.methodType(short.class));
	public default short nbte$shortValue() {
		throw new RuntimeException("Missing implementation for MVAbstractNbtNumberParent#nbte$shortValue");
	}
	
	static final Supplier<Reflection.MethodInvoker> AbstractNbtNumber_intValue =
			Reflection.getOptionalMethod(AbstractNbtNumber.class, "method_10701", MethodType.methodType(int.class));
	public default int nbte$intValue() {
		throw new RuntimeException("Missing implementation for MVAbstractNbtNumberParent#nbte$intValue");
	}
	
	static final Supplier<Reflection.MethodInvoker> AbstractNbtNumber_longValue =
			Reflection.getOptionalMethod(AbstractNbtNumber.class, "method_10699", MethodType.methodType(long.class));
	public default long nbte$longValue() {
		throw new RuntimeException("Missing implementation for MVAbstractNbtNumberParent#nbte$longValue");
	}
	
	static final Supplier<Reflection.MethodInvoker> AbstractNbtNumber_floatValue =
			Reflection.getOptionalMethod(AbstractNbtNumber.class, "method_10700", MethodType.methodType(float.class));
	public default float nbte$floatValue() {
		throw new RuntimeException("Missing implementation for MVAbstractNbtNumberParent#nbte$floatValue");
	}
	
	static final Supplier<Reflection.MethodInvoker> AbstractNbtNumber_doubleValue =
			Reflection.getOptionalMethod(AbstractNbtNumber.class, "method_10697", MethodType.methodType(double.class));
	public default double nbte$doubleValue() {
		throw new RuntimeException("Missing implementation for MVAbstractNbtNumberParent#nbte$doubleValue");
	}
	
	static final Supplier<Reflection.MethodInvoker> AbstractNbtNumber_numberValue =
			Reflection.getOptionalMethod(AbstractNbtNumber.class, "method_10702", MethodType.methodType(Number.class));
	public default Number nbte$numberValue() {
		throw new RuntimeException("Missing implementation for MVAbstractNbtNumberParent#nbte$numberValue");
	}
	
}
