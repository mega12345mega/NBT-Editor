package com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general;

import java.lang.invoke.MethodType;
import java.lang.reflect.Array;

import com.google.gson.JsonParseException;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.util.TextUtil;

import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.nbt.NbtShort;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;

public class NBTTagReference<T> implements TagReference<T, NbtCompound> {
	
	private static Object deserialize(NbtElement element, Class<?> target) {
		if (target.isArray()) {
			if (!(element instanceof AbstractNbtList<?> list))
				return Array.newInstance(target.componentType(), 0);
			
			Object output = Array.newInstance(target.componentType(), list.size());
			for (int i = 0; i < list.size(); i++)
				Array.set(output, i, deserialize(list.get(i), target.componentType()));
			return output;
		}
		
		if (target.isAssignableFrom(NbtElement.class))
			return element.copy();
		if (target.isAssignableFrom(NbtCompound.class))
			return (element instanceof NbtCompound compound ? compound.copy() : new NbtCompound());
		if (target.isAssignableFrom(NbtList.class))
			return (element instanceof NbtList list ? list.copy() : new NbtList());
		
		Class<?> primitiveTarget = (target.isPrimitive() ? target : MethodType.methodType(target).unwrap().returnType());
		if (primitiveTarget.isPrimitive()) {
			if (primitiveTarget == boolean.class)
				return (element instanceof AbstractNbtNumber num ? num.byteValue() != 0 : false);
			if (primitiveTarget == byte.class)
				return (element instanceof AbstractNbtNumber num ? num.byteValue() : (byte) 0);
			if (primitiveTarget == short.class)
				return (element instanceof AbstractNbtNumber num ? num.shortValue() : (short) 0);
			if (primitiveTarget == char.class)
				return (element instanceof AbstractNbtNumber num ? (char) num.shortValue() : (char) 0);
			if (primitiveTarget == int.class)
				return (element instanceof AbstractNbtNumber num ? num.intValue() : (int) 0);
			if (primitiveTarget == long.class)
				return (element instanceof AbstractNbtNumber num ? num.longValue() : (long) 0);
			if (primitiveTarget == float.class)
				return (element instanceof AbstractNbtNumber num ? num.floatValue() : (float) 0);
			if (primitiveTarget == double.class)
				return (element instanceof AbstractNbtNumber num ? num.doubleValue() : (double) 0);
			throw new IllegalArgumentException("Unknown primitive type " + primitiveTarget.getName());
		}
		
		if (target.isAssignableFrom(String.class))
			return (element instanceof NbtString str ? str.value : "");
		
		if (target.isAssignableFrom(Text.class)) {
			try {
				return (element instanceof NbtString str ? TextUtil.fromJsonSafely(str.value) : TextInst.of(""));
			} catch (JsonParseException e) {
				return TextInst.of("");
			}
		}
		
		throw new IllegalArgumentException("Cannot get " + target.getName() + " from nbt!");
	}
	
	private static NbtElement serialize(Object value) {
		if (value == null)
			throw new IllegalArgumentException("Cannot convert null to nbt!");
		
		Class<?> valueType = value.getClass();
		
		if (valueType.isArray()) {
			Class<?> compType = valueType.componentType();
			if (compType.isPrimitive()) {
				if (compType == byte.class)
					return new NbtByteArray((byte[]) value);
				if (compType == int.class)
					return new NbtIntArray((int[]) value);
				if (compType == long.class)
					return new NbtLongArray((long[]) value);
			}
			
			NbtList output = new NbtList();
			int length = Array.getLength(value);
			for (int i = 0; i < length; i++)
				output.add(serialize(Array.get(value, i)));
			return output;
		}
		
		if (NbtElement.class.isAssignableFrom(valueType))
			return ((NbtElement) value).copy();
		
		Class<?> primitiveValueType = (valueType.isPrimitive() ? valueType : MethodType.methodType(valueType).unwrap().returnType());
		if (primitiveValueType.isPrimitive()) {
			if (primitiveValueType == boolean.class)
				return NbtByte.of((boolean) value);
			if (primitiveValueType == byte.class)
				return NbtByte.of((byte) value);
			if (primitiveValueType == short.class)
				return NbtShort.of((short) value);
			if (primitiveValueType == char.class)
				return NbtShort.of((short) (char) value);
			if (primitiveValueType == int.class)
				return NbtInt.of((int) value);
			if (primitiveValueType == long.class)
				return NbtLong.of((long) value);
			if (primitiveValueType == float.class)
				return NbtFloat.of((float) value);
			if (primitiveValueType == double.class)
				return NbtDouble.of((double) value);
			throw new IllegalArgumentException("Unknown primitive type " + primitiveValueType.getName());
		}
		
		if (CharSequence.class.isAssignableFrom(valueType))
			return NbtString.of(((CharSequence) value).toString());
		
		if (Text.class.isAssignableFrom(valueType))
			return NbtString.of(TextInst.toJsonString((Text) value));
		
		throw new IllegalArgumentException("Cannot convert " + valueType.getName() + " to nbt!");
	}
	
	private static NbtElement manageNbt(NbtCompound nbt, String[] path, boolean write, NbtElement toWrite) {
		for (int i = 0; i < path.length - 1; i++) {
			NbtElement element = nbt.get(path[i]);
			if (element instanceof NbtCompound compound)
				nbt = compound;
			else if (write) {
				NbtCompound compound = new NbtCompound();
				nbt.put(path[i], compound);
				nbt = compound;
			} else
				return null;
		}
		String finalKey = path[path.length - 1];
		if (write) {
			if (toWrite == null)
				nbt.remove(finalKey);
			else
				nbt.put(finalKey, toWrite);
			return null;
		}
		return nbt.get(finalKey);
	}
	private static NbtElement getFromNbt(NbtCompound nbt, String[] path) {
		return manageNbt(nbt, path, false, null);
	}
	private static void setToNbt(NbtCompound nbt, String[] path, NbtElement value) {
		manageNbt(nbt, path, true, value);
	}
	private static void removeFromNbt(NbtCompound nbt, String[] path) {
		manageNbt(nbt, path, true, null);
	}
	
	private final Class<T> clazz;
	private final String[] path;
	
	public NBTTagReference(Class<T> clazz, String path) {
		this.clazz = clazz;
		this.path = path.split("/");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public T get(NbtCompound object) {
		return (T) deserialize(object == null ? null : getFromNbt(object, path), clazz);
	}
	
	@Override
	public void set(NbtCompound object, T value) {
		if (value == null) {
			removeFromNbt(object, path);
			return;
		}
		setToNbt(object, path, serialize(value));
	}
	
}
