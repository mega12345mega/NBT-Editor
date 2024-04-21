package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import java.util.concurrent.locks.ReadWriteLock;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.luneruniverse.minecraft.mod.nbteditor.misc.ResetableDataTracker;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.entity.data.DataTracker;

@Mixin(DataTracker.class)
public class DataTrackerMixin implements ResetableDataTracker {
	@Shadow
	private Int2ObjectMap<DataTracker.Entry<?>> entries;
	@Shadow
	private ReadWriteLock lock;
	@Override
	public void reset() {
		lock.writeLock().lock();
		try {
			for (DataTracker.Entry<?> entry : entries.values()) {
				resetEntry(entry);
				entry.setDirty(true);
			}
		} finally {
			lock.writeLock().unlock();
		}
	}
	private <T> void resetEntry(DataTracker.Entry<T> entry) {
		entry.set(entry.initialValue);
	}
}
