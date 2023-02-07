package com.luneruniverse.minecraft.mod.nbteditor.util;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditor;

public class SaveQueue {
	
	private final String name;
	private final Consumer<Object> onSave;
	private volatile boolean saving;
	private volatile boolean queuedSave;
	private volatile Thread saveThread;
	private final Queue<Optional<Object>> infos;
	private final Queue<Optional<Runnable>> onFinished;
	private final boolean waitToCallFinish;
	
	/**
	 * This allows save() to be called while still saving
	 * It will make sure everything is finished saving without blocking
	 * 
	 * @param name Used in the thread's name
	 * @param onSave What to call when a save is requested
	 * @param waitToCallFinish If onFinished should be called after everything is done or after the requested one is done
	 */
	@SuppressWarnings("unchecked")
	public <T> SaveQueue(String name, Consumer<T> onSave, boolean waitToCallFinish) {
		this.name = name;
		this.onSave = info -> onSave.accept((T) info);
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
	
	public void save(Runnable onFinished, Object info) {
		this.infos.add(Optional.ofNullable(info));
		this.onFinished.add(Optional.ofNullable(onFinished));
		
		if (saving) {
			queuedSave = true;
			saveThread.interrupt();
		} else {
			saving = true;
			saveThread = new Thread(() -> {
				try {
					onSave.accept(infos.remove().orElse(null));
				} catch (RuntimeException e) {
					NBTEditor.LOGGER.error("Error while calling save queue", e);
				}
				synchronized (this) {
					int maxSize = queuedSave ? 1 : 0;
					if (!queuedSave || !waitToCallFinish) {
						while (this.onFinished.size() > maxSize)
							this.onFinished.remove().ifPresent(Runnable::run);
					}
					while (this.infos.size() > maxSize)
						this.infos.remove();
					if (queuedSave) {
						queuedSave = false;
						saveThread.run();
					}
					saving = false;
				}
			}, "SaveQueue:" + name);
			saveThread.start();
		}
	}
	public void save(Runnable onFinished) {
		save(onFinished, null);
	}
	public void save(Object info) {
		save(null, info);
	}
	public void save() {
		save(null, null);
	}
	
}
