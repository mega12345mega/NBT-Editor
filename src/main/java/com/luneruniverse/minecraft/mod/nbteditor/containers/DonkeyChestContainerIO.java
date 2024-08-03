package com.luneruniverse.minecraft.mod.nbteditor.containers;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class DonkeyChestContainerIO implements NBTContainerIO {
	
	private static final boolean ITEMS_SHIFTED = Version.<Boolean>newSwitch()
			.range("1.20.5", null, false)
			.range(null, "1.20.4", true)
			.get();
	
	private final boolean llama;
	private final ConstSizeContainerIO chest;
	
	public DonkeyChestContainerIO(boolean llama) {
		this.llama = llama;
		this.chest = new ConstSizeContainerIO(ITEMS_SHIFTED ? 17 : 15);
	}
	
	@Override
	public int getMaxNBTSize(NbtCompound nbt, SourceContainerType source) {
		return 15;
	}
	
	@Override
	public ItemStack[] readNBT(NbtCompound container, SourceContainerType source) {
		ItemStack[] output = chest.readNBT(container, SourceContainerType.ENTITY);
		if (ITEMS_SHIFTED) {
			ItemStack[] temp = new ItemStack[15];
			System.arraycopy(output, 2, temp, 0, temp.length);
			output = temp;
		}
		return output;
	}
	
	@Override
	public int writeNBT(NbtCompound container, ItemStack[] contents, SourceContainerType source) {
		ItemStack[] shiftedContents = contents;
		if (ITEMS_SHIFTED) {
			shiftedContents = new ItemStack[contents.length + 2];
			shiftedContents[0] = ItemStack.EMPTY;
			shiftedContents[1] = ItemStack.EMPTY;
			System.arraycopy(contents, 0, shiftedContents, 2, contents.length);
		}
		
		int output = chest.writeNBT(container, shiftedContents, SourceContainerType.ENTITY) - (ITEMS_SHIFTED ? 2 : 0);
		
		if (llama) {
			int columns = 1;
			for (int i = 3; i < contents.length; i++) {
				if (contents[i] != null && !contents[i].isEmpty())
					columns = (i / 3) + 1;
			}
			if (columns == 1)
				return output;
			if (container.getInt("Strength") < columns)
				container.putInt("Strength", columns);
		}
		
		return output;
	}
	
}
