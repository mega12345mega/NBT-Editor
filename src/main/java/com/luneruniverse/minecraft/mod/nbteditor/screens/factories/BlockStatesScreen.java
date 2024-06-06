package com.luneruniverse.minecraft.mod.nbteditor.screens.factories;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalBlock;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalItem;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalNBT;
import com.luneruniverse.minecraft.mod.nbteditor.misc.BlockStateProperties;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.NBTReference;
import com.luneruniverse.minecraft.mod.nbteditor.screens.LocalEditorScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigCategory;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigItem;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigPanel;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigValueDropdown;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.nbt.NbtCompound;

public class BlockStatesScreen<L extends LocalNBT> extends LocalEditorScreen<L, NBTReference<L>> {
	
	private final ConfigCategory blockStates;
	private final boolean hasBlockStates;
	private ConfigPanel panel;
	
	public BlockStatesScreen(NBTReference<L> ref) {
		super(TextInst.of("Block States"), ref);
		
		BlockStateProperties defaultState;
		BlockStateProperties state;
		Set<String> unset = new HashSet<>();
		if (localNBT instanceof LocalItem item) {
			defaultState = new BlockStateProperties(((BlockItem) item.getItem().getItem()).getBlock().getDefaultState());
			state = defaultState.copy();
			unset = state.setValues(localNBT.getOrCreateNBT().getCompound("BlockStateTag"));
		} else if (localNBT instanceof LocalBlock block) {
			defaultState = new BlockStateProperties(block.getBlock().getDefaultState());
			state = block.getState();
		} else
			throw new IllegalStateException("BlockStatesScreen doesn't support " + localNBT.getClass().getName());
		this.hasBlockStates = !defaultState.getProperties().isEmpty();
		this.blockStates = new ConfigCategory(this.hasBlockStates ? TextInst.translatable("nbteditor.block_states") : null);
		
		for (String property : defaultState.getProperties()) {
			String value = (unset.contains(property) ? "unset" : state.getValue(property));
			
			List<String> options = new ArrayList<>(defaultState.getOptions(property));
			if (localNBT instanceof LocalItem)
				options.add(0, "unset");
			
			blockStates.setConfigurable(property, new ConfigItem<>(TextInst.literal(property),
				new ConfigValueDropdown<>(value, defaultState.getValue(property), options)
				.addValueListener(dropdown -> {
					String newValue = dropdown.getValidValue();
					
					if (localNBT instanceof LocalItem item) {
						NbtCompound blockStatesTag = item.getOrCreateNBT().getCompound("BlockStateTag");
						if (newValue.equals("unset"))
							blockStatesTag.remove(property);
						else
							blockStatesTag.putString(property, newValue);
						item.getNBT().put("BlockStateTag", blockStatesTag);
					} else if (localNBT instanceof LocalBlock) {
						state.setValue(property, newValue);
					}
					
					checkSave();
				})));
		}
	}
	
	@Override
	protected void initEditor() {
		ConfigPanel newPanel = addDrawableChild(new ConfigPanel(16, 64, width - 32, height - 80, blockStates));
		if (panel != null)
			newPanel.setScroll(panel.getScroll());
		panel = newPanel;
	}
	
	@Override
	public void renderEditor(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		if (!hasBlockStates)
			MVDrawableHelper.drawTextWithShadow(matrices, textRenderer, TextInst.translatable("nbteditor.block_states.none"), 16, 64, -1);
	}
	
}
