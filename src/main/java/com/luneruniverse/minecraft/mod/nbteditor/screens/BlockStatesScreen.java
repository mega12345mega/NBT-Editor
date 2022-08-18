package com.luneruniverse.minecraft.mod.nbteditor.screens;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigCategory;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigItem;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigPanel;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigValueDropdown;
import com.luneruniverse.minecraft.mod.nbteditor.util.ItemReference;

import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;

public class BlockStatesScreen extends ItemEditorScreen {
	
	private final ConfigCategory blockStates;
	private final boolean hasBlockStates;
	private ConfigPanel panel;
	
	public BlockStatesScreen(ItemReference ref) {
		super(Text.of("Block States"), ref);
		
		BlockState defaultState = ((BlockItem) item.getItem()).getBlock().getDefaultState();
		this.hasBlockStates = !defaultState.getProperties().isEmpty();
		this.blockStates = new ConfigCategory(this.hasBlockStates ? Text.translatable("nbteditor.blockstates") : null);
		
		NbtCompound nbt = item.getOrCreateNbt();
		NbtCompound blockStatesNbt = nbt.getCompound("BlockStateTag");
		
		for (Property<?> property : defaultState.getProperties()) {
			String thisState = Optional.of(blockStatesNbt.getString(property.getName())).filter(state -> !state.isEmpty()).orElse("unset");
			
			List<String> options = property.getValues().stream().map(Object::toString).collect(Collectors.toList());
			options.add(0, "unset");
			if (!options.contains(thisState))
				thisState = "unset";
			
			blockStates.setConfigurable(property.getName(), new ConfigItem<>(Text.literal(property.getName()),
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
			drawTextWithShadow(matrices, textRenderer, Text.translatable("nbteditor.noblockstates"), 16, 64, -1);
	}
	
}