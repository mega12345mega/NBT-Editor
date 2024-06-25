package com.luneruniverse.minecraft.mod.nbteditor.screens.factories;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import com.luneruniverse.minecraft.mod.nbteditor.commands.OpenCommand;
import com.luneruniverse.minecraft.mod.nbteditor.commands.factories.AttributesCommand;
import com.luneruniverse.minecraft.mod.nbteditor.commands.factories.BlockStatesCommand;
import com.luneruniverse.minecraft.mod.nbteditor.commands.factories.SignboardCommand;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalNBT;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.NBTReference;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.screens.LocalEditorScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.NBTEditorScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigButton;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigCategory;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigPanel;
import com.luneruniverse.minecraft.mod.nbteditor.screens.containers.ContainerScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class LocalFactoryScreen<L extends LocalNBT> extends LocalEditorScreen<L> {
	
	public static final Identifier FACTORY_ICON = new Identifier("nbteditor", "textures/factory.png");
	
	public record LocalFactoryReference(Text buttonText, Predicate<NBTReference<?>> supported, Consumer<NBTReference<?>> factory) {}
	public static final List<LocalFactoryReference> BASIC_FACTORIES = new ArrayList<>();
	private static void addFactory(String key, Predicate<NBTReference<?>> supported, Function<NBTReference<?>, Screen> screen) {
		BASIC_FACTORIES.add(new LocalFactoryReference(TextInst.translatable(key), supported,
				ref -> MainUtil.client.setScreen(screen.apply(ref))));
	}
	private static <T extends NBTReference<?>> void addFactory(String key, Predicate<T> supported, Function<T, Screen> screen, Class<T> clazz) {
		addFactory(key, ref -> clazz.isInstance(ref) && supported.test(clazz.cast(ref)), ref -> screen.apply(clazz.cast(ref)));
	}
	private static void addFactory(String key, Function<NBTReference<?>, Screen> screen) {
		addFactory(key, ref -> true, screen);
	}
	private static <T extends NBTReference<?>> void addFactory(String key, Function<T, Screen> screen, Class<T> clazz) {
		addFactory(key, ref -> true, screen, clazz);
	}
	static {
		addFactory("nbteditor", ref -> new NBTEditorScreen<>(ref));
		BASIC_FACTORIES.add(new LocalFactoryReference(TextInst.translatable("nbteditor.container"),
				OpenCommand.CONTAINER_FILTER, ref -> ContainerScreen.show(ref)));
		addFactory("nbteditor.book", ref -> ref.getItem().getItem() == Items.WRITTEN_BOOK, BookScreen::new, ItemReference.class);
		addFactory("nbteditor.display", ref -> new DisplayScreen<>(ref));
		addFactory("nbteditor.signboard", SignboardCommand.SIGNBOARD_FILTER, ref -> new SignboardScreen<>(ref));
		addFactory("nbteditor.enchantments", EnchantmentsScreen::new, ItemReference.class);
		addFactory("nbteditor.attributes", AttributesCommand.ATTRIBUTES_FILTER, ref -> new AttributesScreen<>(ref));
		addFactory("nbteditor.block_states", BlockStatesCommand.BLOCK_FILTER, ref -> new BlockStatesScreen<>(ref));
	}
	
	private final ConfigCategory config;
	private ConfigPanel panel;
	
	public LocalFactoryScreen(NBTReference<L> ref) {
		super(TextInst.of("Factories"), ref);
		this.config = new ConfigCategory();
		for (LocalFactoryReference factory : BASIC_FACTORIES) {
			if (factory.supported().test(ref)) {
				this.config.setConfigurable(factory.buttonText().getString(), new ConfigButton(150, factory.buttonText(),
						btn -> factory.factory().accept(ref)));
			}
		}
	}
	
	@Override
	protected boolean isSaveRequried() {
		return false;
	}
	
	@Override
	protected FactoryLink<L> getFactoryLink() {
		return null;
	}
	
	@Override
	protected void initEditor() {
		ConfigPanel newPanel = addDrawableChild(new ConfigPanel(16, 64, width - 32, height - 80, config));
		if (panel != null)
			newPanel.setScroll(panel.getScroll());
		panel = newPanel;
	}
	
}
