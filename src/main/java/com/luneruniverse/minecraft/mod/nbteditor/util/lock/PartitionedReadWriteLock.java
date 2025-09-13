package com.luneruniverse.minecraft.mod.nbteditor.util.lock;

/**
 * Only allows one read or one write at a time <br>
 * Allows you to wait for only write operations to finish by calling <code>.write().stop()</code>
 */
public class PartitionedReadWriteLock {
	
	private final PartitionedLock internalReadLock;
	private final PartitionedLock internalWriteLock;
	private final PartitionedLock writeLock;
	
	public PartitionedReadWriteLock() {
		internalReadLock = new PartitionedLockImpl();
		internalWriteLock = new PartitionedLockImpl();
		writeLock = new PartitionedWriteLock();
	}
	
	public PartitionedLock read() {
		return internalReadLock;
	}
	
	public PartitionedLock write() {
		return writeLock;
	}
	
	private class PartitionedWriteLock implements PartitionedLock {
		
		@Override
		public void stop() {
			internalWriteLock.stop();
		}
		
		@Override
		public void lockAll() {
			internalWriteLock.lockAll();
			internalReadLock.lockAll();
		}
		
		@Override
		public void unlockAll() {
			internalWriteLock.unlockAll();
			internalReadLock.unlockAll();
		}
		
		@Override
		public void lock(int partition) {
			internalWriteLock.lock(partition);
			internalReadLock.lock(partition);
		}
		
		@Override
		public void unlock(int partition) {
			internalWriteLock.unlock(partition);
			internalReadLock.unlock(partition);
		}
		
		@Override
		public boolean isAllLocked() {
			return internalWriteLock.isAllLocked();
		}
		
		@Override
		public boolean isLocked(int partition) {
			return internalWriteLock.isLocked(partition);
		}
		
	}
	
}
