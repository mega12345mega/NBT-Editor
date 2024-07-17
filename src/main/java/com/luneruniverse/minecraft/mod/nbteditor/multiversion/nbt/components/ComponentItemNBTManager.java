package com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.components;

import java.util.Map;
import java.util.Optional;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.DynamicRegistryManagerHolder;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.DeserializableNBTManager;

import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;

public class ComponentItemNBTManager implements DeserializableNBTManager<ItemStack> {
	
	@Override
	public NbtCompound serialize(ItemStack subject) {
		return (NbtCompound) subject.encodeAllowEmpty(DynamicRegistryManagerHolder.get());
	}
	@Override
	public ItemStack deserialize(NbtCompound nbt) {
		return ItemStack.OPTIONAL_CODEC.decode(NbtOps.INSTANCE, nbt).getPartialOrThrow().getFirst();
	}
	
	@Override
	public boolean hasNbt(ItemStack subject) {
		return !subject.getComponentChanges().isEmpty();
	}
	@Override
	public NbtCompound getNbt(ItemStack subject) {
		return (NbtCompound) ComponentChanges.CODEC.encodeStart(NbtOps.INSTANCE, subject.getComponentChanges()).getOrThrow();
	}
	@Override
	public NbtCompound getOrCreateNbt(ItemStack subject) {
		return getNbt(subject);
	}
	@Override
	public void setNbt(ItemStack subject, NbtCompound nbt) {
		ComponentChanges components = ComponentChanges.CODEC.decode(NbtOps.INSTANCE, nbt).getPartialOrThrow().getFirst();
		subject.getComponentChanges().entrySet().clear();
		subject.applyChanges(components);
	}
	
	@Override
	public String getNbtString(ItemStack subject) {
		ComponentChanges components = subject.getComponentChanges();
		StringBuilder builder = new StringBuilder("[");
		boolean first = true;
		for (Map.Entry<DataComponentType<?>, Optional<?>> entry : components.entrySet()) {
			if (first)
				first = false;
			else
				builder.append(",");
			entry.getValue().ifPresentOrElse(value -> {
				builder.append(entry.getKey());
				builder.append("=");
				builder.append(value);
			}, () -> {
				builder.append("!");
				builder.append(entry.getKey());
			});
		}
		builder.append(']');
		return builder.toString();
	}
	
}
