package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.lang.invoke.MethodType;
import java.util.function.Supplier;
import java.util.stream.Stream;

import net.minecraft.SharedConstants;

public enum Version {
	v1_19_4,
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
				case 4 -> v1_19_4;
				case 3 -> v1_19_3;
				case 2, 1, 0 -> v1_19;
				default -> throw unsupported.get();
			};
			case 18 -> v1_18;
			default -> throw unsupported.get();
		};
	}
	
	private static final Supplier<Class<?>> Bridge_GameVersion = () -> {
				try {
					return Class.forName("com.mojang.bridge.game.GameVersion");
				} catch (ClassNotFoundException e) {
					throw new RuntimeException(e);
				}
			};
	private static final Supplier<Reflection.MethodInvoker> Bridge_GameVersion_getReleaseTarget =
			Reflection.getOptionalMethod(Bridge_GameVersion, () -> "getReleaseTarget", () -> MethodType.methodType(String.class));
	private static final Supplier<Reflection.MethodInvoker> Bridge_GameVersion_getId =
			Reflection.getOptionalMethod(Bridge_GameVersion, () -> "getId", () -> MethodType.methodType(String.class));
	private static final boolean getReleaseTargetExists;
	private static final boolean getIdExists;
	private static boolean testExists(Supplier<?> toTest) {
		try {
			toTest.get();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	static {
		getReleaseTargetExists = testExists(Bridge_GameVersion_getReleaseTarget);
		getIdExists = testExists(Bridge_GameVersion_getId);
	}
	public static String getReleaseTarget() {
		if (getReleaseTargetExists)
			return Bridge_GameVersion_getReleaseTarget.get().invoke(SharedConstants.getGameVersion());
		
		String id;
		if (getIdExists)
			id = Bridge_GameVersion_getId.get().invoke(SharedConstants.getGameVersion());
		else
			id = SharedConstants.getGameVersion().getId();
		return id.split("\\+|-")[0];
	}
}
