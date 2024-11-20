package com.luneruniverse.minecraft.mod.nbteditor.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PartitionedLock {
	
	private volatile boolean stopped;
	private final Lock globalLock;
	private final Map<Integer, Lock> locks;
	private volatile int globallyLocked;
	private final Map<Integer, Integer> lockedPartitions;
	
	public PartitionedLock() {
		stopped = false;
		globalLock = new ReentrantLock(true);
		locks = new ConcurrentHashMap<>();
		globallyLocked = 0;
		lockedPartitions = new ConcurrentHashMap<>();
	}
	
	/**
	 * Wait until all locks are unlocked, and cause all currently waiting
	 * lock requests to never finish, freezing those threads
	 */
	public void stop() {
		stopped = true;
		
		globallyLocked++;
		globalLock.lock();
		locks.values().forEach(Lock::lock);
	}
	private void checkStop(Integer partition) {
		if (stopped) {
			if (partition != null)
				unlock(partition);
			else
				unlockAll();
			
			while (true) {
				try {
					Thread.sleep(Long.MAX_VALUE);
				} catch (InterruptedException e) {}
			}
		}
	}
	
	public void lockAll() {
		globallyLocked++;
		globalLock.lock();
		locks.values().forEach(Lock::lock);
		
		checkStop(null);
	}
	
	public void unlockAll() {
		locks.values().forEach(Lock::unlock);
		locks.clear();
		globalLock.unlock();
		globallyLocked--;
	}
	
	public void lock(int partition) {
		lockedPartitions.compute(partition, (key, value) -> (value == null ? 0 : value) + 1);
		globalLock.lock();
		try {
			Lock lock = locks.get(partition);
			if (lock == null)
				lock = new ReentrantLock(true);
			lock.lock();
			locks.put(partition, lock);
		} finally {
			globalLock.unlock();
		}
		
		checkStop(partition);
	}
	
	public void unlock(int partition) {
		locks.remove(partition).unlock();
		lockedPartitions.compute(partition, (key, value) -> value == 1 ? null : value - 1);
	}
	
	public boolean isAllLocked() {
		return globallyLocked > 0;
	}
	
	public boolean isLocked(int partition) {
		return globallyLocked > 0 || lockedPartitions.getOrDefault(partition, 0) > 0;
	}
	
}
