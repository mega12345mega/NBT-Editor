package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import net.minecraft.util.Identifier;

public class Version {
	
	public static class VersionSwitch<T> {
		private final int[] version;
		private Supplier<T> match;
		private VersionSwitch(int[] version) {
			this.version = version;
		}
		public VersionSwitch<T> range(String min, String max, Supplier<T> value) {
			int[] minParts = min == null ? null : parseVersion(min);
			int[] maxParts = max == null ? null : parseVersion(max);
			boolean minMatch = (min != null);
			boolean maxMatch = (max != null);
			for (int i = 0; i < 3; i++) {
				if (minMatch) {
					if (minParts[i] < version[i])
						minMatch = false;
					else if (minParts[i] > version[i])
						return this;
				}
				if (maxMatch) {
					if (version[i] < maxParts[i])
						maxMatch = false;
					else if (version[i] > maxParts[i])
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
		public Optional<T> getOptionally() {
			if (match == null)
				return Optional.empty();
			return Optional.of(match.get());
		}
		public Optional<Runnable> runOptionally() {
			if (match == null)
				return Optional.empty();
			return Optional.of(() -> match.get());
		}
	}
	
	public static <T> VersionSwitch<T> newSwitch(int[] version) {
		return new VersionSwitch<>(version);
	}
	public static <T> VersionSwitch<T> newSwitch() {
		return new VersionSwitch<>(Version.get());
	}
	
	private static volatile int[] CURRENT;
	public static int[] get() {
		if (CURRENT == null)
			CURRENT = parseVersion(getReleaseTarget());
		return CURRENT;
	}
	
	private static String releaseTarget;
	public static String getReleaseTarget() {
		if (releaseTarget == null)
			readVersionJson();
		return releaseTarget;
	}
	
	private static Integer dataVersion;
	public static int getDataVersion() {
		if (dataVersion == null)
			readVersionJson();
		return dataVersion;
	}
	
	private static void readVersionJson() {
		try (InputStream in = Version.class.getResourceAsStream("/version.json");
				InputStreamReader reader = new InputStreamReader(in);) {
			JsonObject data = new Gson().fromJson(reader, JsonObject.class);
			
			if (data.has("release_target"))
				releaseTarget = data.get("release_target").getAsString();
			else {
				String id = data.get("id").getAsString();
				releaseTarget = id.split("\\+|-")[0];
			}
			
			dataVersion = data.get("world_version").getAsInt();
		} catch (IOException e) {
			throw new UncheckedIOException("Error trying to parse version.json", e);
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
	
	private static Map<String, Integer> dataVersions;
	public static Optional<Integer> getDataVersion(String version) {
		try {
			return Optional.of(Integer.parseInt(version));
		} catch (NumberFormatException e) {}
		
		if (dataVersions == null) {
			try (InputStream in = MVMisc.getResource(new Identifier("nbteditor", "data_versions.json")).orElseThrow()) {
				dataVersions = new Gson().fromJson(new InputStreamReader(in), new TypeToken<Map<String, Integer>>() {});
			} catch (IOException e) {
				throw new RuntimeException("Failed to parse data_versions.json", e);
			}
		}
		
		return Optional.ofNullable(dataVersions.get(version));
	}
	
}
