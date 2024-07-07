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
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;

public class AttributesNBTTagReference implements TagReference<List<AttributeData>, ItemStack> {
	
	@Override
	public List<AttributeData> get(ItemStack object) {
		if (!object.manager$hasNbt())
			return new ArrayList<>();
		NbtList attributesNbt = object.manager$getNbt().getList("AttributeModifiers", NbtElement.COMPOUND_TYPE);
		List<AttributeData> output = new ArrayList<>();
		for (NbtElement attributeNbtElement : attributesNbt) {
			NbtCompound attributeNbt = (NbtCompound) attributeNbtElement;
			EntityAttribute attribute = MVRegistry.ATTRIBUTE.get(new Identifier(attributeNbt.getString("AttributeName")));
			if (attribute == null)
				continue;
			if (!attributeNbt.contains("Operation", NbtElement.NUMBER_TYPE))
				continue;
			int operation = attributeNbt.getInt("Operation");
			if (operation < 0 || operation >= Operation.values().length)
				continue;
			if (!attributeNbt.contains("Amount", NbtElement.NUMBER_TYPE))
				continue;
			double value = attributeNbt.getDouble("Amount");
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
		}
		return output;
	}
	
	@Override
	public void set(ItemStack object, List<AttributeData> value) {
		if (value.isEmpty()) {
			if (object.manager$hasNbt())
				object.manager$modifyNbt(nbt -> nbt.remove("AttributeModifiers"));
			return;
		}
		NbtList output = new NbtList();
		for (AttributeData attribute : value) {
			NbtCompound attributeNbt = new NbtCompound();
			attributeNbt.putString("AttributeName", MVRegistry.ATTRIBUTE.getId(attribute.attribute()).toString());
			attributeNbt.putString("Name", attributeNbt.getString("AttributeName"));
			attributeNbt.putInt("Operation", attribute.modifierData().get().operation().ordinal());
			attributeNbt.putDouble("Amount", attribute.value());
			if (attribute.modifierData().get().slot() != Slot.ANY) {
				if (attribute.modifierData().get().slot().isOnlyForComponents())
					throw new IllegalArgumentException("The slot " + attribute.modifierData().get().slot() + " isn't available in this version of Minecraft!");
				attributeNbt.putString("Slot", attribute.modifierData().get().slot().name().toLowerCase());
			}
			attributeNbt.putUuid("UUID", attribute.modifierData().get().uuid());
			output.add(attributeNbt);
		}
		object.manager$modifyNbt(nbt -> nbt.put("AttributeModifiers", output));
	}
	
}
