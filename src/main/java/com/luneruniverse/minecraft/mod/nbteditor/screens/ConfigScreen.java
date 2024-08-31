package com.luneruniverse.minecraft.mod.nbteditor.screens;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.SystemUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.luneruniverse.minecraft.mod.nbteditor.NBTEditor;
import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.clientchest.LargeClientChest;
import com.luneruniverse.minecraft.mod.nbteditor.clientchest.SmallClientChest;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.EditableText;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVEnchantments;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVTooltip;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.ScreenTexts;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigButton;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigCategory;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigItem;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigPanel;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigTooltipSupplier;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigValueBoolean;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigValueDropdown;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigValueSlider;
import com.luneruniverse.minecraft.mod.nbteditor.screens.containers.ClientChestScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.text.Text;

public class ConfigScreen extends TickableSupportingScreen {
	
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
		public MVTooltip getTooltip() {
			List<Text> output = new ArrayList<>();
			for (int lvl = 1; lvl <= 3; lvl++)
				output.add(getEnchantNameWithMax(MVEnchantments.FIRE_ASPECT, lvl, this));
			return new MVTooltip(output);
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
		public MVTooltip getTooltip() {
			return new MVTooltip(desc);
		}
	}
	
	public static record Alias(String original, String alias) {}
	
	public enum ItemSizeFormat {
		HIDDEN("nbteditor.config.item_size.hidden", -1, false),
		AUTO("nbteditor.config.item_size.auto", 0, false),
		AUTO_COMPRESSED("nbteditor.config.item_size.auto_compressed", 0, true),
		BYTE("nbteditor.config.item_size.byte", 1, false),
		KILOBYTE("nbteditor.config.item_size.kilobyte", 1000, false),
		MEGABYTE("nbteditor.config.item_size.megabyte", 1000000, false),
		GIGABYTE("nbteditor.config.item_size.gigabyte", 1000000000, false),
		BYTE_COMPRESSED("nbteditor.config.item_size.byte_compressed", 1, true),
		KILOBYTE_COMPRESSED("nbteditor.config.item_size.kilobyte_compressed", 1000, true),
		MEGABYTE_COMPRESSED("nbteditor.config.item_size.megabyte_compressed", 1000000, true),
		GIGABYTE_COMPRESSED("nbteditor.config.item_size.gigabyte_compressed", 1000000000, true);
		
		private final Text label;
		private final int magnitude;
		private final boolean compressed;
		
		private ItemSizeFormat(String key, int magnitude, boolean compressed) {
			this.label = TextInst.translatable(key);
			this.magnitude = magnitude;
			this.compressed = compressed;
		}
		
		public int getMagnitude() {
			return magnitude;
		}
		public boolean isCompressed() {
			return compressed;
		}
		
		@Override
		public String toString() {
			return label.getString();
		}
	}
	
	public enum CreativeTabsPosition {
		BOTTOM_LEFT("nbteditor.config.creative_tabs_pos.bottom_left"),
		BOTTOM_CENTER("nbteditor.config.creative_tabs_pos.bottom_center"),
		BOTTOM_RIGHT("nbteditor.config.creative_tabs_pos.bottom_right"),
		TOP_LEFT("nbteditor.config.creative_tabs_pos.top_left"),
		TOP_CENTER("nbteditor.config.creative_tabs_pos.top_center"),
		TOP_RIGHT("nbteditor.config.creative_tabs_pos.top_right");
		
		private final Text label;
		
		private CreativeTabsPosition(String key) {
			this.label = TextInst.translatable(key);
		}
		
		public boolean isTop() {
			return this == TOP_LEFT || this == TOP_CENTER || this == TOP_RIGHT;
		}
		
		public Point position(int index, int numTabs, int screenWidth, int screenHeight) {
			int x = switch (this) {
				case BOTTOM_LEFT, TOP_LEFT -> index * (CreativeTab.WIDTH + 2) + 10;
				case BOTTOM_CENTER, TOP_CENTER -> {
					int tabsWidth = numTabs * (CreativeTab.WIDTH + 2) - 2;
					int tabsStart = (screenWidth - tabsWidth) / 2;
					yield tabsStart + index * (CreativeTab.WIDTH + 2);
				}
				case BOTTOM_RIGHT, TOP_RIGHT -> screenWidth - CreativeTab.WIDTH - index * (CreativeTab.WIDTH + 2) - 10;
			};
			
			int y = switch (this) {
				case BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT -> screenHeight - CreativeTab.HEIGHT;
				case TOP_LEFT, TOP_CENTER, TOP_RIGHT -> 0;
			};
			
			return new Point(x, y);
		}
		
		@Override
		public String toString() {
			return label.getString();
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
	private static boolean noSlotRestrictions;
	private static boolean hideFormatButtons;
	private static boolean specialNumbers;
	private static List<Alias> aliases;
	private static ItemSizeFormat itemSizeFormat;
	private static boolean invertedPageKeybinds;
	private static boolean triggerBlockUpdates;
	private static boolean warnIncompatibleProtocol;
	private static boolean enchantGlintFix;
	private static boolean recreateBlocksAndEntities;
	private static CreativeTabsPosition creativeTabsPos;
	
	public static void loadSettings() {
		enchantLevelMax = EnchantLevelMax.NEVER;
		enchantNumberTypeArabic = false;
		keyTextSize = 0.5;
		keybindsHidden = false;
		chatLimitExtended = false;
		singleQuotesAllowed = false;
		macScrollPatch = MinecraftClient.IS_SYSTEM_MAC;
		scrollSpeed = 5;
		airEditable = false;
		jsonText = false;
		shortcuts = new ArrayList<>();
		checkUpdates = CheckUpdatesLevel.MINOR;
		largeClientChest = false;
		screenshotOptions = true;
		tooltipOverflowFix = true;
		noSlotRestrictions = false;
		hideFormatButtons = false;
		specialNumbers = true;
		aliases = new ArrayList<>(List.of(
				new Alias("nbteditor", "nbt"),
				new Alias("clientchest", "chest"),
				new Alias("clientchest", "storage"),
				new Alias("factory signature", "sign")));
		itemSizeFormat = ItemSizeFormat.HIDDEN;
		invertedPageKeybinds = false;
		triggerBlockUpdates = true;
		warnIncompatibleProtocol = true;
		enchantGlintFix = false;
		recreateBlocksAndEntities = false;
		creativeTabsPos = CreativeTabsPosition.BOTTOM_LEFT;
		
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
			shortcuts = getStream(settings.get("shortcuts").getAsJsonArray())
					.map(cmd -> cmd.getAsString()).collect(Collectors.toList());
			JsonPrimitive checkUpdatesLegacy = settings.get("checkUpdates").getAsJsonPrimitive();
			checkUpdates = checkUpdatesLegacy.isBoolean() ?
					(checkUpdatesLegacy.getAsBoolean() ? CheckUpdatesLevel.MINOR : CheckUpdatesLevel.NONE)
					: CheckUpdatesLevel.valueOf(checkUpdatesLegacy.getAsString());
			largeClientChest = settings.get("largeClientChest").getAsBoolean();
			screenshotOptions = settings.get("screenshotOptions").getAsBoolean();
			tooltipOverflowFix = settings.get("tooltipOverflowFix").getAsBoolean();
			noSlotRestrictions = settings.get("noArmorRestriction").getAsBoolean();
			hideFormatButtons = settings.get("hideFormatButtons").getAsBoolean();
			specialNumbers = settings.get("specialNumbers").getAsBoolean();
			aliases = getStream(settings.get("aliases").getAsJsonArray())
					.map(alias -> new Alias(alias.getAsJsonObject().get("original").getAsString(),
							alias.getAsJsonObject().get("alias").getAsString())).collect(Collectors.toList());
			itemSizeFormat = ItemSizeFormat.valueOf(settings.get("itemSize").getAsString());
			invertedPageKeybinds = settings.get("invertedPageKeybinds").getAsBoolean();
			triggerBlockUpdates = settings.get("triggerBlockUpdates").getAsBoolean();
			warnIncompatibleProtocol = settings.get("warnIncompatibleProtocol").getAsBoolean();
			enchantGlintFix = settings.get("enchantGlintFix").getAsBoolean();
			recreateBlocksAndEntities = settings.get("recreateBlocksAndEntities").getAsBoolean();
			creativeTabsPos = CreativeTabsPosition.valueOf(settings.get("creativeTabsPos").getAsString());
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
		settings.addProperty("noArmorRestriction", noSlotRestrictions);
		settings.addProperty("hideFormatButtons", hideFormatButtons);
		settings.addProperty("specialNumbers", specialNumbers);
		settings.add("aliases", aliases.stream().map(alias -> {
			JsonObject obj = new JsonObject();
			obj.addProperty("original", alias.original);
			obj.addProperty("alias", alias.alias);
			return obj;
		}).collect(JsonArray::new, JsonArray::add, JsonArray::addAll));
		settings.addProperty("itemSize", itemSizeFormat.name());
		settings.addProperty("invertedPageKeybinds", invertedPageKeybinds);
		settings.addProperty("triggerBlockUpdates", triggerBlockUpdates);
		settings.addProperty("warnIncompatibleProtocol", warnIncompatibleProtocol);
		settings.addProperty("enchantGlintFix", enchantGlintFix);
		settings.addProperty("recreateBlocksAndEntities", recreateBlocksAndEntities);
		settings.addProperty("creativeTabsPos", creativeTabsPos.name());
		
		try {
			Files.write(new File(NBTEditorClient.SETTINGS_FOLDER, "settings.json").toPath(), new Gson().toJson(settings).getBytes());
		} catch (IOException e) {
			NBTEditor.LOGGER.error("Error while saving settings", e);
		}
	}
	// jsonArray.asList().stream() doesn't exist in 1.17
	private static Stream<JsonElement> getStream(JsonArray jsonArray) {
		return StreamSupport.stream(jsonArray.spliterator(), false);
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
		return MainUtil.client.interactionManager != null && !NBTEditorClient.SERVER_CONN.isEditingAllowed();
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
	public static boolean isNoSlotRestrictions() {
		return noSlotRestrictions;
	}
	public static boolean isHideFormatButtons() {
		return hideFormatButtons;
	}
	public static boolean isSpecialNumbers() {
		return specialNumbers;
	}
	public static List<Alias> getAliases() {
		return aliases;
	}
	public static ItemSizeFormat getItemSizeFormat() {
		return itemSizeFormat;
	}
	public static boolean isInvertedPageKeybinds() {
		return invertedPageKeybinds;
	}
	public static boolean isTriggerBlockUpdates() {
		return triggerBlockUpdates;
	}
	public static boolean isWarnIncompatibleProtocol() {
		return warnIncompatibleProtocol;
	}
	public static boolean isEnchantGlintFix() {
		return enchantGlintFix;
	}
	public static boolean isRecreateBlocksAndEntities() {
		return recreateBlocksAndEntities;
	}
	public static CreativeTabsPosition getCreativeTabsPos() {
		return creativeTabsPos;
	}
	
	private static EditableText getEnchantName(Enchantment enchant, int level) {
		EditableText output = TextInst.copy(MVEnchantments.getEnchantmentName(enchant));
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
		
		ConfigCategory mc = new ConfigCategory(TextInst.translatable("nbteditor.config.category.mc"));
		ConfigCategory guis = new ConfigCategory(TextInst.translatable("nbteditor.config.category.guis"));
		ConfigCategory functional = new ConfigCategory(TextInst.translatable("nbteditor.config.category.functional"));
		this.config.setConfigurable("mc", mc);
		this.config.setConfigurable("guis", guis);
		this.config.setConfigurable("functional", functional);
		
		
		// ---------- MC ----------
		
		mc.setConfigurable("extendChatLimit", new ConfigItem<>(TextInst.translatable("nbteditor.config.chat_limit"),
				new ConfigValueBoolean(chatLimitExtended, false, 100, TextInst.translatable("nbteditor.config.chat_limit.extended"), TextInst.translatable("nbteditor.config.chat_limit.normal"))
				.addValueListener(value -> chatLimitExtended = value.getValidValue()))
				.setTooltip("nbteditor.config.chat_limit.desc"));
		
		mc.setConfigurable("tooltipOverflowFix", new ConfigItem<>(TextInst.translatable("nbteditor.config.tooltip_overflow_fix"),
				new ConfigValueBoolean(tooltipOverflowFix, true, 100, TextInst.translatable("nbteditor.config.tooltip_overflow_fix.enabled"), TextInst.translatable("nbteditor.config.tooltip_overflow_fix.disabled"))
				.addValueListener(value -> tooltipOverflowFix = value.getValidValue()))
				.setTooltip("nbteditor.config.tooltip_overflow_fix.desc"));
		
		mc.setConfigurable("maxEnchantLevelDisplay", new ConfigItem<>(TextInst.translatable("nbteditor.config.enchant_level_max"),
				ConfigValueDropdown.forEnum(enchantLevelMax, EnchantLevelMax.NEVER, EnchantLevelMax.class)
				.addValueListener(value -> enchantLevelMax = value.getValidValue()))
				.setTooltip("nbteditor.config.enchant_level_max.desc"));
		
		mc.setConfigurable("useArabicEnchantLevels", new ConfigItem<>(TextInst.translatable("nbteditor.config.enchant_number_type"),
				new ConfigValueBoolean(enchantNumberTypeArabic, false, 100, TextInst.translatable("nbteditor.config.enchant_number_type.arabic"),
				TextInst.translatable("nbteditor.config.enchant_number_type.roman"), new MVTooltip(TextInst.translatable("nbteditor.config.enchant_number_type.desc2")))
				.addValueListener(value -> enchantNumberTypeArabic = value.getValidValue()))
				.setTooltip("nbteditor.config.enchant_number_type.desc"));
		
		mc.setConfigurable("noSlotRestrictions", new ConfigItem<>(TextInst.translatable("nbteditor.config.no_slot_restrictions"),
				new ConfigValueBoolean(noSlotRestrictions, false, 100, TextInst.translatable("nbteditor.config.no_slot_restrictions.enabled"), TextInst.translatable("nbteditor.config.no_slot_restrictions.disabled"))
				.addValueListener(value -> noSlotRestrictions = value.getValidValue()))
				.setTooltip("nbteditor.config.no_slot_restrictions.desc"));
		
		mc.setConfigurable("screenshotOptions", new ConfigItem<>(TextInst.translatable("nbteditor.config.screenshot_options"),
				new ConfigValueBoolean(screenshotOptions, true, 100, TextInst.translatable("nbteditor.config.screenshot_options.enabled"), TextInst.translatable("nbteditor.config.screenshot_options.disabled"))
				.addValueListener(value -> screenshotOptions = value.getValidValue()))
				.setTooltip(new MVTooltip(TextInst.translatable("nbteditor.config.screenshot_options.desc", TextInst.translatable("nbteditor.file_options.show"), TextInst.translatable("nbteditor.file_options.delete")))));
		
		mc.setConfigurable("enchantGlintFix", new ConfigItem<>(TextInst.translatable("nbteditor.config.enchant_glint_fix"),
				new ConfigValueBoolean(enchantGlintFix, false, 100, TextInst.translatable("nbteditor.config.enchant_glint_fix.enabled"), TextInst.translatable("nbteditor.config.enchant_glint_fix.disabled"))
				.addValueListener(value -> enchantGlintFix = value.getValidValue()))
				.setTooltip("nbteditor.config.enchant_glint_fix.desc"));
		
		// ---------- GUIs ----------
		
		guis.setConfigurable("creativeTabsPos", new ConfigItem<>(TextInst.translatable("nbteditor.config.creative_tabs_pos"),
				ConfigValueDropdown.forEnum(creativeTabsPos, CreativeTabsPosition.BOTTOM_LEFT, CreativeTabsPosition.class)
				.addValueListener(value -> creativeTabsPos = value.getValidValue()))
				.setTooltip("nbteditor.config.creative_tabs_pos.desc"));
		
		guis.setConfigurable("scrollSpeed", new ConfigItem<>(TextInst.translatable("nbteditor.config.scroll_speed"),
				ConfigValueSlider.forDouble(100, scrollSpeed, 5, 0.5, 10, 0.05, value -> TextInst.literal(String.format("%.2f", value)))
				.addValueListener(value -> scrollSpeed = value.getValidValue()))
				.setTooltip("nbteditor.config.scroll_speed.desc"));
		
		guis.setConfigurable("hideFormatButtons", new ConfigItem<>(TextInst.translatable("nbteditor.config.hide_format_buttons"),
				new ConfigValueBoolean(hideFormatButtons, false, 100, TextInst.translatable("nbteditor.config.hide_format_buttons.enabled"), TextInst.translatable("nbteditor.config.hide_format_buttons.disabled"))
				.addValueListener(value -> hideFormatButtons = value.getValidValue()))
				.setTooltip("nbteditor.config.hide_format_buttons.desc"));
		
		guis.setConfigurable("macScrollPatch", new ConfigItem<>(TextInst.translatable("nbteditor.config.mac_scroll_patch" + (SystemUtils.IS_OS_MAC ? ".on_mac" : "")),
				new ConfigValueBoolean(macScrollPatch, SystemUtils.IS_OS_MAC, 100, TextInst.translatable("nbteditor.config.mac_scroll_patch.enabled"), TextInst.translatable("nbteditor.config.mac_scroll_patch.disabled"))
				.addValueListener(value -> macScrollPatch = value.getValidValue()))
				.setTooltip("nbteditor.config.mac_scroll_patch.desc"));
		
		guis.setConfigurable("hideKeybinds", new ConfigItem<>(TextInst.translatable("nbteditor.config.keybinds"),
				new ConfigValueBoolean(keybindsHidden, false, 100, TextInst.translatable("nbteditor.config.keybinds.hidden"), TextInst.translatable("nbteditor.config.keybinds.shown"),
				new MVTooltip("nbteditor.keybind.edit", "nbteditor.keybind.factory", "nbteditor.keybind.container", "nbteditor.keybind.enchant"))
				.addValueListener(value -> keybindsHidden = value.getValidValue()))
				.setTooltip("nbteditor.config.keybinds.desc"));
		
		guis.setConfigurable("invertedPageKeybinds", new ConfigItem<>(TextInst.translatable("nbteditor.config.page_keybinds"),
				new ConfigValueBoolean(invertedPageKeybinds, false, 100, TextInst.translatable("nbteditor.config.page_keybinds.inverted"), TextInst.translatable("nbteditor.config.page_keybinds.normal"))
				.addValueListener(value -> invertedPageKeybinds = value.getValidValue()))
				.setTooltip("nbteditor.config.page_keybinds.desc"));
		
		guis.setConfigurable("itemSize", new ConfigItem<>(TextInst.translatable("nbteditor.config.item_size"),
				ConfigValueDropdown.forEnum(itemSizeFormat, ItemSizeFormat.HIDDEN, ItemSizeFormat.class)
				.addValueListener(value -> itemSizeFormat = value.getValidValue()))
				.setTooltip("nbteditor.config.item_size.desc"));
		
		guis.setConfigurable("keyTextSize", new ConfigItem<>(TextInst.translatable("nbteditor.config.key_text_size"),
				ConfigValueSlider.forDouble(100, keyTextSize, 0.5, 0.5, 1, 0.05, value -> TextInst.literal(String.format("%.2f", value)))
				.addValueListener(value -> keyTextSize = value.getValidValue()))
				.setTooltip("nbteditor.config.key_text_size.desc"));
		
		guis.setConfigurable("checkUpdates", new ConfigItem<>(TextInst.translatable("nbteditor.config.check_updates"),
				ConfigValueDropdown.forEnum(checkUpdates, CheckUpdatesLevel.MINOR, CheckUpdatesLevel.class)
				.addValueListener(value -> checkUpdates = value.getValidValue()))
				.setTooltip("nbteditor.config.check_updates.desc"));
		
		guis.setConfigurable("warnIncompatibleProtocol", new ConfigItem<>(TextInst.translatable("nbteditor.config.warn_incompatible_protocol"),
				new ConfigValueBoolean(warnIncompatibleProtocol, true, 100, TextInst.translatable("nbteditor.config.warn_incompatible_protocol.enabled"), TextInst.translatable("nbteditor.config.warn_incompatible_protocol.disabled"))
				.addValueListener(value -> warnIncompatibleProtocol = value.getValidValue()))
				.setTooltip("nbteditor.config.warn_incompatible_protocol.desc"));
		
		// ---------- FUNCTIONAL ----------
		
		functional.setConfigurable("aliases", new ConfigButton(100, TextInst.translatable("nbteditor.config.aliases"),
				btn -> client.setScreen(new AliasesScreen(this)), new MVTooltip("nbteditor.config.aliases.desc")));
		
		functional.setConfigurable("shortcuts", new ConfigButton(100, TextInst.translatable("nbteditor.config.shortcuts"),
				btn -> client.setScreen(new ShortcutsScreen(this)), new MVTooltip("nbteditor.config.shortcuts.desc")));
		
		functional.setConfigurable("recreateBlocksAndEntities", new ConfigItem<>(TextInst.translatable("nbteditor.config.recreate_blocks_and_entities"),
				new ConfigValueBoolean(recreateBlocksAndEntities, false, 100, TextInst.translatable("nbteditor.config.recreate_blocks_and_entities.enabled"), TextInst.translatable("nbteditor.config.recreate_blocks_and_entities.disabled"))
				.addValueListener(value -> recreateBlocksAndEntities = value.getValidValue()))
				.setTooltip("nbteditor.config.recreate_blocks_and_entities.desc"));
		
		functional.setConfigurable("largeClientChest", new ConfigItem<>(TextInst.translatable("nbteditor.config.client_chest_size"),
				new ConfigValueBoolean(largeClientChest, false, 100, TextInst.translatable("nbteditor.config.client_chest_size.large"), TextInst.translatable("nbteditor.config.client_chest_size.small"))
				.addValueListener(value -> largeClientChest = value.getValidValue()))
				.setTooltip("nbteditor.config.client_chest_size.desc"));
		
		functional.setConfigurable("airEditable", new ConfigItem<>(TextInst.translatable("nbteditor.config.air_editable"),
				new ConfigValueBoolean(airEditable, false, 100, TextInst.translatable("nbteditor.config.air_editable.yes"), TextInst.translatable("nbteditor.config.air_editable.no"))
				.addValueListener(value -> airEditable = value.getValidValue()))
				.setTooltip("nbteditor.config.air_editable.desc"));
		
		functional.setConfigurable("specialNumbers", new ConfigItem<>(TextInst.translatable("nbteditor.config.special_numbers"),
				new ConfigValueBoolean(specialNumbers, true, 100, TextInst.translatable("nbteditor.config.special_numbers.enabled"), TextInst.translatable("nbteditor.config.special_numbers.disabled"))
				.addValueListener(value -> specialNumbers = value.getValidValue()))
				.setTooltip("nbteditor.config.special_numbers.desc"));
		
		functional.setConfigurable("triggerBlockUpdates", new ConfigItem<>(TextInst.translatable("nbteditor.config.trigger_block_updates"),
				new ConfigValueBoolean(triggerBlockUpdates, true, 100, TextInst.translatable("nbteditor.config.trigger_block_updates.yes"), TextInst.translatable("nbteditor.config.trigger_block_updates.no"))
				.addValueListener(value -> triggerBlockUpdates = value.getValidValue()))
				.setTooltip("nbteditor.config.trigger_block_updates.desc"));
		
		functional.setConfigurable("jsonText", new ConfigItem<>(TextInst.translatable("nbteditor.config.json_text"),
				new ConfigValueBoolean(jsonText, false, 100, TextInst.translatable("nbteditor.config.json_text.yes"), TextInst.translatable("nbteditor.config.json_text.no"))
				.addValueListener(value -> jsonText = value.getValidValue()))
				.setTooltip("nbteditor.config.json_text.desc"));
		
		functional.setConfigurable("allowSingleQuotes", new ConfigItem<>(TextInst.translatable("nbteditor.config.single_quotes"),
				new ConfigValueBoolean(singleQuotesAllowed, false, 100, TextInst.translatable("nbteditor.config.single_quotes.allowed"),
				TextInst.translatable("nbteditor.config.single_quotes.not_allowed"), new MVTooltip("nbteditor.config.single_quotes.example"))
				.addValueListener(value -> singleQuotesAllowed = value.getValidValue()))
				.setTooltip("nbteditor.config.single_quotes.desc"));
		
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
		
		this.addDrawableChild(MVMisc.newButton(this.width - 134, this.height - 36, 100, 20, ScreenTexts.DONE, btn -> close()));
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
