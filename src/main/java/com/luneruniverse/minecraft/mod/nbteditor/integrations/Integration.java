package com.luneruniverse.minecraft.mod.nbteditor.integrations;

import java.util.Optional;
import java.util.function.Supplier;

import net.fabricmc.loader.api.FabricLoader;

public abstract class Integration {
	
	public static <T extends Integration> Optional<T> getOptional(Supplier<T> integration) {
		T value = integration.get();
		if (value.isLoaded())
			return Optional.of(value);
		return Optional.empty();
	}
	
	private Boolean loaded;
	
	public boolean isLoaded() {
		if (loaded == null) {
			loaded = FabricLoader.getInstance().getAllMods().stream()
					.anyMatch(mod -> mod.getMetadata().getId().equals(getModId()));
		}
		return loaded;
	}
	
	public abstract String getModId();
	
}
