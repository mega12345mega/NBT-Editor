package com.luneruniverse.minecraft.mod.nbteditor.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditor;

public class SaveQueue<T> {
	
	private final String name;
	private final Consumer<T> onSave;
	private volatile boolean saving;
	private volatile boolean queuedSave;
	private volatile Thread saveThread;
	private final Queue<Optional<T>> infos;
	private final Queue<CompletableFuture<Void>> onFinished;
	private final boolean waitToCallFinish;
	
	/**
	 * This allows save() to be called while still saving
	 * It will make sure everything is finished saving without blocking
	 * 
	 * @param name Used in the thread's name
	 * @param onSave What to call when a save is requested
	 * @param waitToCallFinish If onFinished should be called after everything is done or after the requested one is done
	 */
	public SaveQueue(String name, Consumer<T> onSave, boolean waitToCallFinish) {
		this.name = name;
		this.onSave = onSave;
		this.infos = new ConcurrentLinkedQueue<>();
		this.onFinished = new ConcurrentLinkedQueue<>();
		this.waitToCallFinish = waitToCallFinish;
	}
	
	/**
	 * This allows save() to be called while still saving
	 * It will make sure everything is finished saving without blocking
	 * 
	 * @param name Used in the thread's name
	 * @param onSave What to call when a save is requested
	 * @param waitToCallFinish If onFinished should be called after everything is done or after the requested one is done
	 */
	public SaveQueue(String name, Runnable onSave, boolean waitToCallFinish) {
		this(name, info -> onSave.run(), waitToCallFinish);
	}
	
	public synchronized CompletableFuture<Void> save(T info) {
		CompletableFuture<Void> output = new CompletableFuture<>();
		
		infos.add(Optional.ofNullable(info));
		onFinished.add(output);
		
		if (saving) {
			queuedSave = true;
			saveThread.interrupt();
		} else {
			saving = true;
			saveThread = new Thread(() -> {
				boolean rerun;
				do {
					rerun = false;
					
					Throwable exception;
					try {
						onSave.accept(infos.remove().orElse(null));
						exception = null;
					} catch (Throwable e) {
						exception = e;
					}
					
					synchronized (this) {
						if (!queuedSave || !waitToCallFinish) {
							if (exception == null)
								onFinished.remove().complete(null);
							else
								onFinished.remove().completeExceptionally(exception);
						}
						if (onFinished.size() > 1) {
							// Merge currently queued onFinished callbacks, so that,
							// if waitToCallFinish is false, everything up until and
							// including the queued save will be called when the
							// queued save completes
							List<CompletableFuture<Void>> currentOnFinished = new ArrayList<>();
							while (!onFinished.isEmpty())
								currentOnFinished.add(onFinished.remove());
							onFinished.add(MainUtil.mergeFutures(currentOnFinished));
						}
						
						int maxSize = queuedSave ? 1 : 0;
						while (infos.size() > maxSize)
							infos.remove();
						
						if (queuedSave) {
							queuedSave = false;
							rerun = true;
						} else
							saving = false;
					}
				} while (rerun);
			}, "NBTEditor/Async/SaveQueue:" + name);
			saveThread.start();
		}
		
		return output;
	}
	public CompletableFuture<Void> save() {
		return save((T) null);
	}
	public void save(Runnable onFinished, T info) {
		save(info).thenAccept(v -> onFinished.run()).exceptionally(e -> {
			NBTEditor.LOGGER.error("Error saving something", e);
			return null;
		});
	}
	public void save(Runnable onFinished) {
		save(onFinished, null);
	}
	
	public boolean isSaving() {
		return saving;
	}
	
}
