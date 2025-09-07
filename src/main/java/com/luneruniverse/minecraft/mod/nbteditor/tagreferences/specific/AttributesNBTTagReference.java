package com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.IdentifierInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistry;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.MVNbtCompoundParent;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general.TagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.AttributeData;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.AttributeData.AttributeModifierData.AttributeModifierId;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.AttributeData.AttributeModifierData.Operation;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.AttributeData.AttributeModifierData.Slot;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

public class AttributesNBTTagReference implements TagReference<List<AttributeData>, NbtCompound> {
	
	public enum NBTLayout {
		/**
		 * 1.20.4-
		 */
		ITEM_OLD(true, "AttributeModifiers", "AttributeName", "Amount"),
		/**
		 * 1.20.6-
		 */
		ENTITY_OLD(false, "Attributes", "Name", "Base"),
		/**
		 * 1.21+
		 */
		ENTITY_NEW(false, "attributes", "id", "base");
		
		private final boolean modifiers;
		private final String attributeListTag;
		private final String attributeNameTag;
		private final String amountTag;
		
		private NBTLayout(boolean modifiers, String attributeListTag, String attributeNameTag, String amountTag) {
			this.modifiers = modifiers;
			this.attributeListTag = attributeListTag;
			this.attributeNameTag = attributeNameTag;
			this.amountTag = amountTag;
		}
		
		public boolean isModifiers() {
			return modifiers;
		}
		public String getAttributeListTag() {
			return attributeListTag;
		}
		public String getAttributeNameTag() {
			return attributeNameTag;
		}
		public String getAmountTag() {
			return amountTag;
		}
	}
	
	private final NBTLayout layout;
	
	public AttributesNBTTagReference(NBTLayout layout) {
		this.layout = layout;
	}
	
	@Override
	public List<AttributeData> get(NbtCompound object) {
		NbtList attributesNbt = object.nbte$getListOrDefault(layout.getAttributeListTag(), NbtElement.COMPOUND_TYPE);
		List<AttributeData> output = new ArrayList<>();
		for (NbtElement attributeNbtElement : attributesNbt.nbte$iterable()) {
			NbtCompound attributeNbt = (NbtCompound) attributeNbtElement;
			
			EntityAttribute attribute = attributeNbt.nbte$getString(layout.getAttributeNameTag())
					.map(IdentifierInst::of).map(MVRegistry.ATTRIBUTE::get).orElse(null);
			if (attribute == null)
				continue;
			
			if (!attributeNbt.nbte$contains(layout.getAmountTag(), MVNbtCompoundParent.NUMBER_TYPE))
				continue;
			double value = attributeNbt.nbte$getDoubleOrDefault(layout.getAmountTag());
			
			if (layout.isModifiers()) {
				if (!attributeNbt.nbte$contains("Operation", MVNbtCompoundParent.NUMBER_TYPE))
					continue;
				int operation = attributeNbt.nbte$getIntOrDefault("Operation");
				if (operation < 0 || operation >= Operation.values().length)
					continue;
				
				Slot slot = Slot.ANY;
				if (attributeNbt.nbte$contains("Slot", NbtElement.STRING_TYPE)) {
					try {
						slot = Slot.valueOf(attributeNbt.nbte$getStringOrDefault("Slot").toUpperCase());
					} catch (IllegalArgumentException e) {
						continue;
					}
					if (!slot.isInThisVersion())
						continue;
				}
				
				if (!attributeNbt.nbte$containsUuid("UUID"))
					continue;
				UUID uuid = attributeNbt.nbte$getUuid("UUID").get();
				
				output.add(new AttributeData(attribute, value, Operation.values()[operation], slot, new AttributeModifierId(uuid)));
			} else
				output.add(new AttributeData(attribute, value));
		}
		return output;
	}
	
	@Override
	public void set(NbtCompound object, List<AttributeData> value) {
		if (value.isEmpty()) {
			object.remove(layout.getAttributeListTag());
			return;
		}
		NbtList output = new NbtList();
		for (AttributeData attribute : value) {
			NbtCompound attributeNbt = new NbtCompound();
			
			attributeNbt.putString(layout.getAttributeNameTag(), MVRegistry.ATTRIBUTE.getId(attribute.attribute()).toString());
			attributeNbt.putDouble(layout.getAmountTag(), attribute.value());
			
			if (layout.isModifiers()) {
				attributeNbt.putString("Name", attributeNbt.nbte$getStringOrDefault("AttributeName"));
				attributeNbt.putInt("Operation", attribute.modifierData().get().operation().ordinal());
				if (attribute.modifierData().get().slot() != Slot.ANY) {
					if (!attribute.modifierData().get().slot().isInThisVersion())
						throw new IllegalArgumentException("The slot " + attribute.modifierData().get().slot() + " isn't available in this version of Minecraft!");
					attributeNbt.putString("Slot", attribute.modifierData().get().slot().name().toLowerCase());
				}
				attributeNbt.nbte$putUuid("UUID", attribute.modifierData().get().id().getUUID());
			}
			
			output.add(attributeNbt);
		}
		object.put(layout.getAttributeListTag(), output);
	}
	
}
