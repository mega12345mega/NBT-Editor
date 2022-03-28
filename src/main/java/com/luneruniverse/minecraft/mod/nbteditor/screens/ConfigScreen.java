package com.luneruniverse.minecraft.mod.nbteditor.screens;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.List;
import java.util.function.BiFunction;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.luneruniverse.minecraft.mod.nbteditor.NBTEditor;
import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.Option;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

public class ConfigScreen extends GameOptionsScreen {
	
	public enum MaxEnchantLevelDisplay {
		NEVER("nbteditor.config.never_show_max_enchant_level", (level, maxLevel) -> false),
		NOT_MAXED_EXACT("nbteditor.config.not_maxed_exact_show_max_enchant_level", (level, maxLevel) -> level != maxLevel),
		NOT_MAXED("nbteditor.config.not_maxed_show_max_enchant_level", (level, maxLevel) -> level < maxLevel),
		ALWAYS("nbteditor.config.always_show_max_enchant_level", (level, maxLevel) -> true);
		
		private final TranslatableText label;
		private final BiFunction<Integer, Integer, Boolean> showMax;
		
		private MaxEnchantLevelDisplay(String key, BiFunction<Integer, Integer, Boolean> showMax) {
			label = new TranslatableText(key);
			this.showMax = showMax;
		}
		
		public boolean shouldShowMax(int level, int maxLevel) {
			return showMax.apply(level, maxLevel);
		}
		public MaxEnchantLevelDisplay next() {
			return values()[(this.ordinal() + 1) % values().length];
		}
	}
	
	private static MaxEnchantLevelDisplay maxEnchantLevelDisplay;
	private static boolean useArabicEnchantLevels;
	private static double keyTextSize;
	private static boolean hideKeybinds;
	private static boolean lockSlots; // Not shown in screen
	private static boolean extendChatLimit;
	private static boolean allowSingleQuotes;
	
	public static void loadSettings() {
		maxEnchantLevelDisplay = MaxEnchantLevelDisplay.NEVER;
		useArabicEnchantLevels = false;
		keyTextSize = 0.5;
		hideKeybinds = false;
		extendChatLimit = false;
		allowSingleQuotes = false;
		
		try {
			JsonObject settings = new Gson().fromJson(new String(Files.readAllBytes(new File(NBTEditorClient.SETTINGS_FOLDER, "settings.json").toPath())), JsonObject.class);
			maxEnchantLevelDisplay = MaxEnchantLevelDisplay.valueOf(settings.get("maxEnchantLevelDisplay").getAsString());
			useArabicEnchantLevels = settings.get("useArabicEnchantLevels").getAsBoolean();
			keyTextSize = settings.get("keyTextSize").getAsDouble();
			hideKeybinds = settings.get("hideKeybinds").getAsBoolean();
			lockSlots = settings.get("lockSlots").getAsBoolean();
			extendChatLimit = settings.get("extendChatLimit").getAsBoolean();
			allowSingleQuotes = settings.get("allowSingleQuotes").getAsBoolean();
		} catch (NoSuchFileException | ClassCastException | NullPointerException e) {
			NBTEditor.LOGGER.info("Missing some settings from settings.json, fixing ...");
			saveSettings();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private static void saveSettings() {
		JsonObject settings = new JsonObject();
		settings.addProperty("maxEnchantLevelDisplay", maxEnchantLevelDisplay.name());
		settings.addProperty("useArabicEnchantLevels", useArabicEnchantLevels);
		settings.addProperty("keyTextSize", keyTextSize);
		settings.addProperty("hideKeybinds", hideKeybinds);
		settings.addProperty("lockSlots", lockSlots);
		settings.addProperty("extendChatLimit", extendChatLimit);
		settings.addProperty("allowSingleQuotes", allowSingleQuotes);
		
		try {
			Files.write(new File(NBTEditorClient.SETTINGS_FOLDER, "settings.json").toPath(), new Gson().toJson(settings).getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static MaxEnchantLevelDisplay getMaxEnchantLevelDisplay() {
		return maxEnchantLevelDisplay;
	}
	public static boolean isUseArabicEnchantLevels() {
		return useArabicEnchantLevels;
	}
	public static Text getEnchantName(Enchantment enchant, int level) {
		if (maxEnchantLevelDisplay == null)
			return enchant.getName(level);
		
		TranslatableText mutableText = new TranslatableText(enchant.getTranslationKey());
        if (enchant.isCursed()) {
            mutableText.formatted(Formatting.RED);
        } else {
            mutableText.formatted(Formatting.GRAY);
        }
        if (level != 1 || enchant.getMaxLevel() != 1 || maxEnchantLevelDisplay == MaxEnchantLevelDisplay.ALWAYS) {
            mutableText.append(" ");
            if (isUseArabicEnchantLevels())
            	mutableText.append("" + level);
            else
            	mutableText.append(new TranslatableText("enchantment.level." + level));
        }
        return mutableText;
	}
	public static double getKeyTextSize() {
		return keyTextSize;
	}
	public static boolean shouldHideKeybinds() {
		return hideKeybinds;
	}
	public static void setLockSlots(boolean lockSlots) {
		ConfigScreen.lockSlots = lockSlots;
		saveSettings();
	}
	public static boolean shouldLockSlots() {
		return lockSlots || shouldDisableLockSlotsButton();
	}
	public static boolean shouldDisableLockSlotsButton() {
		return MainUtil.client.interactionManager != null && !MainUtil.client.interactionManager.getCurrentGameMode().isCreative();
	}
	public static boolean shouldExtendChatLimit() {
		return extendChatLimit;
	}
	public static boolean shouldAllowSingleQuotes() {
		return allowSingleQuotes;
	}
	
	
	
	private ButtonListWidget list;
	
	public ConfigScreen(Screen parent) {
		super(parent, MainUtil.client.options, new TranslatableText("nbteditor.config"));
	}
	
	@Override
	protected void init() {
		this.list = new ButtonListWidget(this.client, this.width, this.height, 32, this.height - 32, 25);
		this.list.addAll(new Option[] {
				new Option("nbteditor.config.max_enchant_level") {
					@Override
					public ClickableWidget createButton(GameOptions options, int x, int y, int width) {
						return new ButtonWidget(x, y, width, 20, maxEnchantLevelDisplay.label, btn -> {
							maxEnchantLevelDisplay = maxEnchantLevelDisplay.next();
							btn.setMessage(maxEnchantLevelDisplay.label);
						}, new SimpleTooltip(ConfigScreen.this, "nbteditor.config.max_enchant_level"));
					}
				},
				new Option("nbteditor.config.number_system_enchant_levels") {
					@Override
					public ClickableWidget createButton(GameOptions options, int x, int y, int width) {
						return new ButtonWidget(x, y, width, 20, useArabicEnchantLevels ? new TranslatableText("nbteditor.config.use_roman_enchant_levels") : new TranslatableText("nbteditor.config.use_arabic_enchant_levels"), btn -> {
							useArabicEnchantLevels = !useArabicEnchantLevels;
							btn.setMessage(useArabicEnchantLevels ? new TranslatableText("nbteditor.config.use_roman_enchant_levels") : new TranslatableText("nbteditor.config.use_arabic_enchant_levels"));
						}, new SimpleTooltip(ConfigScreen.this, "nbteditor.config.number_system_enchant_levels"));
					}
				},
				new Option("nbteditor.config.size_key_text") {
					@Override
					public ClickableWidget createButton(GameOptions options, int x, int y, int width) {
						return new SliderWidget(x, y, width, 20, new TranslatableText("nbteditor.config.key_text_size", keyTextSize), (keyTextSize - 0.5) * 2) {
							@Override
							protected void updateMessage() {
								this.setMessage(new TranslatableText("nbteditor.config.key_text_size", keyTextSize));
							}
							@Override
							protected void applyValue() {
								keyTextSize = Math.floor(value * 10) / 10 / 2 + 0.5;
							}
						};
					}
				},
				new Option("nbteditor.config.keybinds") {
					@Override
					public ClickableWidget createButton(GameOptions options, int x, int y, int width) {
						return new ButtonWidget(x, y, width, 20, hideKeybinds ? new TranslatableText("nbteditor.config.show_keybinds") : new TranslatableText("nbteditor.config.hide_keybinds"), btn -> {
							hideKeybinds = !hideKeybinds;
							btn.setMessage(hideKeybinds ? new TranslatableText("nbteditor.config.show_keybinds") : new TranslatableText("nbteditor.config.hide_keybinds"));
						}, new SimpleTooltip(ConfigScreen.this, "nbteditor.config.keybinds"));
					}
				},
				new Option("nbteditor.config.chat_limit") {
					@Override
					public ClickableWidget createButton(GameOptions options, int x, int y, int width) {
						return new ButtonWidget(x, y, width, 20, extendChatLimit ? new TranslatableText("nbteditor.config.normal_chat_limit") : new TranslatableText("nbteditor.config.extend_chat_limit"), btn -> {
							extendChatLimit = !extendChatLimit;
							btn.setMessage(extendChatLimit ? new TranslatableText("nbteditor.config.normal_chat_limit") : new TranslatableText("nbteditor.config.extend_chat_limit"));
						}, new SimpleTooltip(ConfigScreen.this, "nbteditor.config.chat_limit"));
					}
				},
				new Option("nbteditor.config.single_quotes") {
					@Override
					public ClickableWidget createButton(GameOptions options, int x, int y, int width) {
						return new ButtonWidget(x, y, width, 20, allowSingleQuotes ? new TranslatableText("nbteditor.config.disallow_single_quotes") : new TranslatableText("nbteditor.config.allow_single_quotes"), btn -> {
							allowSingleQuotes = !allowSingleQuotes;
							btn.setMessage(allowSingleQuotes ? new TranslatableText("nbteditor.config.disallow_single_quotes") : new TranslatableText("nbteditor.config.allow_single_quotes"));
						}, new SimpleTooltip(ConfigScreen.this, "nbteditor.config.single_quotes"));
					}
				}
			});
		this.addSelectableChild(this.list);
		this.addDrawableChild(
				new ButtonWidget(this.width / 2 - 100, this.height - 27, 200, 20, ScreenTexts.DONE, (button) -> {
					onClose();
				}));
	}
	
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		this.list.render(matrices, mouseX, mouseY, delta);
		drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 5, 0xffffff);
		super.render(matrices, mouseX, mouseY, delta);
		List<OrderedText> list = getHoveredButtonTooltip(this.list, mouseX, mouseY);
		if (list != null) {
			this.renderOrderedTooltip(matrices, list, mouseX, mouseY);
		}
	}
	
	@Override
	public void removed() {
		saveSettings();
	}
	
}
