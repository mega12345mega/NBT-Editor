package com.luneruniverse.minecraft.mod.nbteditor.screens;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionRegistry;
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
import net.minecraft.item.ItemStack;

public class EnchantmentsScreen extends ItemEditorScreen {
	
	private static final Map<String, Enchantment> ENCHANTMENTS;
	static {
		ENCHANTMENTS = MultiVersionRegistry.ENCHANTMENT.getEntrySet().stream().map(enchant -> Map.entry(enchant.getKey().toString(), enchant.getValue()))
				.sorted((a, b) -> a.getKey().compareToIgnoreCase(b.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
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
		
		ItemStack inputItem = ref.getItem();
		ConfigCategory entry = new ConfigCategory();
		List<String> orderedEnchants = ENCHANTMENTS.entrySet().stream()
				.map(enchant -> Map.entry(enchant.getKey(), enchant.getValue().isAcceptableItem(inputItem)))
				.sorted((a, b) -> {
					if (a.getValue()) {
						if (!b.getValue())
							return -1;
					} else if (b.getValue())
						return 1;
					return a.getKey().compareToIgnoreCase(b.getKey());
				})
				.map(Map.Entry::getKey).toList();
		String firstEnchant = orderedEnchants.get(0);
		entry.setConfigurable("enchantment", new ConfigItem<>(TextInst.translatable("nbteditor.enchantments.enchantment"),
				new ConfigValueDropdown<>(firstEnchant, firstEnchant, orderedEnchants,
				ENCHANTMENTS.entrySet().stream().filter(enchant -> enchant.getValue().isAcceptableItem(inputItem)).map(Map.Entry::getKey).toList())));
		entry.setConfigurable("level", new ConfigItem<>(TextInst.translatable("nbteditor.enchantments.level"), ConfigValueNumber.forInt(1, 1, 1, 32767)));
		config = new ConfigList(TextInst.translatable("nbteditor.enchantments"), false, entry);
		
		MainUtil.getEnchantments(item).forEach(enchant -> {
			ConfigCategory enchantConfig = entry.clone(true);
			getConfigEnchantment(enchantConfig).setValue(MultiVersionRegistry.ENCHANTMENT.getId(enchant.getKey()).toString());
			getConfigLevel(enchantConfig).setValue(enchant.getValue());
			config.addConfigurable(enchantConfig);
		});
		
		config.addValueListener(source -> {
			List<Map.Entry<Enchantment, Integer>> newEnchants = new ArrayList<>();
			for (ConfigPath path : config.getConfigurables().values()) {
				ConfigCategory enchant = (ConfigCategory) path;
				newEnchants.add(Map.entry(ENCHANTMENTS.get(getConfigEnchantment(enchant).getValidValue()), getConfigLevel(enchant).getValidValue()));
			}
			MainUtil.setEnchantments(item, newEnchants);
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
					16 + (32 + 8) * 2 + (100 + 8) * 2, 16 + 6 + 10, width - x - 8 - 20 - 8, -1, false, true);
		}
	}
	
}
