package com.luneruniverse.minecraft.mod.nbteditor.misc;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import net.minecraft.resource.ResourceReload;

public class ParallelResourceReload implements ResourceReload {
	
	private final ResourceReload[] monitors;
	private final CompletableFuture<?> future;
	
	public ParallelResourceReload(ResourceReload mainMonitor, ResourceReload... additionalMonitors) {
		monitors = new ResourceReload[1 + additionalMonitors.length];
		monitors[0] = mainMonitor;
		System.arraycopy(additionalMonitors, 0, monitors, 1, additionalMonitors.length);
		
		future = CompletableFuture.allOf(
				Arrays.stream(monitors).map(ResourceReload::whenComplete).toArray(CompletableFuture<?>[]::new))
				.thenApply(voidResult -> mainMonitor.whenComplete().join());
	}
	
	@Override
	public CompletableFuture<?> whenComplete() {
		return future;
	}
	
	@Override
	public float getProgress() {
		return (float) Arrays.stream(monitors).mapToDouble(ResourceReload::getProgress).average().getAsDouble();
	}
	
}
