package com.luneruniverse.minecraft.mod.nbteditor.screens;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigCategory;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigItem;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigList;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigPanel;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigPath;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigValueDouble;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigValueDropdown;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigValueDropdownEnum;
import com.luneruniverse.minecraft.mod.nbteditor.util.ItemReference;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.registry.Registry;

public class AttributesScreen extends ItemEditorScreen {
	
	private static final Map<String, EntityAttribute> ATTRIBUTES;
	private static final ConfigCategory ATTRIBUTE_ENTRY;
	static {
		ATTRIBUTES = Registry.ATTRIBUTE.getEntrySet().stream().map(attribute -> Map.entry(attribute.getKey().getValue().toString(), attribute.getValue()))
				.sorted((a, b) -> a.getKey().compareToIgnoreCase(b.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
		String firstAttribute = ATTRIBUTES.keySet().stream().findFirst().get();
		
		ATTRIBUTE_ENTRY = new ConfigCategory();
		ATTRIBUTE_ENTRY.setConfigurable("attribute", new ConfigItem<>(Text.translatable("nbteditor.attributes.attribute"), new ConfigValueDropdown<>(
				firstAttribute, firstAttribute, new ArrayList<>(ATTRIBUTES.keySet()))));
		ATTRIBUTE_ENTRY.setConfigurable("operation", new ConfigItem<>(Text.translatable("nbteditor.attributes.operation"), new ConfigValueDropdownEnum<>(
				Operation.ADD, Operation.ADD, Operation.class)));
		ATTRIBUTE_ENTRY.setConfigurable("amount", new ConfigItem<>(Text.translatable("nbteditor.attributes.amount"), new ConfigValueDouble(
				0, 0, -Double.MAX_VALUE, Double.MAX_VALUE)));
		ATTRIBUTE_ENTRY.setConfigurable("slot", new ConfigItem<>(Text.translatable("nbteditor.attributes.slot"), new ConfigValueDropdownEnum<>(
				Slot.ALL, Slot.ALL, Slot.class)));
	}
	@SuppressWarnings("unchecked")
	private static ConfigValueDropdown<String> getConfigAttribute(ConfigCategory attribute) {
		return ((ConfigItem<ConfigValueDropdown<String>>) attribute.getConfigurable("attribute")).getValue();
	}
	@SuppressWarnings("unchecked")
	private static ConfigValueDropdown<Operation> getConfigOperation(ConfigCategory attribute) {
		return ((ConfigItem<ConfigValueDropdown<Operation>>) attribute.getConfigurable("operation")).getValue();
	}
	@SuppressWarnings("unchecked")
	private static ConfigValueDouble getConfigAmount(ConfigCategory attribute) {
		return ((ConfigItem<ConfigValueDouble>) attribute.getConfigurable("amount")).getValue();
	}
	@SuppressWarnings("unchecked")
	private static ConfigValueDropdown<Slot> getConfigSlot(ConfigCategory attribute) {
		return ((ConfigItem<ConfigValueDropdown<Slot>>) attribute.getConfigurable("slot")).getValue();
	}
	
	private enum Operation {
		ADD("Add"),
		MULTIPLY_BASE("Multiply Base"),
		MULTIPLY("Multiply");
		
		private final String name;
		private Operation(String name) {
			this.name = name;
		}
		@Override
		public String toString() {
			return name;
		}
	}
	private enum Slot {
		ALL("All"),
		MAINHAND("Main Hand"),
		OFFHAND("Off Hand"),
		HEAD("Head"),
		CHEST("Chest"),
		LEGS("Legs"),
		FEET("Feet");
		
		private final String name;
		private Slot(String name) {
			this.name = name;
		}
		@Override
		public String toString() {
			return name;
		}
	}
	
	private final ConfigList attributes;
	private ConfigPanel panel;
	
	public AttributesScreen(ItemReference ref) {
		super(Text.of("Item Attributes"), ref);
		
		NbtCompound nbt = item.getOrCreateNbt();
		NbtList attributesNbt = nbt.getList("AttributeModifiers", NbtType.COMPOUND);
		this.attributes = new ConfigList(Text.translatable("nbteditor.attributes"), false, ATTRIBUTE_ENTRY);
		
		for (NbtElement element : attributesNbt) {
			NbtCompound attributeNbt = (NbtCompound) element;
			ConfigCategory attribute = ATTRIBUTE_ENTRY.clone(true);
			
			String attributeName = attributeNbt.getString("AttributeName");
			if (!attributeName.contains(":"))
				attributeName = "minecraft:" + attributeName;
			if (!ATTRIBUTES.containsKey(attributeName))
				continue;
			getConfigAttribute(attribute).setValue(attributeName);
			
			int operation = attributeNbt.getInt("Operation");
			if (operation < 0 || operation >= Operation.values().length)
				continue;
			getConfigOperation(attribute).setValue(Operation.values()[operation]);
			
			getConfigAmount(attribute).setValue(attributeNbt.getDouble("Amount"));
			
			String slotStr = attributeNbt.getString("Slot");
			Slot slot = Slot.ALL;
			if (!slotStr.isEmpty()) {
				try {
					slot = Slot.valueOf(slotStr.toUpperCase());
				} catch (IllegalArgumentException e) {
					// Invalid slot
				}
			}
			getConfigSlot(attribute).setValue(slot);
			
			this.attributes.addConfigurable(attribute);
		}
		
		this.attributes.addValueListener(source -> {
			attributesNbt.clear();
			for (ConfigPath path : this.attributes.getConfigurables().values()) {
				ConfigCategory attribute = (ConfigCategory) path;
				NbtCompound attributeNbt = new NbtCompound();
				attributeNbt.putUuid("UUID", UUID.randomUUID());
				attributeNbt.putString("AttributeName", getConfigAttribute(attribute).getValidValue());
				attributeNbt.putString("Name", attributeNbt.getString("AttributeName"));
				attributeNbt.putInt("Operation", getConfigOperation(attribute).getValidValue().ordinal());
				attributeNbt.putDouble("Amount", getConfigAmount(attribute).getValidValue());
				Slot slot = getConfigSlot(attribute).getValidValue();
				if (slot != Slot.ALL)
					attributeNbt.putString("Slot", slot.name().toLowerCase());
				attributesNbt.add(attributeNbt);
			}
			nbt.put("AttributeModifiers", attributesNbt);
			checkSave();
		});
	}
	
	@Override
	protected void initEditor() {
		ConfigPanel newPanel = addDrawableChild(new ConfigPanel(16, 64, width - 32, height - 80, attributes));
		if (panel != null)
			newPanel.setScroll(panel.getScroll());
		panel = newPanel;
	}
	
	@Override
	protected void checkSave() {
		ItemStack item = this.item;
		ItemStack savedItem = this.savedItem;
		try {
			// Compare the items without the UUIDs, as they are randomized every edit
			this.item = removeAttributeUUIDs(item);
			this.savedItem = removeAttributeUUIDs(savedItem);
			super.checkSave();
		} finally {
			this.item = item;
			this.savedItem = savedItem;
		}
	}
	private ItemStack removeAttributeUUIDs(ItemStack item) {
		item = item.copy();
		if (!item.hasNbt())
			return item;
		NbtList attributes = item.getNbt().getList("AttributeModifiers", NbtType.COMPOUND);
		for (NbtElement attribute : attributes)
			((NbtCompound) attribute).remove("UUID");
		return item;
	}
	
}
