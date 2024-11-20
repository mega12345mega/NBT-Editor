package com.luneruniverse.minecraft.mod.nbteditor.screens.factories;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.AttributeData.AttributeModifierData.AttributeModifierId;

import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;

public class AttributesScreen<L extends LocalNBT> extends LocalEditorScreen<L> {
	
	private static ConfigButton createExtremeAmountBtn(String key, boolean mostPositive, boolean infinity) {
		return new ConfigButton(30, TextInst.translatable(key), btn -> {
			ConfigCategory attribute = (ConfigCategory) btn.getParent().getParent();
			EntityAttribute type = ATTRIBUTES.get(getConfigAttribute(attribute).getValidValue());
			
			double min = Double.MIN_VALUE;
			double max = Double.MAX_VALUE;
			if (type instanceof ClampedEntityAttribute clamped) {
				min = clamped.getMinValue();
				max = clamped.getMaxValue();
			}
			
			double value;
			if (!infinity) {
				if (!mostPositive)
					value = min;
				else
					value = max;
			} else {
				if (!mostPositive)
					value = Double.NEGATIVE_INFINITY;
				else
					value = Double.POSITIVE_INFINITY;
			}
			getConfigAmount(attribute).setValue(value);
		}, new MVTooltip(key + ".desc"));
	}
	
	private static final Map<String, EntityAttribute> ATTRIBUTES;
	private static final ConfigHiddenDataNamed<ConfigCategory, AttributeModifierId> BASE_ATTRIBUTE_ENTRY;
	private static final ConfigHiddenDataNamed<ConfigCategory, AttributeModifierId> ATTRIBUTE_ENTRY;
	static {
		ATTRIBUTES = MVRegistry.ATTRIBUTE.getEntrySet().stream().map(attribute -> Map.entry(attribute.getKey().toString(), attribute.getValue()))
				.sorted((a, b) -> a.getKey().compareToIgnoreCase(b.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
		String firstAttribute = ATTRIBUTES.keySet().stream().findFirst().get();
		
		ConfigCategory visibleBase = new ConfigCategory();
		visibleBase.setConfigurable("attribute", new ConfigItem<>(TextInst.translatable("nbteditor.attributes.attribute"), ConfigValueDropdown.forList(
				firstAttribute, firstAttribute, new ArrayList<>(ATTRIBUTES.keySet()))));
		visibleBase.setConfigurable("amount", new ConfigBar()
				.setConfigurable("number", new ConfigItem<>(TextInst.translatable("nbteditor.attributes.base"),
						ConfigValueNumber.forDouble(0, 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)))
				.setConfigurable("max", createExtremeAmountBtn("nbteditor.attributes.amount.max", true, false))
				.setConfigurable("min", createExtremeAmountBtn("nbteditor.attributes.amount.min", false, false))
				.setConfigurable("infinity", createExtremeAmountBtn("nbteditor.attributes.amount.infinity", true, true))
				.setConfigurable("negative_infinity", createExtremeAmountBtn("nbteditor.attributes.amount.negative_infinity", false, true)));
		BASE_ATTRIBUTE_ENTRY = new ConfigHiddenDataNamed<>(visibleBase, AttributeModifierId.randomUUID(), (id, defaults) -> AttributeModifierId.randomUUID());
		
		ConfigCategory visible = new ConfigCategory();
		visible.setConfigurable("attribute", new ConfigItem<>(TextInst.translatable("nbteditor.attributes.attribute"), ConfigValueDropdown.forList(
				firstAttribute, firstAttribute, new ArrayList<>(ATTRIBUTES.keySet()))));
		visible.setConfigurable("operation", new ConfigItem<>(TextInst.translatable("nbteditor.attributes.operation"), ConfigValueDropdown.forEnum(
				AttributeModifierData.Operation.ADD, AttributeModifierData.Operation.ADD, AttributeModifierData.Operation.class)));
		visible.setConfigurable("amount", new ConfigBar()
				.setConfigurable("number", new ConfigItem<>(TextInst.translatable("nbteditor.attributes.amount"),
						ConfigValueNumber.forDouble(0, 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)))
				.setConfigurable("max", createExtremeAmountBtn("nbteditor.attributes.amount.max", true, false))
				.setConfigurable("min", createExtremeAmountBtn("nbteditor.attributes.amount.min", false, false))
				.setConfigurable("infinity", createExtremeAmountBtn("nbteditor.attributes.amount.infinity", true, true))
				.setConfigurable("negative_infinity", createExtremeAmountBtn("nbteditor.attributes.amount.negative_infinity", false, true)));
		visible.setConfigurable("slot", new ConfigItem<>(TextInst.translatable("nbteditor.attributes.slot"), ConfigValueDropdown.forFilteredEnum(
				AttributeModifierData.Slot.ANY, AttributeModifierData.Slot.ANY, AttributeModifierData.Slot.class, AttributeModifierData.Slot::isInThisVersion)));
		ATTRIBUTE_ENTRY = new ConfigHiddenDataNamed<>(visible, AttributeModifierId.randomUUID(), (id, defaults) -> AttributeModifierId.randomUUID());
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
		ConfigHiddenDataNamed<ConfigCategory, AttributeModifierId> entry =
				(modifiers ? ATTRIBUTE_ENTRY : BASE_ATTRIBUTE_ENTRY);
		List<AttributeData> attributes = (localNBT instanceof LocalItem localItem ?
				ItemTagReferences.ATTRIBUTES.get(localItem.getEditableItem()) :
				EntityTagReferences.ATTRIBUTES.get((LocalEntity) localNBT));
		
		this.attributes = new ConfigList(TextInst.translatable("nbteditor.attributes"), false, entry);
		for (AttributeData attribute : attributes) {
			ConfigHiddenDataNamed<ConfigCategory, AttributeModifierId> hiddenAttributeConfig = entry.clone(true);
			ConfigCategory attributeConfig = hiddenAttributeConfig.getVisible();
			
			getConfigAttribute(attributeConfig).setValue(MVRegistry.ATTRIBUTE.getId(attribute.attribute()).toString());
			getConfigAmount(attributeConfig).setValue(attribute.value());
			
			if (modifiers) {
				AttributeData.AttributeModifierData modifier = attribute.modifierData().get();
				getConfigOperation(attributeConfig).setValue(modifier.operation());
				getConfigSlot(attributeConfig).setValue(modifier.slot());
				hiddenAttributeConfig.setData(modifier.id());
			}
			
			this.attributes.addConfigurable(hiddenAttributeConfig);
		}
		
		this.attributes.addValueListener(source -> {
			List<AttributeData> newAttributes = new ArrayList<>();
			
			for (ConfigPath path : this.attributes.getConfigurables().values()) {
				ConfigHiddenDataNamed<ConfigCategory, AttributeModifierId> hiddenAttributeConfig =
						(ConfigHiddenDataNamed<ConfigCategory, AttributeModifierId>) path;
				ConfigCategory attributeConfig = hiddenAttributeConfig.getVisible();
				
				EntityAttribute attribute = ATTRIBUTES.get(getConfigAttribute(attributeConfig).getValidValue());
				double amount = getConfigAmount(attributeConfig).getValidValue();
				
				if (modifiers) {
					AttributeModifierData.Operation operation = getConfigOperation(attributeConfig).getValidValue();
					AttributeModifierData.Slot slot = getConfigSlot(attributeConfig).getValidValue();
					AttributeModifierId id = hiddenAttributeConfig.getData();
					newAttributes.add(new AttributeData(attribute, amount, operation, slot, id));
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
