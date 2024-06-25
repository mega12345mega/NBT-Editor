package com.luneruniverse.minecraft.mod.nbteditor.misc;

public interface ResetableDataTracker {
	public default void reset() {
		throw new RuntimeException("Missing implementation for ResetableDataTracker#reset");
	}
}
