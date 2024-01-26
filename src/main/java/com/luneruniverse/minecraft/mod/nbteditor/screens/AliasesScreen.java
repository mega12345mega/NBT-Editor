package com.luneruniverse.minecraft.mod.nbteditor.screens;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommandGroup;
import com.luneruniverse.minecraft.mod.nbteditor.commands.CommandHandler;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVTooltip;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.ScreenTexts;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen.Alias;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigBar;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigItem;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigList;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigPanel;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigPath;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigValueText;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;

public class AliasesScreen extends TickableSupportingScreen {
	
	private static final ConfigBar ALIAS_ENTRY;
	static {
		ALIAS_ENTRY = new ConfigBar();
		ALIAS_ENTRY.setConfigurable("original", new ConfigItem<>(TextInst.of(""), new ConfigValueText(200, "", "")));
		ALIAS_ENTRY.setConfigurable("alias", new ConfigItem<>(TextInst.of(""), new ConfigValueText(100, "", "")));
	}
	@SuppressWarnings("unchecked")
	private static ConfigValueText getConfigOriginal(ConfigBar enchant) {
		return ((ConfigItem<ConfigValueText>) enchant.getConfigurable("original")).getValue();
	}
	@SuppressWarnings("unchecked")
	private static ConfigValueText getConfigAlias(ConfigBar enchant) {
		return ((ConfigItem<ConfigValueText>) enchant.getConfigurable("alias")).getValue();
	}
	
	private final Screen parent;
	private final ConfigList config;
	private ConfigPanel panel;
	private boolean cancel;
	
	public AliasesScreen(Screen parent) {
		super(TextInst.translatable("nbteditor.config.aliases"));
		this.parent = parent;
		this.config = new ConfigList(TextInst.translatable("nbteditor.config.aliases").append(" - ")
				.append(TextInst.translatable("nbteditor.config.aliases.example")), false, ALIAS_ENTRY);
		for (ConfigScreen.Alias alias : ConfigScreen.getAliases()) {
			ConfigBar entry = ALIAS_ENTRY.clone(true);
			getConfigOriginal(entry).setValue(alias.original());
			getConfigAlias(entry).setValue(alias.alias());
			this.config.addConfigurable(entry);
		}
	}
	
	private void addExtremeAliases(Collection<ClientCommand> commands, String path) {
		for (ClientCommand command : commands) {
			if (command.getExtremeAlias() != null) {
				ConfigBar entry = ALIAS_ENTRY.clone(true);
				getConfigOriginal(entry).setValue(path + command.getName());
				getConfigAlias(entry).setValue(command.getExtremeAlias());
				this.config.addConfigurable(entry);
			}
			
			if (command instanceof ClientCommandGroup group)
				addExtremeAliases(group.getChildren(), path + command.getName() + " ");
		}
	}
	
	@Override
	protected void init() {
		ConfigPanel newPanel = addDrawableChild(new ConfigPanel(16, 16, width - 32, height - 32, config));
		if (panel != null)
			newPanel.setScroll(panel.getScroll());
		panel = newPanel;
		
		this.addDrawableChild(MVMisc.newButton(this.width - 134, this.height - 36, 100, 20, ScreenTexts.DONE, btn -> close()));
		this.addDrawableChild(MVMisc.newButton(this.width - 134, this.height - 36 - 24, 100, 20, ScreenTexts.CANCEL, btn -> {
			cancel = true;
			close();
		}));
		this.addDrawableChild(MVMisc.newButton(this.width - 134, this.height - 36 - 24 * 2, 100, 20,
				TextInst.translatable("nbteditor.config.aliases.extreme"), btn -> addExtremeAliases(CommandHandler.COMMANDS.values(), ""),
				new MVTooltip("nbteditor.config.aliases.extreme.desc")));
	}
	
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
	}
	
	public void close() {
		client.setScreen(this.parent);
	}
	
	@Override
	public void removed() {
		if (cancel)
			return;
		Map<String, Alias> newAliases = new LinkedHashMap<>();
		for (ConfigPath path : this.config.getConfigurables().values()) {
			ConfigBar entry = (ConfigBar) path;
			String original = getConfigOriginal(entry).getValidValue();
			String alias = getConfigAlias(entry).getValidValue();
			newAliases.put(original.substring(0, original.lastIndexOf(' ') + 1) + alias, new Alias(original, alias));
		}
		if (new HashSet<>(ConfigScreen.getAliases()).equals(new HashSet<>(newAliases.values())))
			return;
		ConfigScreen.getAliases().clear();
		ConfigScreen.getAliases().addAll(newAliases.values());
		ClientCommandManager.reregisterClientCommands();
	}
	
}
