package com.luneruniverse.minecraft.mod.nbteditor.util.lock;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PartitionedLockImpl implements PartitionedLock {
	
	private volatile boolean stopped;
	private final Lock globalLock;
	private final Map<Integer, Lock> locks;
	private volatile int globallyLocked;
	private final Map<Integer, Integer> lockedPartitions;
	
	public PartitionedLockImpl() {
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
	@Override
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
	
	@Override
	public void lockAll() {
		globallyLocked++;
		globalLock.lock();
		locks.values().forEach(Lock::lock);
		
		checkStop(null);
	}
	
	@Override
	public void unlockAll() {
		locks.values().forEach(Lock::unlock);
		locks.clear();
		globalLock.unlock();
		globallyLocked--;
	}
	
	@Override
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
	
	@Override
	public void unlock(int partition) {
		locks.remove(partition).unlock();
		lockedPartitions.compute(partition, (key, value) -> value == 1 ? null : value - 1);
	}
	
	@Override
	public boolean isAllLocked() {
		return globallyLocked > 0;
	}
	
	@Override
	public boolean isLocked(int partition) {
		return globallyLocked > 0 || lockedPartitions.getOrDefault(partition, 0) > 0;
	}
	
}
