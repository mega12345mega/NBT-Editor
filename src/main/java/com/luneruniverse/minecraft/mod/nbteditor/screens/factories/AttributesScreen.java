package com.luneruniverse.minecraft.mod.nbteditor.screens.factories;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalEntity;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalItem;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalNBT;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistry;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVTooltip;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.NBTReference;
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
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigValueNumber;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.EntityTagReferences;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.ItemTagReferences;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.AttributeData;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.AttributeData.AttributeModifierData;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.text.Text;

public class AttributesScreen<L extends LocalNBT> extends LocalEditorScreen<L> {
	
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
	private static final ConfigHiddenDataNamed<ConfigCategory, UUID> BASE_ATTRIBUTE_ENTRY;
	private static final ConfigHiddenDataNamed<ConfigCategory, UUID> ATTRIBUTE_ENTRY;
	static {
		ATTRIBUTES = MVRegistry.ATTRIBUTE.getEntrySet().stream().map(attribute -> Map.entry(attribute.getKey().toString(), attribute.getValue()))
				.sorted((a, b) -> a.getKey().compareToIgnoreCase(b.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
		String firstAttribute = ATTRIBUTES.keySet().stream().findFirst().get();
		
		ConfigCategory visibleBase = new ConfigCategory();
		visibleBase.setConfigurable("attribute", new ConfigItem<>(TextInst.translatable("nbteditor.attributes.attribute"), ConfigValueDropdown.forList(
				firstAttribute, firstAttribute, new ArrayList<>(ATTRIBUTES.keySet()))));
		visibleBase.setConfigurable("amount", new ConfigBar().setConfigurable("number", new ConfigItem<>(TextInst.translatable("nbteditor.attributes.base"),
				ConfigValueNumber.forDouble(0, 0, -Double.MAX_VALUE, Double.MAX_VALUE))).setConfigurable("autofill", new MaxButton()));
		BASE_ATTRIBUTE_ENTRY = new ConfigHiddenDataNamed<>(visibleBase, UUID.randomUUID(), (uuid, defaults) -> UUID.randomUUID());
		
		ConfigCategory visible = new ConfigCategory();
		visible.setConfigurable("attribute", new ConfigItem<>(TextInst.translatable("nbteditor.attributes.attribute"), ConfigValueDropdown.forList(
				firstAttribute, firstAttribute, new ArrayList<>(ATTRIBUTES.keySet()))));
		visible.setConfigurable("operation", new ConfigItem<>(TextInst.translatable("nbteditor.attributes.operation"), ConfigValueDropdown.forEnum(
				AttributeModifierData.Operation.ADD, AttributeModifierData.Operation.ADD, AttributeModifierData.Operation.class)));
		visible.setConfigurable("amount", new ConfigBar().setConfigurable("number", new ConfigItem<>(TextInst.translatable("nbteditor.attributes.amount"),
				ConfigValueNumber.forDouble(0, 0, -Double.MAX_VALUE, Double.MAX_VALUE))).setConfigurable("autofill", new MaxButton()));
		visible.setConfigurable("slot", new ConfigItem<>(TextInst.translatable("nbteditor.attributes.slot"), ConfigValueDropdown.forFilteredEnum(
				AttributeModifierData.Slot.ANY, AttributeModifierData.Slot.ANY, AttributeModifierData.Slot.class, AttributeModifierData.Slot::isInThisVersion)));
		ATTRIBUTE_ENTRY = new ConfigHiddenDataNamed<>(visible, UUID.randomUUID(), (uuid, defaults) -> UUID.randomUUID());
	}
	@SuppressWarnings("unchecked")
	private static ConfigValueDropdown<String> getConfigAttribute(ConfigCategory attribute) {
		return ((ConfigItem<ConfigValueDropdown<String>>) attribute.getConfigurable("attribute")).getValue();
	}
	@SuppressWarnings("unchecked")
	private static ConfigValueDropdown<AttributeModifierData.Operation> getConfigOperation(ConfigCategory attribute) {
		return ((ConfigItem<ConfigValueDropdown<AttributeModifierData.Operation>>) attribute.getConfigurable("operation")).getValue();
	}
	@SuppressWarnings("unchecked")
	private static ConfigValueNumber<Double> getConfigAmount(ConfigCategory attribute) {
		return ((ConfigItem<ConfigValueNumber<Double>>) ((ConfigBar) attribute.getConfigurable("amount")).getConfigurable("number")).getValue();
	}
	@SuppressWarnings("unchecked")
	private static ConfigValueDropdown<AttributeModifierData.Slot> getConfigSlot(ConfigCategory attribute) {
		return ((ConfigItem<ConfigValueDropdown<AttributeModifierData.Slot>>) attribute.getConfigurable("slot")).getValue();
	}
	
	private final ConfigList attributes;
	private ConfigPanel panel;
	
	@SuppressWarnings("unchecked")
	public AttributesScreen(NBTReference<L> ref) {
		super(TextInst.of("Attributes"), ref);
		
		boolean modifiers = (ref instanceof ItemReference);
		ConfigHiddenDataNamed<ConfigCategory, UUID> entry = (modifiers ? ATTRIBUTE_ENTRY : BASE_ATTRIBUTE_ENTRY);
		List<AttributeData> attributes = (localNBT instanceof LocalItem localItem ?
				ItemTagReferences.ATTRIBUTES.get(localItem.getEditableItem()) :
				EntityTagReferences.ATTRIBUTES.get((LocalEntity) localNBT));
		
		this.attributes = new ConfigList(TextInst.translatable("nbteditor.attributes"), false, entry);
		for (AttributeData attribute : attributes) {
			ConfigHiddenDataNamed<ConfigCategory, UUID> hiddenAttributeConfig = entry.clone(true);
			ConfigCategory attributeConfig = hiddenAttributeConfig.getVisible();
			
			getConfigAttribute(attributeConfig).setValue(MVRegistry.ATTRIBUTE.getId(attribute.attribute()).toString());
			getConfigAmount(attributeConfig).setValue(attribute.value());
			
			if (modifiers) {
				AttributeData.AttributeModifierData modifier = attribute.modifierData().get();
				getConfigOperation(attributeConfig).setValue(modifier.operation());
				getConfigSlot(attributeConfig).setValue(modifier.slot());
				hiddenAttributeConfig.setData(modifier.uuid());
			}
			
			this.attributes.addConfigurable(hiddenAttributeConfig);
		}
		
		this.attributes.addValueListener(source -> {
			List<AttributeData> newAttributes = new ArrayList<>();
			
			for (ConfigPath path : this.attributes.getConfigurables().values()) {
				ConfigHiddenDataNamed<ConfigCategory, UUID> hiddenAttributeConfig =
						(ConfigHiddenDataNamed<ConfigCategory, UUID>) path;
				ConfigCategory attributeConfig = hiddenAttributeConfig.getVisible();
				
				EntityAttribute attribute = ATTRIBUTES.get(getConfigAttribute(attributeConfig).getValidValue());
				double amount = getConfigAmount(attributeConfig).getValidValue();
				
				if (modifiers) {
					AttributeModifierData.Operation operation = getConfigOperation(attributeConfig).getValidValue();
					AttributeModifierData.Slot slot = getConfigSlot(attributeConfig).getValidValue();
					UUID uuid = hiddenAttributeConfig.getData();
					newAttributes.add(new AttributeData(attribute, amount, operation, slot, uuid));
				} else
					newAttributes.add(new AttributeData(attribute, amount));
			}
			
			if (localNBT instanceof LocalItem localItem)
				ItemTagReferences.ATTRIBUTES.set(localItem.getEditableItem(), newAttributes);
			else
				EntityTagReferences.ATTRIBUTES.set((LocalEntity) localNBT, newAttributes);
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
