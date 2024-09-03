package com.luneruniverse.minecraft.mod.nbteditor.util;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

public class CompletableFutureCache<T> {
	
	public enum Status {
		UNLOADED,
		LOADING,
		LOADED
	}
	
	private final Supplier<CompletableFuture<T>> loader;
	private final ReadWriteLock lock;
	private volatile Status status;
	private volatile T value;
	private final Queue<CompletableFuture<T>> futures;
	
	public CompletableFutureCache(Supplier<CompletableFuture<T>> loader) {
		this.loader = loader;
		this.lock = new ReentrantReadWriteLock();
		this.status = Status.UNLOADED;
		this.futures = new ConcurrentLinkedQueue<>();
	}
	
	public CompletableFuture<T> get() {
		lock.readLock().lock();
		try {
			if (status == Status.LOADED)
				return CompletableFuture.completedFuture(value);
			if (status == Status.LOADING) {
				CompletableFuture<T> future = new CompletableFuture<>();
				futures.add(future);
				return future;
			}
		} finally {
			lock.readLock().unlock();
		}
		lock.writeLock().lock();
		if (status != Status.UNLOADED) {
			lock.writeLock().unlock();
			return get();
		}
		try {
			CompletableFuture<T> future = new CompletableFuture<>();
			futures.add(future);
			status = Status.LOADING;
			loader.get().thenAccept(loadedValue -> {
				lock.writeLock().lock();
				value = loadedValue;
				status = Status.LOADED;
				lock.writeLock().unlock();
				while (!futures.isEmpty())
					futures.remove().complete(loadedValue);
			});
			return future;
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	public Status getStatus() {
		return status;
	}
	
}
