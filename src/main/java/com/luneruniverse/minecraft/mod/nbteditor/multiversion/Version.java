package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Version {
	public static class VersionSwitch<T> {
		private Supplier<T> match;
		private VersionSwitch() {}
		public VersionSwitch<T> range(String min, String max, Supplier<T> value) {
			int[] minParts = min == null ? null : parseVersion(min);
			int[] maxParts = max == null ? null : parseVersion(max);
			int[] actualParts = Version.get();
			boolean minMatch = (min != null);
			boolean maxMatch = (max != null);
			for (int i = 0; i < 3; i++) {
				if (minMatch) {
					if (minParts[i] < actualParts[i])
						minMatch = false;
					else if (minParts[i] > actualParts[i])
						return this;
				}
				if (maxMatch) {
					if (actualParts[i] < maxParts[i])
						maxMatch = false;
					else if (actualParts[i] > maxParts[i])
						return this;
				}
			}
			if (match != null)
				throw new IllegalArgumentException("Overlapping versions!");
			match = value;
			return this;
		}
		public VersionSwitch<T> range(String min, String max, T value) {
			return range(min, max, () -> value);
		}
		public VersionSwitch<T> range(String min, String max, Runnable run) {
			return range(min, max, () -> {
				run.run();
				return null;
			});
		}
		public T get() {
			if (match == null)
				throw new IllegalStateException("Missing version!");
			return match.get();
		}
		public void run() {
			get();
		}
	}
	
	public static <T> VersionSwitch<T> newSwitch() {
		return new VersionSwitch<T>();
	}
	
	private static volatile int[] CURRENT;
	public static int[] get() {
		if (CURRENT == null)
			CURRENT = parseVersion(getReleaseTarget());
		return CURRENT;
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
	
	private static int[] parseVersion(String version) {
		int[] parts = Stream.of(version.split("\\.")).mapToInt(Integer::parseInt).toArray();
		if (parts[0] != 1 || parts.length < 2 || parts.length > 3)
			throw new IllegalArgumentException("Unsupported Minecraft version: " + version);
		if (parts.length == 3)
			return parts;
		return new int[] {parts[0], parts[1], 0};
	}
}
