package com.luneruniverse.minecraft.mod.nbteditor.containers;

import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalBlock;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

public record ItemBlockContainerIO(ContainerIO<ItemStack> item, ContainerIO<LocalBlock> block) {
	
	public static ItemBlockContainerIO forBlockEntityTagIO(ContainerIO<NbtCompound> io, String entityId) {
		return new ItemBlockContainerIO(ContainerIO.forItemStackBlockEntityTag(io, entityId), ContainerIO.forLocalNBT(io));
	}
	public static ItemBlockContainerIO forBlockEntityTagIO(ContainerIO<NbtCompound> io, BlockEntityType<?> entityId) {
		return forBlockEntityTagIO(io, BlockEntityType.getId(entityId).toString());
	}
	
	public static ItemBlockContainerIO forSlotKeyItems(int numSlots) {
		return Version.<ItemBlockContainerIO>newSwitch()
				.range("1.20.5", null, () -> {
					return new ItemBlockContainerIO(
							new ContainerComponentContainerIO(numSlots),
							ContainerIO.forLocalNBT(new SlotKeyNbtListContainerIO(numSlots).forNbtCompoundItems()));
				})
				.range(null, "1.20.4", () -> forBlockEntityTagIO(
						new SlotKeyNbtListContainerIO(numSlots).forNbtCompoundItems(), ""))
				.get();
	}
	
	public static ItemBlockContainerIO forKeys(String entityId, String... keys) {
		return new ItemBlockContainerIO(
				ContainerIO.forItemStackBlockEntityTag(new KeysContainerIO(true, keys), entityId),
				ContainerIO.forLocalNBT(new KeysContainerIO(false, keys)));
	}
	public static ItemBlockContainerIO forKeys(BlockEntityType<?> entityId, String... keys) {
		return forKeys(BlockEntityType.getId(entityId).toString(), keys);
	}
	
	public ItemBlockContainerIO withTextures(Identifier... textures) {
		return new ItemBlockContainerIO(item.withTextures(textures), block.withTextures(textures));
	}
	
}
