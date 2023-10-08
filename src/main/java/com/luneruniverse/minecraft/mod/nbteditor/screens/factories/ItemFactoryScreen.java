package com.luneruniverse.minecraft.mod.nbteditor.screens.factories;

import com.luneruniverse.minecraft.mod.nbteditor.containers.ContainerIO;
import com.luneruniverse.minecraft.mod.nbteditor.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ItemEditorScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.NBTEditorScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigButton;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigCategory;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigPanel;
import com.luneruniverse.minecraft.mod.nbteditor.screens.containers.ContainerScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Items;
import net.minecraft.item.SignItem;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class ItemFactoryScreen extends ItemEditorScreen {

	public static final Identifier FACTORY_ICON = new Identifier("nbteditor", "textures/factory");
	public static final Identifier FACTORY_ICON_HOVERED = new Identifier("nbteditor", "textures/factory_hovered");
	
	public record ItemFactoryReference(Text buttonText, Predicate<ItemReference> supported, Consumer<ItemReference> factory) {}
	public static final List<ItemFactoryReference> BASIC_FACTORIES = new ArrayList<>();
	private static void addFactory(String key, Predicate<ItemReference> supported, Function<ItemReference, Screen> screen) {
		BASIC_FACTORIES.add(new ItemFactoryReference(TextInst.translatable(key), supported,
				ref -> MainUtil.client.setScreen(screen.apply(ref))));
	}
	private static void addFactory(String key, Function<ItemReference, Screen> screen) {
		addFactory(key, ref -> true, screen);
	}
	static {
		addFactory("nbteditor", NBTEditorScreen::new);
		BASIC_FACTORIES.add(new ItemFactoryReference(TextInst.translatable("nbteditor.container"), ref -> ContainerIO.isContainer(ref.getItem()), ContainerScreen::show));
		addFactory("nbteditor.book", ref -> ref.getItem().getItem() == Items.WRITTEN_BOOK, BookScreen::new);
		addFactory("nbteditor.display", DisplayScreen::new);
		addFactory("nbteditor.signboard", ref -> ref.getItem().getItem() instanceof SignItem, SignboardScreen::new);
		addFactory("nbteditor.enchantments", EnchantmentsScreen::new);
		addFactory("nbteditor.attributes", AttributesScreen::new);
		addFactory("nbteditor.block_states", ref -> ref.getItem().getItem() instanceof BlockItem, BlockStatesScreen::new);
	}
	
	private final ConfigCategory config;
	private ConfigPanel panel;
	
	public ItemFactoryScreen(ItemReference ref) {
		super(TextInst.of("Item Factories"), ref);
		this.config = new ConfigCategory();
		for (ItemFactoryReference factory : BASIC_FACTORIES) {
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
	protected FactoryLink getFactoryLink() {
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
