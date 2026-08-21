package com.luneruniverse.minecraft.mod.nbteditor.multiversion.mixin.toggled;

import java.lang.invoke.MethodType;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.MVAbstractNbtNumberParent;

import net.minecraft.nbt.AbstractNbtNumber;

@Mixin(AbstractNbtNumber.class)
public class AbstractNbtNumberMixin_1_21_4 implements MVAbstractNbtNumberParent {
	
	private static final Supplier<Reflection.MethodInvoker> AbstractNbtNumber_byteValue =
			Reflection.getOptionalMethod(AbstractNbtNumber.class, "method_10698", MethodType.methodType(byte.class));
	public byte nbte$byteValue() {
		return AbstractNbtNumber_byteValue.get().invoke(this);
	}
	
	private static final Supplier<Reflection.MethodInvoker> AbstractNbtNumber_shortValue =
			Reflection.getOptionalMethod(AbstractNbtNumber.class, "method_10696", MethodType.methodType(short.class));
	public short nbte$shortValue() {
		return AbstractNbtNumber_shortValue.get().invoke(this);
	}
	
	private static final Supplier<Reflection.MethodInvoker> AbstractNbtNumber_intValue =
			Reflection.getOptionalMethod(AbstractNbtNumber.class, "method_10701", MethodType.methodType(int.class));
	public int nbte$intValue() {
		return AbstractNbtNumber_intValue.get().invoke(this);
	}
	
	private static final Supplier<Reflection.MethodInvoker> AbstractNbtNumber_longValue =
			Reflection.getOptionalMethod(AbstractNbtNumber.class, "method_10699", MethodType.methodType(long.class));
	public long nbte$longValue() {
		return AbstractNbtNumber_longValue.get().invoke(this);
	}
	
	private static final Supplier<Reflection.MethodInvoker> AbstractNbtNumber_floatValue =
			Reflection.getOptionalMethod(AbstractNbtNumber.class, "method_10700", MethodType.methodType(float.class));
	public float nbte$floatValue() {
		return AbstractNbtNumber_floatValue.get().invoke(this);
	}
	
	private static final Supplier<Reflection.MethodInvoker> AbstractNbtNumber_doubleValue =
			Reflection.getOptionalMethod(AbstractNbtNumber.class, "method_10697", MethodType.methodType(double.class));
	public double nbte$doubleValue() {
		return AbstractNbtNumber_doubleValue.get().invoke(this);
	}
	
	private static final Supplier<Reflection.MethodInvoker> AbstractNbtNumber_numberValue =
			Reflection.getOptionalMethod(AbstractNbtNumber.class, "method_10702", MethodType.methodType(Number.class));
	public Number nbte$numberValue() {
		return AbstractNbtNumber_numberValue.get().invoke(this);
	}
	
}
