package com.luneruniverse.minecraft.mod.nbteditor.containers;

import java.util.Map;

import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalBlock;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.ItemTagReferences;
import com.luneruniverse.minecraft.mod.nbteditor.util.BlockStateProperties;

import net.minecraft.item.ItemStack;

/**
 * Patches MC-48453
 */
public class BlockStateUpdatingContainerIO {
	
	public static ContainerIO<ItemStack> forItemStack(ContainerIO<ItemStack> io, String... states) {
		return new DelegateContainerIO<>(io) {
			@Override
			public int write(ItemStack container, ItemStack[] contents) {
				int numWritten = super.write(container, contents);
				
				Map<String, String> blockState = ItemTagReferences.BLOCK_STATE.get(container);
				for (int i = 0; i < states.length; i++) {
					ItemStack item = contents[i];
					if (item == null || item.isEmpty())
						blockState.remove(states[i]);
					else
						blockState.put(states[i], "true");
				}
				ItemTagReferences.BLOCK_STATE.set(container, blockState);
				
				return numWritten;
			}
		};
	}
	
	public static ContainerIO<LocalBlock> forLocalBlock(ContainerIO<LocalBlock> io, String... states) {
		return new DelegateContainerIO<>(io) {
			@Override
			public int write(LocalBlock container, ItemStack[] contents) {
				int numWritten = super.write(container, contents);
				
				BlockStateProperties blockState = container.getState();
				for (int i = 0; i < states.length; i++) {
					ItemStack item = contents[i];
					blockState.setValue(states[i], item == null || item.isEmpty() ? "false" : "true");
				}
				
				return numWritten;
			}
		};
	}
	
	public static ItemBlockContainerIO forItemBlock(ItemBlockContainerIO io, String... states) {
		return new ItemBlockContainerIO(forItemStack(io.item(), states), forLocalBlock(io.block(), states));
	}
	
}
