package com.luneruniverse.minecraft.mod.nbteditor.async;

import java.io.IOException;
import java.io.OutputStream;
import java.util.OptionalLong;
import java.util.WeakHashMap;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditor;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistry;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class ItemSize {
	
	private static class ByteCountingOutputStream extends OutputStream {
		private long count;
		public long getCount() {
			return count;
		}
		@Override
		public void write(int b) throws IOException {
			count++;
		}
		@Override
		public void write(byte[] b) throws IOException {
			count += b.length;
		}
		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			count += len;
		}
	}
	
	private static final WeakHashMap<ItemStack, OptionalLong> uncompressedSizes = new WeakHashMap<>();
	private static final WeakHashMap<ItemStack, OptionalLong> compressedSizes = new WeakHashMap<>();
	
	public static OptionalLong getItemSize(ItemStack stack, boolean compressed) {
		if (!stack.manager$hasNbt()) {
			return OptionalLong.of(calcItemSize(stack, compressed));
		}
		WeakHashMap<ItemStack, OptionalLong> sizes = (compressed ? compressedSizes : uncompressedSizes);
		OptionalLong size;
		synchronized (sizes) {
			size = sizes.get(stack);
			if (size != null)
				return size;
			size = OptionalLong.empty();
			sizes.put(stack, size);
		}
		Thread thread = new Thread(() -> {
			long knownSize = calcItemSize(stack, compressed);
			synchronized (sizes) {
				sizes.put(stack, OptionalLong.of(knownSize));
			}
		}, "NBTEditor/Async/ItemSizeProcessor [" + MVRegistry.ITEM.getId(stack.getItem()) + "]");
		thread.setDaemon(true);
		thread.start();
		return size;
	}
	
	private static long calcItemSize(ItemStack stack, boolean compressed) {
		ByteCountingOutputStream stream = new ByteCountingOutputStream();
		try {
			NbtCompound nbt = stack.manager$serialize(true);
			if (compressed)
				MVMisc.writeCompressedNbt(nbt, stream);
			else
				MVMisc.writeNbt(nbt, stream);
		} catch (Exception e) {
			NBTEditor.LOGGER.error("Error while getting the size of an item", e);
		}
		return stream.getCount();
	}
	
}
