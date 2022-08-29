package com.luneruniverse.minecraft.mod.nbteditor;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.NoSuchElementException;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.Identifier;

public class InternalItems {
	
	public static void load() {} // Load the class
	
	private static ItemStack getItem(String name) {
		try {
			NbtCompound nbt = NbtIo.read(new DataInputStream(MinecraftClient.getInstance().getResourceManager()
					.getResource(new Identifier("nbteditor", "internalitems/" + name + ".nbt")).orElseThrow().getInputStream()));
			return ItemStack.fromNbt(nbt);
		} catch (IOException | NoSuchElementException e) {
			NBTEditor.LOGGER.error("Error while loading internal item '" + name + "'", e);
			return null;
		}
	}
	
	
	
	public static final ItemStack COLOR_CODES = getItem("colorcodes");
	
}
