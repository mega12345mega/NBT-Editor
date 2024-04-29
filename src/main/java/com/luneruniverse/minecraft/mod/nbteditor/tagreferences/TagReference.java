package com.luneruniverse.minecraft.mod.nbteditor.tagreferences;

import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonParseException;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;

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

public class TagReference {
	
	protected final int[] version;
	private final Map<Field, String[]> refs;
	
	public TagReference(int[] version) {
		this.version = version;
		this.refs = new HashMap<>();
		
		Class<?> clazz = getClass();
		while (clazz != TagReference.class) {
			for (Field field : clazz.getDeclaredFields()) {
				RefersTo[] refersToOptions = field.getDeclaredAnnotationsByType(RefersTo.class);
				Version.VersionSwitch<RefersTo> refersToSwitch = Version.newSwitch(version);
				for (RefersTo refersToOption : refersToOptions) {
					String min = refersToOption.min();
					if (min.isEmpty())
						min = null;
					String max = refersToOption.max();
					if (max.isEmpty())
						max = null;
					refersToSwitch.range(min, max, refersToOption);
				}
				refersToSwitch.getOptionally().ifPresent(refersTo -> refs.put(field, refersTo.path().split("/")));
			}
			clazz = clazz.getSuperclass();
		}
		
		refs.keySet().forEach(field -> field.setAccessible(true));
	}
	
	public void load(NbtCompound nbt) {
		try {
			for (Map.Entry<Field, String[]> ref : refs.entrySet()) {
				NbtElement element = getFromNbt(nbt, ref.getValue());
				ref.getKey().set(this, deserialize(element, ref.getKey().getType()));
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException("Failed to load " + getClass().getName() + " from an NbtCompound", e);
		}
	}
	private Object deserialize(NbtElement element, Class<?> target) {
		if (target.isArray()) {
			if (!(element instanceof AbstractNbtList<?> list))
				return Array.newInstance(target.componentType(), 0);
			
			Object output = Array.newInstance(target.componentType(), list.size());
			for (int i = 0; i < list.size(); i++)
				Array.set(output, i, deserialize(list.get(i), target.componentType()));
			return output;
		}
		
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
				return (element instanceof NbtString str ? Text.Serialization.fromJson(str.value) : TextInst.of(""));
			} catch (JsonParseException e) {
				return TextInst.of("");
			}
		}
		
		throw new IllegalArgumentException("Cannot get " + target.getName() + " from nbt!");
	}
	
	public void save(NbtCompound nbt) {
		try {
			for (Map.Entry<Field, String[]> ref : refs.entrySet()) {
				Object value = ref.getKey().get(this);
				setToNbt(nbt, ref.getValue(), serialize(value));
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException("Failed to save " + getClass().getName() + " to an NbtCompound", e);
		}
	}
	private NbtElement serialize(Object value) {
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
			return NbtString.of(Text.Serialization.toJsonString((Text) value));
		
		throw new IllegalArgumentException("Cannot convert " + valueType.getName() + " to nbt!");
	}
	
	private NbtElement manageNbt(NbtCompound nbt, String[] path, NbtElement toWrite) {
		boolean writing = (toWrite != null);
		for (int i = 0; i < path.length - 1; i++) {
			NbtElement element = nbt.get(path[i]);
			if (element instanceof NbtCompound compound)
				nbt = compound;
			else if (writing) {
				NbtCompound compound = new NbtCompound();
				nbt.put(path[i], compound);
				nbt = compound;
			} else
				return null;
		}
		String finalKey = path[path.length - 1];
		if (writing) {
			nbt.put(finalKey, toWrite);
			return null;
		}
		return nbt.get(finalKey);
	}
	protected NbtElement getFromNbt(NbtCompound nbt, String[] path) {
		return manageNbt(nbt, path, null);
	}
	protected void setToNbt(NbtCompound nbt, String[] path, NbtElement value) {
		manageNbt(nbt, path, value);
	}
	
}
