package com.luneruniverse.minecraft.mod.nbteditor.containers;

import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalEntity;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;

import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

public record ItemEntityContainerIO(ContainerIO<ItemStack> item, ContainerIO<LocalEntity> entity) {
	
	public static ItemEntityContainerIO forEntityTagIO(ContainerIO<NbtCompound> io, String entityId) {
		return new ItemEntityContainerIO(ContainerIO.forItemStackEntityTag(io, entityId), ContainerIO.forLocalNBT(io));
	}
	public static ItemEntityContainerIO forEntityTagIO(ContainerIO<NbtCompound> io, EntityType<?> entityId) {
		return forEntityTagIO(io, EntityType.getId(entityId).toString());
	}
	
	public static ItemEntityContainerIO forSlotKeyItems(int numSlots) {
		return Version.<ItemEntityContainerIO>newSwitch()
				.range("1.20.5", null, () -> {
					return new ItemEntityContainerIO(
							new ContainerComponentContainerIO(numSlots),
							ContainerIO.forLocalNBT(new SlotKeyNbtListContainerIO(numSlots).forNbtCompoundItems()));
				})
				.range(null, "1.20.4", () -> forEntityTagIO(
						new SlotKeyNbtListContainerIO(numSlots).forNbtCompoundItems(), ""))
				.get();
	}
	
	public static ItemEntityContainerIO forKeys(String entityId, String... keys) {
		return new ItemEntityContainerIO(
				ContainerIO.forItemStackEntityTag(new KeysContainerIO(true, keys), entityId),
				ContainerIO.forLocalNBT(new KeysContainerIO(false, keys)));
	}
	public static ItemEntityContainerIO forKeys(EntityType<?> entityId, String... keys) {
		return forKeys(EntityType.getId(entityId).toString(), keys);
	}
	
	public ItemEntityContainerIO withTextures(Identifier... textures) {
		return new ItemEntityContainerIO(item.withTextures(textures), entity.withTextures(textures));
	}
	
}
