package com.luneruniverse.minecraft.mod.nbteditor.screens.factories;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.luneruniverse.minecraft.mod.nbteditor.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ItemEditorScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigCategory;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigItem;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigPanel;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigValueDropdown;

import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.state.property.Property;

public class BlockStatesScreen extends ItemEditorScreen {
	
	private final ConfigCategory blockStates;
	private final boolean hasBlockStates;
	private ConfigPanel panel;
	
	public BlockStatesScreen(ItemReference ref) {
		super(TextInst.of("Block States"), ref);
		
		BlockState defaultState = ((BlockItem) item.getItem()).getBlock().getDefaultState();
		this.hasBlockStates = !defaultState.getProperties().isEmpty();
		this.blockStates = new ConfigCategory(this.hasBlockStates ? TextInst.translatable("nbteditor.block_states") : null);
		
		NbtCompound nbt = item.getOrCreateNbt();
		NbtCompound blockStatesNbt = nbt.getCompound("BlockStateTag");
		
		for (Property<?> property : defaultState.getProperties()) {
			String thisState = Optional.of(blockStatesNbt.getString(property.getName())).filter(state -> !state.isEmpty()).orElse("unset");
			
			List<String> options = property.getValues().stream().map(Object::toString).collect(Collectors.toList());
			options.add(0, "unset");
			if (!options.contains(thisState))
				thisState = "unset";
			
			blockStates.setConfigurable(property.getName(), new ConfigItem<>(TextInst.literal(property.getName()),
				new ConfigValueDropdown<>(thisState, defaultState.get(property).toString(), options)
				.addValueListener(dropdown -> {
					String value = dropdown.getValidValue();
					
					if (value.equals("unset"))
						blockStatesNbt.remove(property.getName());
					else
						blockStatesNbt.putString(property.getName(), value);
					nbt.put("BlockStateTag", blockStatesNbt);
					
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
			drawTextWithShadow(matrices, textRenderer, TextInst.translatable("nbteditor.block_states.none"), 16, 64, -1);
	}
	
}
