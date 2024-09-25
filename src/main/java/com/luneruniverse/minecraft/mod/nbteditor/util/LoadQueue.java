package com.luneruniverse.minecraft.mod.nbteditor.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditor;

public class LoadQueue<T> {
	
	private final String name;
	private final Function<Integer, T> onLoad;
	private volatile boolean loading;
	private volatile boolean queuedLoad;
	private volatile Thread loadThread;
	private final Queue<Integer> levels;
	private final Queue<CompletableFuture<T>> onFinished;
	private final boolean waitToCallFinish;
	
	/**
	 * This allows load() to be called while still loading
	 * It will make sure everything is finished loading without blocking
	 * 
	 * @param name Used in the thread's name
	 * @param onLoad What to call when a load is requested
	 * @param waitToCallFinish If onFinished should be called after everything is done or after the requested one is done
	 */
	public LoadQueue(String name, Function<Integer, T> onLoad, boolean waitToCallFinish) {
		this.name = name;
		this.onLoad = onLoad;
		this.levels = new ConcurrentLinkedQueue<>();
		this.onFinished = new ConcurrentLinkedQueue<>();
		this.waitToCallFinish = waitToCallFinish;
	}
	
	/**
	 * This allows load() to be called while still loading
	 * It will make sure everything is finished loading without blocking
	 * 
	 * @param name Used in the thread's name
	 * @param onLoad What to call when a load is requested
	 * @param waitToCallFinish If onFinished should be called after everything is done or after the requested one is done
	 */
	public LoadQueue(String name, Supplier<T> onLoad, boolean waitToCallFinish) {
		this(name, level -> onLoad.get(), waitToCallFinish);
	}
	
	public synchronized CompletableFuture<T> load(int level) {
		CompletableFuture<T> output = new CompletableFuture<>();
		
		levels.add(level);
		onFinished.add(output);
		
		if (loading) {
			queuedLoad = true;
			loadThread.interrupt();
		} else {
			loading = true;
			loadThread = new Thread(() -> {
				boolean rerun;
				do {
					rerun = false;
					
					T loadedValue;
					Throwable exception;
					try {
						loadedValue = onLoad.apply(levels.remove());
						exception = null;
					} catch (RuntimeException e) {
						loadedValue = null;
						exception = e;
					}
					
					synchronized (this) {
						if (!queuedLoad || !waitToCallFinish) {
							if (exception == null)
								onFinished.remove().complete(loadedValue);
							else
								onFinished.remove().completeExceptionally(exception);
						}
						if (onFinished.size() > 1) {
							// Merge currently queued onFinished callbacks, so that,
							// if waitToCallFinish is false, everything up until and
							// including the queued load will be called when the
							// queued load completes
							List<CompletableFuture<T>> currentOnFinished = new ArrayList<>();
							while (!onFinished.isEmpty())
								currentOnFinished.add(onFinished.remove());
							onFinished.add(MainUtil.mergeFutures(currentOnFinished));
						}
						
						if (levels.size() > 1) {
							int maxLevel = Integer.MIN_VALUE;
							while (!levels.isEmpty())
								maxLevel = Math.max(maxLevel, levels.remove());
							levels.add(maxLevel);
						}
						
						if (queuedLoad) {
							queuedLoad = false;
							rerun = true;
						} else
							loading = false;
					}
				} while (rerun);
			}, "NBTEditor/Async/LoadQueue:" + name);
			loadThread.start();
		}
		
		return output;
	}
	public CompletableFuture<T> load() {
		return load(0);
	}
	public void load(Consumer<T> onFinished, int level) {
		load(level).thenAccept(onFinished).exceptionally(e -> {
			NBTEditor.LOGGER.error("Error loading something", e);
			return null;
		});
	}
	public void load(Consumer<T> onFinished) {
		load(onFinished, 0);
	}
	
	public boolean isLoading() {
		return loading;
	}
	
}
