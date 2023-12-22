package com.luneruniverse.minecraft.mod.nbteditor.screens;

public interface InjectableTickable extends Tickable {
	@Override
	default void tick() {
		throw new RuntimeException("Missing implementation for InjectableTickable#tick");
	}
}
