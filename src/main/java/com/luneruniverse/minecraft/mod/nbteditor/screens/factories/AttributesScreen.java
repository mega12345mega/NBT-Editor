package com.luneruniverse.minecraft.mod.nbteditor.screens.factories;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalItem;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistry;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVTooltip;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.screens.LocalEditorScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigBar;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigButton;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigCategory;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigHiddenDataNamed;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigItem;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigList;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigPanel;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigPath;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigValueDropdown;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigValueDropdownEnum;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigValueNumber;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;

public class AttributesScreen extends LocalEditorScreen<LocalItem, ItemReference> {
	
	private static class MaxButton extends ConfigButton {
		
		private static final Text MAX = TextInst.translatable("nbteditor.attributes.amount.max");
		private static final Text MIN = TextInst.translatable("nbteditor.attributes.amount.min");
		private static final Text INFINITY = TextInst.translatable("nbteditor.attributes.amount.infinity");
		private static final Text NEG_INFINITY = TextInst.translatable("nbteditor.attributes.amount.negative_infinity");
		private static final MVTooltip TOOLTIP = new MVTooltip("nbteditor.attributes.amount.autofill_keybinds");
		
		public MaxButton() {
			super(100, getMaxMsg(), btn -> {
				ConfigCategory attribute = (ConfigCategory) btn.getParent().getParent();
				EntityAttribute type = ATTRIBUTES.get(getConfigAttribute(attribute).getValidValue());
				
				double max = Double.MAX_VALUE;
				double min = Double.MIN_VALUE;
				if (type instanceof ClampedEntityAttribute clamped) {
					max = clamped.getMaxValue();
					min = clamped.getMinValue();
				}
				
				double value;
				boolean shift = Screen.hasShiftDown();
				boolean ctrl = Screen.hasControlDown();
				if (!shift) {
					if (!ctrl)
						value = max;
					else
						value = min;
				} else {
					if (!ctrl)
						value = Double.MAX_VALUE;
					else
						value = -Double.MAX_VALUE;
				}
				getConfigAmount(attribute).setValue(value);
			}, TOOLTIP);
		}
		private static Text getMaxMsg() {
			boolean shift = Screen.hasShiftDown();
			boolean ctrl = Screen.hasControlDown();
			if (!shift) {
				if (!ctrl)
					return MAX;
				else
					return MIN;
			} else {
				if (!ctrl)
					return INFINITY;
				else
					return NEG_INFINITY;
			}
		}
		
		@Override
		public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
			setMessage(getMaxMsg());
			super.render(matrices, mouseX, mouseY, delta);
		}
		
		@Override
		public MaxButton clone(boolean defaults) {
			return new MaxButton();
		}
		
	}
	
	private static final Map<String, EntityAttribute> ATTRIBUTES;
	private static final ConfigHiddenDataNamed<ConfigCategory, UUID> ATTRIBUTE_ENTRY;
	static {
		ATTRIBUTES = MVRegistry.ATTRIBUTE.getEntrySet().stream().map(attribute -> Map.entry(attribute.getKey().toString(), attribute.getValue()))
				.sorted((a, b) -> a.getKey().compareToIgnoreCase(b.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
		String firstAttribute = ATTRIBUTES.keySet().stream().findFirst().get();
		
		ConfigCategory visible = new ConfigCategory();
		visible.setConfigurable("attribute", new ConfigItem<>(TextInst.translatable("nbteditor.attributes.attribute"), new ConfigValueDropdown<>(
				firstAttribute, firstAttribute, new ArrayList<>(ATTRIBUTES.keySet()))));
		visible.setConfigurable("operation", new ConfigItem<>(TextInst.translatable("nbteditor.attributes.operation"), new ConfigValueDropdownEnum<>(
				Operation.ADD, Operation.ADD, Operation.class)));
		visible.setConfigurable("amount", new ConfigBar().setConfigurable("number", new ConfigItem<>(TextInst.translatable("nbteditor.attributes.amount"),
				ConfigValueNumber.forDouble(0, 0, -Double.MAX_VALUE, Double.MAX_VALUE))).setConfigurable("autofill", new MaxButton()));
		visible.setConfigurable("slot", new ConfigItem<>(TextInst.translatable("nbteditor.attributes.slot"), new ConfigValueDropdownEnum<>(
				Slot.ALL, Slot.ALL, Slot.class)));
		ATTRIBUTE_ENTRY = new ConfigHiddenDataNamed<>(visible, UUID.randomUUID(), (uuid, defaults) -> UUID.randomUUID());
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
	private static ConfigValueNumber<Double> getConfigAmount(ConfigCategory attribute) {
		return ((ConfigItem<ConfigValueNumber<Double>>) ((ConfigBar) attribute.getConfigurable("amount")).getConfigurable("number")).getValue();
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
	
	@SuppressWarnings("unchecked")
	public AttributesScreen(ItemReference ref) {
		super(TextInst.of("Item Attributes"), ref);
		
		NbtCompound nbt = localNBT.getOrCreateNBT();
		NbtList attributesNbt = nbt.getList("AttributeModifiers", NbtElement.COMPOUND_TYPE);
		this.attributes = new ConfigList(TextInst.translatable("nbteditor.attributes"), false, ATTRIBUTE_ENTRY);
		
		for (NbtElement element : attributesNbt) {
			NbtCompound attributeNbt = (NbtCompound) element;
			ConfigHiddenDataNamed<ConfigCategory, UUID> fullAttribute = ATTRIBUTE_ENTRY.clone(true);
			ConfigCategory attribute = fullAttribute.getVisible();
			
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
			
			if (!attributeNbt.containsUuid("UUID"))
				continue;
			fullAttribute.setData(attributeNbt.getUuid("UUID"));
			
			this.attributes.addConfigurable(fullAttribute);
		}
		
		this.attributes.addValueListener(source -> {
			attributesNbt.clear();
			for (ConfigPath path : this.attributes.getConfigurables().values()) {
				ConfigHiddenDataNamed<ConfigCategory, UUID> fullAttribute =
						(ConfigHiddenDataNamed<ConfigCategory, UUID>) path;
				ConfigCategory attribute = fullAttribute.getVisible();
				NbtCompound attributeNbt = new NbtCompound();
				attributeNbt.putUuid("UUID", fullAttribute.getData());
				attributeNbt.putString("AttributeName", getConfigAttribute(attribute).getValidValue());
				attributeNbt.putString("Name", attributeNbt.getString("AttributeName"));
				attributeNbt.putInt("Operation", getConfigOperation(attribute).getValidValue().ordinal());
				attributeNbt.putDouble("Amount", getConfigAmount(attribute).getValidValue());
				Slot slot = getConfigSlot(attribute).getValidValue();
				if (slot != Slot.ALL)
					attributeNbt.putString("Slot", slot.name().toLowerCase());
				attributesNbt.add(attributeNbt);
			}
			if (attributesNbt.isEmpty())
				nbt.remove("AttributeModifiers");
			else
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
	
}
