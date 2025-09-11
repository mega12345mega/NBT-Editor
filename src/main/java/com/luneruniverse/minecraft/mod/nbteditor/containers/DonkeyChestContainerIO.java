package com.luneruniverse.minecraft.mod.nbteditor.containers;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

public class DonkeyChestContainerIO implements ContainerIO<NbtCompound> {
	
	private static final boolean ITEMS_SHIFTED = Version.<Boolean>newSwitch()
			.range("1.20.5", null, false)
			.range(null, "1.20.4", true)
			.get();
	
	private final boolean llama;
	private final ContainerIO<NbtCompound> delegate;
	private final Identifier[] textures;
	
	public DonkeyChestContainerIO(boolean llama) {
		this.llama = llama;
		this.delegate = new SlotKeyNbtListContainerIO(ITEMS_SHIFTED ? 17 : 15).forNbtCompoundItems();
		this.textures = new Identifier[15];
	}
	
	@Override
	public boolean isSupported(NbtCompound container) {
		return delegate.isSupported(container);
	}
	
	@Override
	public int getMaxSlots(NbtCompound container) {
		return 15;
	}
	
	@Override
	public Identifier[] getTextures(NbtCompound container) {
		return textures;
	}
	
	@Override
	public ItemStack[] read(NbtCompound container) {
		ItemStack[] contents = delegate.read(container);
		if (ITEMS_SHIFTED) {
			ItemStack[] temp = new ItemStack[15];
			System.arraycopy(contents, 2, temp, 0, temp.length);
			contents = temp;
		}
		return contents;
	}
	
	@Override
	public int write(NbtCompound container, ItemStack[] contents) {
		ItemStack[] shiftedContents = contents;
		if (ITEMS_SHIFTED) {
			shiftedContents = new ItemStack[17];
			shiftedContents[0] = ItemStack.EMPTY;
			shiftedContents[1] = ItemStack.EMPTY;
			System.arraycopy(contents, 0, shiftedContents, 2, contents.length);
		}
		delegate.write(container, shiftedContents);
		
		for (ItemStack item : contents) {
			if (item != null && !item.isEmpty()) {
				container.putBoolean("ChestedHorse", true);
				break;
			}
		}
		
		if (llama) {
			int columns = 1;
			for (int i = 3; i < contents.length; i++) {
				if (contents[i] != null && !contents[i].isEmpty())
					columns = (i / 3) + 1;
			}
			if (columns != 1 && container.nbte$getIntOrDefault("Strength") < columns)
				container.putInt("Strength", columns);
		}
		
		return 15;
	}
	
	@Override
	public int getNumWritten(NbtCompound container, ItemStack[] contents) {
		return 15;
	}
	
	@Override
	public int getWrittenSlotIndex(NbtCompound container, ItemStack[] contents, int slot) {
		return slot;
	}
	
}
