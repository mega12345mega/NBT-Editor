package com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistry;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general.TagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.AttributeData;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.AttributeData.AttributeModifierData.Operation;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.AttributeData.AttributeModifierData.Slot;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;

public class AttributesNBTTagReference implements TagReference<List<AttributeData>, NbtCompound> {
	
	private final boolean modifiers;
	private final String attributeListTag;
	private final String attributeNameTag;
	private final String amountTag;
	
	public AttributesNBTTagReference(boolean modifiers) {
		this.modifiers = modifiers;
		if (modifiers) {
			attributeListTag = "AttributeModifiers";
			attributeNameTag = "AttributeName";
			amountTag = "Amount";
		} else {
			attributeListTag = "Attributes";
			attributeNameTag = "Name";
			amountTag = "Base";
		}
	}
	
	@Override
	public List<AttributeData> get(NbtCompound object) {
		NbtList attributesNbt = object.getList(attributeListTag, NbtElement.COMPOUND_TYPE);
		List<AttributeData> output = new ArrayList<>();
		for (NbtElement attributeNbtElement : attributesNbt) {
			NbtCompound attributeNbt = (NbtCompound) attributeNbtElement;
			
			EntityAttribute attribute = MVRegistry.ATTRIBUTE.get(new Identifier(attributeNbt.getString(attributeNameTag)));
			if (attribute == null)
				continue;
			
			if (!attributeNbt.contains(amountTag, NbtElement.NUMBER_TYPE))
				continue;
			double value = attributeNbt.getDouble(amountTag);
			
			if (modifiers) {
				if (!attributeNbt.contains("Operation", NbtElement.NUMBER_TYPE))
					continue;
				int operation = attributeNbt.getInt("Operation");
				if (operation < 0 || operation >= Operation.values().length)
					continue;
				
				Slot slot = Slot.ANY;
				if (attributeNbt.contains("Slot", NbtElement.STRING_TYPE)) {
					try {
						slot = Slot.valueOf(attributeNbt.getString("Slot").toUpperCase());
					} catch (IllegalArgumentException e) {
						continue;
					}
					if (slot.isOnlyForComponents())
						continue;
				}
				
				if (!attributeNbt.containsUuid("UUID"))
					continue;
				UUID uuid = attributeNbt.getUuid("UUID");
				
				output.add(new AttributeData(attribute, value, Operation.values()[operation], slot, uuid));
			} else
				output.add(new AttributeData(attribute, value));
		}
		return output;
	}
	
	@Override
	public void set(NbtCompound object, List<AttributeData> value) {
		if (value.isEmpty()) {
			object.remove(attributeListTag);
			return;
		}
		NbtList output = new NbtList();
		for (AttributeData attribute : value) {
			NbtCompound attributeNbt = new NbtCompound();
			
			attributeNbt.putString(attributeNameTag, MVRegistry.ATTRIBUTE.getId(attribute.attribute()).toString());
			attributeNbt.putDouble(amountTag, attribute.value());
			
			if (modifiers) {
				attributeNbt.putString("Name", attributeNbt.getString("AttributeName"));
				attributeNbt.putInt("Operation", attribute.modifierData().get().operation().ordinal());
				if (attribute.modifierData().get().slot() != Slot.ANY) {
					if (attribute.modifierData().get().slot().isOnlyForComponents())
						throw new IllegalArgumentException("The slot " + attribute.modifierData().get().slot() + " isn't available in this version of Minecraft!");
					attributeNbt.putString("Slot", attribute.modifierData().get().slot().name().toLowerCase());
				}
				attributeNbt.putUuid("UUID", attribute.modifierData().get().uuid());
			}
			
			output.add(attributeNbt);
		}
		object.put(attributeListTag, output);
	}
	
}
