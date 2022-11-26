package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.util.stream.Stream;

import net.minecraft.SharedConstants;

public enum Version {
	v1_19,
	v1_18;
	
	private static volatile Version CURRENT;
	public static Version get() {
		if (CURRENT != null)
			return CURRENT;
		
		String version = SharedConstants.getGameVersion().getReleaseTarget();
		int[] parts = Stream.of(version.split("\\.")).mapToInt(Integer::parseInt).toArray();
		if (parts[0] != 1)
			throw new IllegalStateException("Unsupported Minecraft version: " + version);
		return CURRENT = switch (parts[1]) {
			case 19 -> v1_19;
			case 18 -> v1_18;
			default -> throw new IllegalStateException("Unsupported Minecraft version: " + version);
		};
	}
}
