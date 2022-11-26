package com.luneruniverse.minecraft.mod.nbteditor.screens;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigCategory;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigItem;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigList;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigPanel;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigPath;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigValueDropdown;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigValueNumber;
import com.luneruniverse.minecraft.mod.nbteditor.util.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Items;
import net.minecraft.util.registry.Registry;

public class EnchantmentsScreen extends ItemEditorScreen {
	
	private static final Map<String, Enchantment> ENCHANTMENTS;
	private static final ConfigCategory ENCHANTMENT_ENTRY;
	static {
		ENCHANTMENTS = MultiVersionMisc.getEntrySet(Registry.ENCHANTMENT).stream().map(enchant -> Map.entry(enchant.getKey().getValue().toString(), enchant.getValue()))
				.sorted((a, b) -> a.getKey().compareToIgnoreCase(b.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
		String firstEnchant = ENCHANTMENTS.keySet().stream().findFirst().get();
		
		ENCHANTMENT_ENTRY = new ConfigCategory();
		ENCHANTMENT_ENTRY.setConfigurable("enchantment", new ConfigItem<>(TextInst.translatable("nbteditor.enchantments.enchantment"), new ConfigValueDropdown<>(
				firstEnchant, firstEnchant, new ArrayList<>(ENCHANTMENTS.keySet()))));
		ENCHANTMENT_ENTRY.setConfigurable("level", new ConfigItem<>(TextInst.translatable("nbteditor.enchantments.level"), ConfigValueNumber.forInt(1, 1, 1, 32767)));
	}
	@SuppressWarnings("unchecked")
	private static ConfigValueDropdown<String> getConfigEnchantment(ConfigCategory enchant) {
		return ((ConfigItem<ConfigValueDropdown<String>>) enchant.getConfigurable("enchantment")).getValue();
	}
	@SuppressWarnings("unchecked")
	private static ConfigValueNumber<Integer> getConfigLevel(ConfigCategory enchant) {
		return ((ConfigItem<ConfigValueNumber<Integer>>) enchant.getConfigurable("level")).getValue();
	}
	
	
	private final ConfigList config;
	private ConfigPanel panel;
	
	public EnchantmentsScreen(ItemReference ref) {
		super(TextInst.of("Enchantments"), ref);
		
		config = new ConfigList(TextInst.translatable("nbteditor.enchantments"), false, ENCHANTMENT_ENTRY);
		
		EnchantmentHelper.get(item).forEach((type, lvl) -> {
			ConfigCategory enchant = ENCHANTMENT_ENTRY.clone(true);
			getConfigEnchantment(enchant).setValue(Registry.ENCHANTMENT.getId(type).toString());
			getConfigLevel(enchant).setValue(lvl);
			config.addConfigurable(enchant);
		});
		
		config.addValueListener(source -> {
			Map<Enchantment, Integer> newEnchants = new LinkedHashMap<>();
			for (ConfigPath path : config.getConfigurables().values()) {
				ConfigCategory enchant = (ConfigCategory) path;
				newEnchants.put(ENCHANTMENTS.get(getConfigEnchantment(enchant).getValidValue()), getConfigLevel(enchant).getValidValue());
			}
			item.getOrCreateNbt().remove(item.isOf(Items.ENCHANTED_BOOK) ? EnchantedBookItem.STORED_ENCHANTMENTS_KEY : "Enchantments");
			EnchantmentHelper.set(newEnchants, item);
			checkSave();
		});
	}
	
	@Override
	protected void initEditor() {
		ConfigPanel newPanel = addDrawableChild(new ConfigPanel(16, 64, width - 32, height - 80, config));
		if (panel != null)
			newPanel.setScroll(panel.getScroll());
		panel = newPanel;
	}
	
	@Override
	protected void renderEditor(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		if (!ConfigScreen.isKeybindsHidden()) {
			int x = 16 + (32 + 8) * 2 + (100 + 8) * 2;
			MainUtil.drawWrappingString(matrices, textRenderer, TextInst.translatable("nbteditor.enchantments.tip").getString(),
					16 + (32 + 8) * 2 + (100 + 8) * 2, 16 + 6 + 10, width - x - 8, -1, false, true);
		}
	}
	
}
