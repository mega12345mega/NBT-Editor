package com.luneruniverse.minecraft.mod.nbteditor.util;

import java.util.ArrayList;
import java.util.List;

public class SaveQueue {
	
	private final String name;
	private final Runnable onSave;
	private volatile boolean saving;
	private volatile boolean queuedSave;
	private volatile Thread saveThread;
	private final List<Runnable> onFinished;
	
	public SaveQueue(String name, Runnable onSave) {
		this.name = name;
		this.onSave = onSave;
		this.onFinished = new ArrayList<>();
	}
	
	public void save(Runnable onFinished) {
		if (onFinished != null)
			this.onFinished.add(onFinished);
		
		if (saving) {
			queuedSave = true;
			saveThread.interrupt();
		} else {
			saving = true;
			saveThread = new Thread(() -> {
				onSave.run();
				int maxSize = queuedSave ? 1 : 0;
				while (this.onFinished.size() > maxSize)
					this.onFinished.remove(0).run();
				saving = false;
				if (queuedSave) {
					queuedSave = false;
					save();
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
