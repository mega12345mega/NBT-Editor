package com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt;

import java.lang.invoke.MethodType;
import java.util.function.Supplier;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;

import net.minecraft.nbt.AbstractNbtNumber;

public interface MVAbstractNbtNumberParent {
	
	static final Supplier<Reflection.MethodInvoker> AbstractNbtNumber_byteValue =
			Reflection.getOptionalMethod(AbstractNbtNumber.class, "method_10698", MethodType.methodType(byte.class));
	public default byte nbte$byteValue() {
		if (MVNbtCompoundParent.NBT_CODE_REFACTORED)
			return ((AbstractNbtNumber) this).byteValue();
		return AbstractNbtNumber_byteValue.get().invoke(this);
	}
	
	static final Supplier<Reflection.MethodInvoker> AbstractNbtNumber_shortValue =
			Reflection.getOptionalMethod(AbstractNbtNumber.class, "method_10696", MethodType.methodType(short.class));
	public default short nbte$shortValue() {
		if (MVNbtCompoundParent.NBT_CODE_REFACTORED)
			return ((AbstractNbtNumber) this).shortValue();
		return AbstractNbtNumber_shortValue.get().invoke(this);
	}
	
	static final Supplier<Reflection.MethodInvoker> AbstractNbtNumber_intValue =
			Reflection.getOptionalMethod(AbstractNbtNumber.class, "method_10701", MethodType.methodType(int.class));
	public default int nbte$intValue() {
		if (MVNbtCompoundParent.NBT_CODE_REFACTORED)
			return ((AbstractNbtNumber) this).intValue();
		return AbstractNbtNumber_intValue.get().invoke(this);
	}
	
	static final Supplier<Reflection.MethodInvoker> AbstractNbtNumber_longValue =
			Reflection.getOptionalMethod(AbstractNbtNumber.class, "method_10699", MethodType.methodType(long.class));
	public default long nbte$longValue() {
		if (MVNbtCompoundParent.NBT_CODE_REFACTORED)
			return ((AbstractNbtNumber) this).longValue();
		return AbstractNbtNumber_longValue.get().invoke(this);
	}
	
	static final Supplier<Reflection.MethodInvoker> AbstractNbtNumber_floatValue =
			Reflection.getOptionalMethod(AbstractNbtNumber.class, "method_10700", MethodType.methodType(float.class));
	public default float nbte$floatValue() {
		if (MVNbtCompoundParent.NBT_CODE_REFACTORED)
			return ((AbstractNbtNumber) this).floatValue();
		return AbstractNbtNumber_floatValue.get().invoke(this);
	}
	
	static final Supplier<Reflection.MethodInvoker> AbstractNbtNumber_doubleValue =
			Reflection.getOptionalMethod(AbstractNbtNumber.class, "method_10697", MethodType.methodType(double.class));
	public default double nbte$doubleValue() {
		if (MVNbtCompoundParent.NBT_CODE_REFACTORED)
			return ((AbstractNbtNumber) this).doubleValue();
		return AbstractNbtNumber_doubleValue.get().invoke(this);
	}
	
	static final Supplier<Reflection.MethodInvoker> AbstractNbtNumber_numberValue =
			Reflection.getOptionalMethod(AbstractNbtNumber.class, "method_10702", MethodType.methodType(Number.class));
	public default Number nbte$numberValue() {
		if (MVNbtCompoundParent.NBT_CODE_REFACTORED)
			return ((AbstractNbtNumber) this).numberValue();
		return AbstractNbtNumber_numberValue.get().invoke(this);
	}
	
}
