package com.luneruniverse.minecraft.mod.nbteditor.async;

import java.io.IOException;
import java.io.OutputStream;
import java.util.OptionalInt;
import java.util.WeakHashMap;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistry;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class ItemSize {
	
	private static class ByteCountingOutputStream extends OutputStream {
		private int count;
		public int getCount() {
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
	
	private static final WeakHashMap<ItemStack, OptionalInt> uncompressedSizes = new WeakHashMap<>();
	private static final WeakHashMap<ItemStack, OptionalInt> compressedSizes = new WeakHashMap<>();
	
	public static OptionalInt getItemSize(ItemStack stack, boolean compressed) {
		if (!stack.hasNbt()) {
			return OptionalInt.of(calcItemSize(stack, compressed));
		}
		WeakHashMap<ItemStack, OptionalInt> sizes = (compressed ? compressedSizes : uncompressedSizes);
		OptionalInt size;
		synchronized (sizes) {
			size = sizes.get(stack);
			if (size != null)
				return size;
			size = OptionalInt.empty();
			sizes.put(stack, size);
		}
		Thread thread = new Thread(() -> {
			int knownSize = calcItemSize(stack, compressed);
			synchronized (sizes) {
				sizes.put(stack, OptionalInt.of(knownSize));
			}
		}, "Item Size Processor [" + MVRegistry.ITEM.getId(stack.getItem()) + "]");
		thread.setDaemon(true);
		thread.start();
		return size;
	}
	
	private static int calcItemSize(ItemStack stack, boolean compressed) {
		ByteCountingOutputStream stream = new ByteCountingOutputStream();
		try {
			NbtCompound nbt = stack.writeNbt(new NbtCompound());
			if (compressed)
				MVMisc.writeCompressedNbt(nbt, stream);
			else
				MVMisc.writeNbt(nbt, stream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return stream.getCount();
	}
	
}
