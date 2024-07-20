package com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.NBTManagers;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.AttributeData.AttributeModifierData.Operation;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.AttributeData.AttributeModifierData.Slot;

import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.text.Text;

public record AttributeData(EntityAttribute attribute, double value, Optional<AttributeModifierData> modifierData) {
	
	public static record AttributeModifierData(Operation operation, Slot slot, UUID uuid) {
		
		public enum Operation {
			ADD("nbteditor.attributes.operation.add"),
			MULTIPLY_BASE("nbteditor.attributes.operation.multiply_base"),
			MULTIPLY("nbteditor.attributes.operation.multiply");
			
			public static Operation fromMinecraft(net.minecraft.entity.attribute.EntityAttributeModifier.Operation operation) {
				return switch (operation) {
					case ADD_VALUE -> ADD;
					case ADD_MULTIPLIED_BASE -> MULTIPLY_BASE;
					case ADD_MULTIPLIED_TOTAL -> MULTIPLY;
				};
			}
			
			private final Text name;
			private Operation(String key) {
				this.name = TextInst.translatable(key);
			}
			public net.minecraft.entity.attribute.EntityAttributeModifier.Operation toMinecraft() {
				return switch (this) {
					case ADD -> net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_VALUE;
					case MULTIPLY -> net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE;
					case MULTIPLY_BASE -> net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
				};
			}
			@Override
			public String toString() {
				return name.getString();
			}
		}
		
		public enum Slot {
			ANY("nbteditor.attributes.slot.any", false),
			HAND("nbteditor.attributes.slot.hand", true),
			MAINHAND("nbteditor.attributes.slot.mainhand", false),
			OFFHAND("nbteditor.attributes.slot.offhand", false),
			ARMOR("nbteditor.attributes.slot.armor", true),
			HEAD("nbteditor.attributes.slot.head", false),
			CHEST("nbteditor.attributes.slot.chest", false),
			LEGS("nbteditor.attributes.slot.legs", false),
			FEET("nbteditor.attributes.slot.feet", false),
			BODY("nbteditor.attributes.slot.body", true);
			
			public static Slot fromMinecraft(AttributeModifierSlot slot) {
				return switch (slot) {
					case ANY -> ANY;
					case HAND -> HAND;
					case MAINHAND -> MAINHAND;
					case OFFHAND -> OFFHAND;
					case ARMOR -> ARMOR;
					case HEAD -> HEAD;
					case CHEST -> CHEST;
					case LEGS -> LEGS;
					case FEET -> FEET;
					case BODY -> BODY;
				};
			}
			public static List<Slot> getNotOnlyForComponents() {
				return Arrays.stream(values()).filter(slot -> !slot.isOnlyForComponents()).toList();
			}
			
			private final Text name;
			private final boolean onlyForComponents;
			private Slot(String key, boolean onlyForComponents) {
				this.name = TextInst.translatable(key);
				this.onlyForComponents = onlyForComponents;
			}
			public AttributeModifierSlot toMinecraft() {
				return switch (this) {
					case ANY -> AttributeModifierSlot.ANY;
					case HAND -> AttributeModifierSlot.HAND;
					case MAINHAND -> AttributeModifierSlot.MAINHAND;
					case OFFHAND -> AttributeModifierSlot.OFFHAND;
					case ARMOR -> AttributeModifierSlot.ARMOR;
					case HEAD -> AttributeModifierSlot.HEAD;
					case CHEST -> AttributeModifierSlot.CHEST;
					case LEGS -> AttributeModifierSlot.LEGS;
					case FEET -> AttributeModifierSlot.FEET;
					case BODY -> AttributeModifierSlot.BODY;
				};
			}
			public boolean isOnlyForComponents() {
				return onlyForComponents;
			}
			public boolean isInThisVersion() {
				return !onlyForComponents || NBTManagers.COMPONENTS_EXIST;
			}
			@Override
			public String toString() {
				return name.getString();
			}
		}
		
	}
	
	public AttributeData(EntityAttribute attribute, double value) {
		this(attribute, value, Optional.empty());
	}
	public AttributeData(EntityAttribute attribute, double value, Operation operation, Slot slot, UUID uuid) {
		this(attribute, value, Optional.of(new AttributeModifierData(operation, slot, uuid)));
	}
	
}
