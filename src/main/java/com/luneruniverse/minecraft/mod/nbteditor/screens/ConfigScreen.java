package com.luneruniverse.minecraft.mod.nbteditor.screens;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.SystemUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.luneruniverse.minecraft.mod.nbteditor.NBTEditor;
import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.clientchest.LargeClientChest;
import com.luneruniverse.minecraft.mod.nbteditor.clientchest.SmallClientChest;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.EditableText;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionScreen;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionTooltip;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.ScreenTexts;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigButton;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigCategory;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigItem;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigPanel;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigTooltipSupplier;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigValueBoolean;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigValueDropdownEnum;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigValueSlider;
import com.luneruniverse.minecraft.mod.nbteditor.screens.containers.ClientChestScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ConfigScreen extends MultiVersionScreen {
	
	public enum EnchantLevelMax implements ConfigTooltipSupplier {
		NEVER("nbteditor.config.enchant_level_max.never", (level, maxLevel) -> false),
		NOT_MAXED_EXACT("nbteditor.config.enchant_level_max.not_exact", (level, maxLevel) -> level != maxLevel),
		NOT_MAXED("nbteditor.config.enchant_level_max.not_max", (level, maxLevel) -> level < maxLevel),
		ALWAYS("nbteditor.config.enchant_level_max.always", (level, maxLevel) -> true);
		
		private final Text label;
		private final BiFunction<Integer, Integer, Boolean> showMax;
		
		private EnchantLevelMax(String key, BiFunction<Integer, Integer, Boolean> showMax) {
			this.label = TextInst.translatable(key);
			this.showMax = showMax;
		}
		
		public boolean shouldShowMax(int level, int maxLevel) {
			return showMax.apply(level, maxLevel);
		}
		public EnchantLevelMax next() {
			return values()[(this.ordinal() + 1) % values().length];
		}
		
		@Override
		public String toString() {
			return label.getString();
		}
		@Override
		public MultiVersionTooltip getTooltip() {
			List<Text> output = new ArrayList<>();
			for (int lvl = 1; lvl <= 3; lvl++)
				output.add(getEnchantNameWithMax(Enchantments.FIRE_ASPECT, lvl, this));
			return new MultiVersionTooltip(output);
		}
	}
	
	public enum CheckUpdatesLevel implements ConfigTooltipSupplier {
		MINOR("nbteditor.config.check_updates.minor", 1),
		PATCH("nbteditor.config.check_updates.patch", 2),
		NONE("nbteditor.config.check_updates.none", -1);
		
		private final Text label;
		private final Text desc;
		private final int level;
		
		private CheckUpdatesLevel(String key, int level) {
			this.label = TextInst.translatable(key);
			this.desc = TextInst.translatable(key + ".desc");
			this.level = level;
		}
		
		public int getLevel() {
			return level;
		}
		
		@Override
		public String toString() {
			return label.getString();
		}
		@Override
		public MultiVersionTooltip getTooltip() {
			return new MultiVersionTooltip(desc);
		}
	}
	
	private static EnchantLevelMax enchantLevelMax;
	private static boolean enchantNumberTypeArabic;
	private static double keyTextSize;
	private static boolean keybindsHidden;
	private static boolean lockSlots; // Not shown in screen
	private static boolean chatLimitExtended;
	private static boolean singleQuotesAllowed;
	private static boolean macScrollPatch;
	private static double scrollSpeed;
	private static boolean airEditable;
	private static boolean jsonText;
	private static List<String> shortcuts;
	private static CheckUpdatesLevel checkUpdates;
	private static boolean largeClientChest;
	private static boolean screenshotOptions;
	private static boolean tooltipOverflowFix;
	private static boolean noArmorRestriction;
	private static boolean hideFormatButtons;
	
	public static void loadSettings() {
		enchantLevelMax = EnchantLevelMax.NEVER;
		enchantNumberTypeArabic = false;
		keyTextSize = 0.5;
		keybindsHidden = false;
		chatLimitExtended = false;
		singleQuotesAllowed = false;
		macScrollPatch = MinecraftClient.IS_SYSTEM_MAC;
		scrollSpeed = 1;
		airEditable = false;
		jsonText = false;
		shortcuts = new ArrayList<>();
		checkUpdates = CheckUpdatesLevel.MINOR;
		largeClientChest = false;
		screenshotOptions = true;
		tooltipOverflowFix = true;
		noArmorRestriction = false;
		hideFormatButtons = false;
		
		try {
			// Many config options use the old names
			// To avoid converting the config types, the old names are still used
			JsonObject settings = new Gson().fromJson(new String(Files.readAllBytes(new File(NBTEditorClient.SETTINGS_FOLDER, "settings.json").toPath())), JsonObject.class);
			enchantLevelMax = EnchantLevelMax.valueOf(settings.get("maxEnchantLevelDisplay").getAsString());
			enchantNumberTypeArabic = settings.get("useArabicEnchantLevels").getAsBoolean();
			keyTextSize = settings.get("keyTextSize").getAsDouble();
			keybindsHidden = settings.get("hideKeybinds").getAsBoolean();
			lockSlots = settings.get("lockSlots").getAsBoolean();
			chatLimitExtended = settings.get("extendChatLimit").getAsBoolean();
			singleQuotesAllowed = settings.get("allowSingleQuotes").getAsBoolean();
			macScrollPatch = !settings.get("keySkizzers").getAsBoolean();
			scrollSpeed = settings.get("scrollSpeed").getAsDouble();
			airEditable = settings.get("airEditable").getAsBoolean();
			jsonText = settings.get("jsonText").getAsBoolean();
			shortcuts = StreamSupport.stream(settings.get("shortcuts").getAsJsonArray().spliterator(), false)
					.map(cmd -> cmd.getAsString()).collect(Collectors.toList());
			JsonPrimitive checkUpdatesLegacy = settings.get("checkUpdates").getAsJsonPrimitive();
			checkUpdates = checkUpdatesLegacy.isBoolean() ?
					(checkUpdatesLegacy.getAsBoolean() ? CheckUpdatesLevel.MINOR : CheckUpdatesLevel.NONE)
					: CheckUpdatesLevel.valueOf(checkUpdatesLegacy.getAsString());
			largeClientChest = settings.get("largeClientChest").getAsBoolean();
			screenshotOptions = settings.get("screenshotOptions").getAsBoolean();
			tooltipOverflowFix = settings.get("tooltipOverflowFix").getAsBoolean();
			noArmorRestriction = settings.get("noArmorRestriction").getAsBoolean();
			hideFormatButtons = settings.get("hideFormatButtons").getAsBoolean();
		} catch (NoSuchFileException | ClassCastException | NullPointerException e) {
			NBTEditor.LOGGER.info("Missing some settings from settings.json, fixing ...");
			saveSettings();
		} catch (Exception e) {
			NBTEditor.LOGGER.error("Error while loading settings", e);
		}
	}
	private static void saveSettings() {
		JsonObject settings = new JsonObject();
		settings.addProperty("maxEnchantLevelDisplay", enchantLevelMax.name());
		settings.addProperty("useArabicEnchantLevels", enchantNumberTypeArabic);
		settings.addProperty("keyTextSize", keyTextSize);
		settings.addProperty("hideKeybinds", keybindsHidden);
		settings.addProperty("lockSlots", lockSlots);
		settings.addProperty("extendChatLimit", chatLimitExtended);
		settings.addProperty("allowSingleQuotes", singleQuotesAllowed);
		settings.addProperty("keySkizzers", !macScrollPatch);
		settings.addProperty("scrollSpeed", scrollSpeed);
		settings.addProperty("airEditable", airEditable);
		settings.addProperty("jsonText", jsonText);
		settings.add("shortcuts", shortcuts.stream().collect(JsonArray::new, JsonArray::add, JsonArray::addAll));
		settings.addProperty("checkUpdates", checkUpdates.name());
		settings.addProperty("largeClientChest", largeClientChest);
		settings.addProperty("screenshotOptions", screenshotOptions);
		settings.addProperty("tooltipOverflowFix", tooltipOverflowFix);
		settings.addProperty("noArmorRestriction", noArmorRestriction);
		settings.addProperty("hideFormatButtons", hideFormatButtons);
		
		try {
			Files.write(new File(NBTEditorClient.SETTINGS_FOLDER, "settings.json").toPath(), new Gson().toJson(settings).getBytes());
		} catch (IOException e) {
			NBTEditor.LOGGER.error("Error while saving settings", e);
		}
	}
	
	public static EnchantLevelMax getEnchantLevelMax() {
		return enchantLevelMax;
	}
	public static boolean isEnchantNumberTypeArabic() {
		return enchantNumberTypeArabic;
	}
	public static double getKeyTextSize() {
		return keyTextSize;
	}
	public static boolean isKeybindsHidden() {
		return keybindsHidden;
	}
	public static void setLockSlots(boolean lockSlots) {
		ConfigScreen.lockSlots = lockSlots;
		saveSettings();
	}
	public static boolean isLockSlots() {
		return lockSlots || isLockSlotsRequired();
	}
	public static boolean isLockSlotsRequired() {
		return MainUtil.client.interactionManager != null && !MainUtil.client.interactionManager.getCurrentGameMode().isCreative();
	}
	public static boolean isChatLimitExtended() {
		return chatLimitExtended;
	}
	public static boolean isSingleQuotesAllowed() {
		return singleQuotesAllowed;
	}
	public static boolean isMacScrollPatch() {
		return macScrollPatch;
	}
	public static double getScrollSpeed() {
		return scrollSpeed;
	}
	public static boolean isAirEditable() {
		return airEditable;
	}
	public static boolean isJsonText() {
		return jsonText;
	}
	public static List<String> getShortcuts() {
		return shortcuts;
	}
	public static CheckUpdatesLevel getCheckUpdates() {
		return checkUpdates;
	}
	public static boolean isLargeClientChest() {
		return largeClientChest;
	}
	public static boolean isScreenshotOptions() {
		return screenshotOptions;
	}
	public static boolean isTooltipOverflowFix() {
		return tooltipOverflowFix;
	}
	public static boolean isNoArmorRestriction() {
		return noArmorRestriction;
	}
	public static boolean isHideFormatButtons() {
		return hideFormatButtons;
	}
	
	private static EditableText getEnchantName(Enchantment enchant, int level) {
		EditableText output = TextInst.translatable(enchant.getTranslationKey());
        if (enchant.isCursed()) {
            output.formatted(Formatting.RED);
        } else {
            output.formatted(Formatting.GRAY);
        }
        if (level != 1 || enchant.getMaxLevel() != 1 || enchantLevelMax == EnchantLevelMax.ALWAYS) {
            output.append(" ");
            if (isEnchantNumberTypeArabic())
            	output.append("" + level);
            else
            	output.append(TextInst.translatable("enchantment.level." + level));
        }
        return output;
	}
	public static Text getEnchantNameWithMax(Enchantment enchant, int level, EnchantLevelMax display) {
		EditableText text = getEnchantName(enchant, level);
		if (display.shouldShowMax(level, enchant.getMaxLevel())) {
			text = text.append("/").append(
					ConfigScreen.isEnchantNumberTypeArabic() ?
							TextInst.of("" + enchant.getMaxLevel()) :
							TextInst.translatable("enchantment.level." + enchant.getMaxLevel()));
		}
		return text.getInternalValue(); // Allows Enchantment Descriptions to detect the enchantments
	}
	public static Text getEnchantNameWithMax(Enchantment enchant, int level) {
		return getEnchantNameWithMax(enchant, level, enchantLevelMax);
	}
	
	
	public static final List<Consumer<ConfigCategory>> ADDED_OPTIONS = new ArrayList<>();
	
	
	private final Screen parent;
	private final ConfigCategory config;
	private ConfigPanel panel;
	
	public ConfigScreen(Screen parent) {
		super(TextInst.translatable("nbteditor.config"));
		this.parent = parent;
		this.config = new ConfigCategory(TextInst.translatable("nbteditor.config"));
		this.config.setConfigurable("shortcuts", new ConfigButton(100, TextInst.translatable("nbteditor.config.shortcuts"),
				btn -> client.setScreen(new ShortcutsScreen(this)), new MultiVersionTooltip("nbteditor.config.shortcuts.desc")));
		this.config.setConfigurable("maxEnchantLevelDisplay", new ConfigItem<>(TextInst.translatable("nbteditor.config.enchant_level_max"),
				new ConfigValueDropdownEnum<>(enchantLevelMax, EnchantLevelMax.NEVER, EnchantLevelMax.class)
				.addValueListener(value -> enchantLevelMax = value.getValidValue()))
				.setTooltip("nbteditor.config.enchant_level_max.desc"));
		this.config.setConfigurable("useArabicEnchantLevels", new ConfigItem<>(TextInst.translatable("nbteditor.config.enchant_number_type"),
				new ConfigValueBoolean(enchantNumberTypeArabic, false, 100, TextInst.translatable("nbteditor.config.enchant_number_type.arabic"),
				TextInst.translatable("nbteditor.config.enchant_number_type.roman"), new MultiVersionTooltip(TextInst.translatable("nbteditor.config.enchant_number_type.desc2")))
				.addValueListener(value -> enchantNumberTypeArabic = value.getValidValue()))
				.setTooltip("nbteditor.config.enchant_number_type.desc"));
		this.config.setConfigurable("keyTextSize", new ConfigItem<>(TextInst.translatable("nbteditor.config.key_text_size"),
				ConfigValueSlider.forDouble(100, keyTextSize, 0.5, 0.5, 1, 0.05, value -> TextInst.literal(String.format("%.2f", value)))
				.addValueListener(value -> keyTextSize = value.getValidValue()))
				.setTooltip("nbteditor.config.key_text_size.desc"));
		this.config.setConfigurable("hideKeybinds", new ConfigItem<>(TextInst.translatable("nbteditor.config.keybinds"),
				new ConfigValueBoolean(keybindsHidden, false, 100, TextInst.translatable("nbteditor.config.keybinds.hidden"), TextInst.translatable("nbteditor.config.keybinds.shown"),
				new MultiVersionTooltip("nbteditor.keybind.edit", "nbteditor.keybind.item_factory", "nbteditor.keybind.container", "nbteditor.keybind.enchant"))
				.addValueListener(value -> keybindsHidden = value.getValidValue()))
				.setTooltip("nbteditor.config.keybinds.desc"));
		this.config.setConfigurable("extendChatLimit", new ConfigItem<>(TextInst.translatable("nbteditor.config.chat_limit"),
				new ConfigValueBoolean(chatLimitExtended, false, 100, TextInst.translatable("nbteditor.config.chat_limit.extended"), TextInst.translatable("nbteditor.config.chat_limit.normal"))
				.addValueListener(value -> chatLimitExtended = value.getValidValue()))
				.setTooltip("nbteditor.config.chat_limit.desc"));
		this.config.setConfigurable("allowSingleQuotes", new ConfigItem<>(TextInst.translatable("nbteditor.config.single_quotes"),
				new ConfigValueBoolean(singleQuotesAllowed, false, 100, TextInst.translatable("nbteditor.config.single_quotes.allowed"),
				TextInst.translatable("nbteditor.config.single_quotes.not_allowed"), new MultiVersionTooltip("nbteditor.config.single_quotes.example"))
				.addValueListener(value -> singleQuotesAllowed = value.getValidValue()))
				.setTooltip("nbteditor.config.single_quotes.desc"));
		this.config.setConfigurable("macScrollPatch", new ConfigItem<>(TextInst.translatable("nbteditor.config.mac_scroll_patch" + (SystemUtils.IS_OS_MAC ? ".on_mac" : "")),
				new ConfigValueBoolean(macScrollPatch, SystemUtils.IS_OS_MAC, 100, TextInst.translatable("nbteditor.config.mac_scroll_patch.enabled"), TextInst.translatable("nbteditor.config.mac_scroll_patch.disabled"))
				.addValueListener(value -> macScrollPatch = value.getValidValue()))
				.setTooltip("nbteditor.config.mac_scroll_patch.desc"));
		this.config.setConfigurable("scrollSpeed", new ConfigItem<>(TextInst.translatable("nbteditor.config.scroll_speed"),
				ConfigValueSlider.forDouble(100, scrollSpeed, 1, 0.5, 2, 0.05, value -> TextInst.literal(String.format("%.2f", value)))
				.addValueListener(value -> scrollSpeed = value.getValidValue()))
				.setTooltip("nbteditor.config.scroll_speed.desc"));
		this.config.setConfigurable("airEditable", new ConfigItem<>(TextInst.translatable("nbteditor.config.air_editable"),
				new ConfigValueBoolean(airEditable, false, 100, TextInst.translatable("nbteditor.config.air_editable.yes"), TextInst.translatable("nbteditor.config.air_editable.no"))
				.addValueListener(value -> airEditable = value.getValidValue()))
				.setTooltip("nbteditor.config.air_editable.desc"));
		this.config.setConfigurable("jsonText", new ConfigItem<>(TextInst.translatable("nbteditor.config.json_text"),
				new ConfigValueBoolean(jsonText, false, 100, TextInst.translatable("nbteditor.config.json_text.yes"), TextInst.translatable("nbteditor.config.json_text.no"))
				.addValueListener(value -> jsonText = value.getValidValue()))
				.setTooltip("nbteditor.config.json_text.desc"));
		this.config.setConfigurable("checkUpdates", new ConfigItem<>(TextInst.translatable("nbteditor.config.check_updates"),
				new ConfigValueDropdownEnum<>(checkUpdates, CheckUpdatesLevel.MINOR, CheckUpdatesLevel.class)
				.addValueListener(value -> checkUpdates = value.getValidValue()))
				.setTooltip("nbteditor.config.check_updates.desc"));
		this.config.setConfigurable("largeClientChest", new ConfigItem<>(TextInst.translatable("nbteditor.config.client_chest_size"),
				new ConfigValueBoolean(largeClientChest, false, 100, TextInst.translatable("nbteditor.config.client_chest_size.large"), TextInst.translatable("nbteditor.config.client_chest_size.small"))
				.addValueListener(value -> largeClientChest = value.getValidValue()))
				.setTooltip("nbteditor.config.client_chest_size.desc"));
		this.config.setConfigurable("screenshotOptions", new ConfigItem<>(TextInst.translatable("nbteditor.config.screenshot_options"),
				new ConfigValueBoolean(screenshotOptions, true, 100, TextInst.translatable("nbteditor.config.screenshot_options.enabled"), TextInst.translatable("nbteditor.config.screenshot_options.disabled"))
				.addValueListener(value -> screenshotOptions = value.getValidValue()))
				.setTooltip(new MultiVersionTooltip(TextInst.translatable("nbteditor.config.screenshot_options.desc", TextInst.translatable("nbteditor.file_options.show"), TextInst.translatable("nbteditor.file_options.delete")))));
		this.config.setConfigurable("tooltipOverflowFix", new ConfigItem<>(TextInst.translatable("nbteditor.config.tooltip_overflow_fix"),
				new ConfigValueBoolean(tooltipOverflowFix, true, 100, TextInst.translatable("nbteditor.config.tooltip_overflow_fix.enabled"), TextInst.translatable("nbteditor.config.tooltip_overflow_fix.disabled"))
				.addValueListener(value -> tooltipOverflowFix = value.getValidValue()))
				.setTooltip("nbteditor.config.tooltip_overflow_fix.desc"));
		this.config.setConfigurable("noArmorRestriction", new ConfigItem<>(TextInst.translatable("nbteditor.config.no_armor_restriction"),
				new ConfigValueBoolean(noArmorRestriction, false, 100, TextInst.translatable("nbteditor.config.no_armor_restriction.enabled"), TextInst.translatable("nbteditor.config.no_armor_restriction.disabled"))
				.addValueListener(value -> noArmorRestriction = value.getValidValue()))
				.setTooltip("nbteditor.config.no_armor_restriction.desc"));
		this.config.setConfigurable("hideFormatButtons", new ConfigItem<>(TextInst.translatable("nbteditor.config.hide_format_buttons"),
				new ConfigValueBoolean(hideFormatButtons, false, 100, TextInst.translatable("nbteditor.config.hide_format_buttons.enabled"), TextInst.translatable("nbteditor.config.hide_format_buttons.disabled"))
				.addValueListener(value -> hideFormatButtons = value.getValidValue()))
				.setTooltip("nbteditor.config.hide_format_buttons.desc"));
		ADDED_OPTIONS.forEach(option -> option.accept(config));
	}
	
	@Override
	protected void init() {
		ConfigPanel newPanel = addDrawableChild(new ConfigPanel(16, 16, width - 32, height - 32, config) {
			@Override
			protected boolean shouldScissor() {
				// If Mac Scroll Patch needs to be enabled, then the config menu would render incorrectly too
				// So always use scroll patch on config so the patch can be enabled without issues
				return false;
			}
		});
		if (panel != null)
			newPanel.setScroll(panel.getScroll());
		panel = newPanel;
		
		this.addDrawableChild(MultiVersionMisc.newButton(this.width - 134, this.height - 36, 100, 20, ScreenTexts.DONE, btn -> close()));
	}
	
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		super.renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
	}
	
	public void close() {
		client.setScreen(this.parent);
	}
	
	@Override
	public void removed() {
		saveSettings();
		if (largeClientChest != (NBTEditorClient.CLIENT_CHEST instanceof LargeClientChest)) {
			NBTEditorClient.CLIENT_CHEST = largeClientChest ? new LargeClientChest(5) : new SmallClientChest(100);
			ClientChestScreen.PAGE = Math.min(ClientChestScreen.PAGE, NBTEditorClient.CLIENT_CHEST.getPageCount() - 1);
			NBTEditorClient.CLIENT_CHEST.loadAync();
		}
	}
	
}
