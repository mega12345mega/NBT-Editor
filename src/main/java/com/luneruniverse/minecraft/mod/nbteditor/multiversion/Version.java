package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.lang.invoke.MethodType;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.mojang.bridge.game.GameVersion;

import net.minecraft.SharedConstants;

public enum Version {
	v1_19_3,
	v1_19,
	v1_18;
	
	private static volatile Version CURRENT;
	public static Version get() {
		if (CURRENT != null)
			return CURRENT;
		
		String version = getReleaseTarget();
		Supplier<IllegalStateException> unsupported = () ->
				new IllegalStateException("Unsupported Minecraft version: " + version);
		
		int[] parts = Stream.of(version.split("\\.")).mapToInt(Integer::parseInt).toArray();
		if (parts[0] != 1)
			throw unsupported.get();
		
		return CURRENT = switch (parts[1]) {
			case 19 -> switch (parts.length == 2 ? 0 : parts[2]) {
				case 3 -> v1_19_3;
				case 2, 1, 0 -> v1_19;
				default -> throw unsupported.get();
			};
			case 18 -> v1_18;
			default -> throw unsupported.get();
		};
	}
	
	private static final Supplier<Reflection.MethodInvoker> GameVersion_getReleaseTarget =
			Reflection.getOptionalMethod(GameVersion.class, "getReleaseTarget", MethodType.methodType(String.class));
	private static final boolean getReleaseTargetExists;
	static {
		boolean exists = false;
		try {
			GameVersion_getReleaseTarget.get();
			exists = true;
		} catch (Exception e) {}
		getReleaseTargetExists = exists;
	}
	public static String getReleaseTarget() {
		if (getReleaseTargetExists)
			return GameVersion_getReleaseTarget.get().invoke(SharedConstants.getGameVersion());
		return SharedConstants.getGameVersion().getId().split("\\+|-")[0];
	}
}
