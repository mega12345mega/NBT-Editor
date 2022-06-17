package com.luneruniverse.minecraft.mod.nbteditor.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SaveQueue {
	
	private final String name;
	private final Runnable onSave;
	private volatile boolean saving;
	private volatile boolean queuedSave;
	private volatile Thread saveThread;
	private final List<Optional<Runnable>> onFinished;
	private final boolean waitToCallFinish;
	
	/**
	 * This allows save() to be called while still saving
	 * It will make sure everything is finished saving without blocking
	 * 
	 * @param name Used in the thread's name
	 * @param onSave What to call when a save is requested
	 * @param waitToCallFinish If onFinished should be called after everything is done or after the requested one is done
	 */
	public SaveQueue(String name, Runnable onSave, boolean waitToCallFinish) {
		this.name = name;
		this.onSave = onSave;
		this.onFinished = new ArrayList<>();
		this.waitToCallFinish = waitToCallFinish;
	}
	
	public void save(Runnable onFinished) {
		this.onFinished.add(Optional.ofNullable(onFinished));
		
		if (saving) {
			queuedSave = true;
			saveThread.interrupt();
		} else {
			saving = true;
			saveThread = new Thread(() -> {
				onSave.run();
				if (!queuedSave || !waitToCallFinish) {
					int maxSize = queuedSave ? 1 : 0;
					while (this.onFinished.size() > maxSize)
						this.onFinished.remove(0).ifPresent(Runnable::run);
				}
				saving = false;
				if (queuedSave) {
					queuedSave = false;
					saveThread.run();
				}
			}, "SaveQueue:" + name);
			saveThread.start();
		}
	}
	public void save() {
		save(null);
	}
	
	public Runnable getOnSave() {
		return onSave;
	}
	
}
