package com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.components;

import java.util.Map;
import java.util.Optional;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.DynamicRegistryManagerHolder;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.DeserializableNBTManager;
import com.mojang.serialization.DataResult;

import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Identifier;

public class ComponentItemNBTManager implements DeserializableNBTManager<ItemStack> {
	
	@Override
	public NbtCompound serialize(ItemStack subject) {
		return (NbtCompound) subject.encodeAllowEmpty(DynamicRegistryManagerHolder.get());
	}
	@Override
	public ItemStack deserialize(NbtCompound nbt) {
		if (nbt.contains("id", NbtElement.STRING_TYPE) &&
				new Identifier(nbt.getString("id")).equals(new Identifier("minecraft", "air")))
			return ItemStack.EMPTY;
		if (nbt.contains("count", NbtElement.INT_TYPE) && nbt.getInt("count") <= 0)
			return ItemStack.EMPTY;
		
		ItemStack item = ItemStack.OPTIONAL_CODEC.decode(
				DynamicRegistryManagerHolder.get().getOps(NbtOps.INSTANCE), nbt).getPartialOrThrow().getFirst();
		if (item.contains(DataComponentTypes.MAX_DAMAGE) && item.getOrDefault(DataComponentTypes.MAX_STACK_SIZE, 1) > 1)
			item.remove(DataComponentTypes.MAX_DAMAGE);
		return item;
	}
	
	@Override
	public boolean hasNbt(ItemStack subject) {
		return !subject.getComponentChanges().isEmpty();
	}
	@Override
	public NbtCompound getNbt(ItemStack subject) {
		return (NbtCompound) ComponentChanges.CODEC.encodeStart(
				DynamicRegistryManagerHolder.get().getOps(NbtOps.INSTANCE), subject.getComponentChanges()).getOrThrow();
	}
	@Override
	public NbtCompound getOrCreateNbt(ItemStack subject) {
		return getNbt(subject);
	}
	@Override
	public void setNbt(ItemStack subject, NbtCompound nbt) {
		ComponentChanges components = ComponentChanges.CODEC.decode(
				DynamicRegistryManagerHolder.get().getOps(NbtOps.INSTANCE), nbt).getPartialOrThrow().getFirst();
		Optional<? extends Integer> maxDamage = components.get(DataComponentTypes.MAX_DAMAGE);
		Optional<? extends Integer> maxStackSize = components.get(DataComponentTypes.MAX_STACK_SIZE);
		if (maxDamage != null && maxDamage.isPresent() &&
				(maxStackSize == null ?
						subject.getDefaultComponents().get(DataComponentTypes.MAX_STACK_SIZE) > 1 :
						maxStackSize.isPresent() && maxStackSize.get() > 1)) {
			components = components.withRemovedIf(component -> component == DataComponentTypes.MAX_DAMAGE);
		}
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
				builder.append(encodeComponent(entry.getKey(), value).getPartialOrThrow());
			}, () -> {
				builder.append("!");
				builder.append(entry.getKey());
			});
		}
		builder.append(']');
		return builder.toString();
	}
	@SuppressWarnings("unchecked")
	private <T> DataResult<NbtElement> encodeComponent(DataComponentType<T> component, Object value) {
		return component.getCodecOrThrow().encodeStart(NbtOps.INSTANCE, (T) value);
	}
	
}
