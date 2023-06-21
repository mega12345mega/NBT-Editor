package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public enum Version {
	v1_19_4,
	v1_19_3,
	v1_19,
	v1_18_v1_17;
	
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
			case 18 -> v1_18_v1_17;
			case 17 -> v1_18_v1_17;
			default -> throw unsupported.get();
		};
	}
	
	private static String releaseTarget;
	public static String getReleaseTarget() {
		if (releaseTarget != null)
			return releaseTarget;
		
		try (InputStream in = Version.class.getResourceAsStream("/version.json");
				InputStreamReader reader = new InputStreamReader(in);) {
			JsonObject data = new Gson().fromJson(reader, JsonObject.class);
			if (data.has("release_target"))
				return releaseTarget = data.get("release_target").getAsString();
			String id = data.get("id").getAsString();
			return releaseTarget = id.split("\\+|-")[0];
		} catch (IOException e) {
			throw new UncheckedIOException("Error trying to read game version", e);
		}
	}
}
