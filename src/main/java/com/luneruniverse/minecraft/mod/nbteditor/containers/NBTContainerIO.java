package com.luneruniverse.minecraft.mod.nbteditor.containers;

public interface NBTContainerIO extends NonItemNBTContainerIO {
	/**
	 * In <= 1.20.4, the contents of a non-null return value is irrelevant
	 * @return The id to add to block_entity_data/entity_data, or null if the root tag should be passed to readNBT and writeNBT
	 */
	public String getDefaultEntityId();
}
