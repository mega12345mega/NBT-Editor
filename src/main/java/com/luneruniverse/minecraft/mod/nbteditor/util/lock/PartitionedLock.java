package com.luneruniverse.minecraft.mod.nbteditor.util.lock;

public interface PartitionedLock {
	/**
	 * Wait until all locks are unlocked, and cause all currently waiting
	 * lock requests to never finish, freezing those threads
	 */
	public void stop();
	
	public void lockAll();
	public void unlockAll();
	
	public void lock(int partition);
	public void unlock(int partition);
	
	public boolean isAllLocked();
	public boolean isLocked(int partition);
}
