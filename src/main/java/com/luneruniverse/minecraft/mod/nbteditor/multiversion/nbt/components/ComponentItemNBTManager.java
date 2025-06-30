package com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.components;

import java.util.Map;
import java.util.Optional;

import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.DynamicRegistryManagerHolder;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.IdentifierInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.Attempt;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.DeserializableNBTManager;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;

import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;

public class ComponentItemNBTManager implements DeserializableNBTManager<ItemStack> {
	
	@Override
	public Attempt<NbtCompound> trySerialize(ItemStack subject) {
		if (subject.isEmpty())
			return new Attempt<>(new NbtCompound());
		
		DataResult<NbtElement> result = ItemStack.CODEC.encodeStart(
				DynamicRegistryManagerHolder.get().getOps(NbtOps.INSTANCE), subject);
		return new Attempt<>(
				result.resultOrPartial().map(nbt -> (NbtCompound) nbt.copy()),
				result.error().map(DataResult.Error::message).orElse(null));
	}
	@Override
	public Attempt<ItemStack> tryDeserialize(NbtCompound nbt) {
		if (nbt.contains("id", NbtElement.STRING_TYPE) &&
				IdentifierInst.of(nbt.getString("id")).equals(IdentifierInst.of("minecraft", "air")))
			return new Attempt<>(ItemStack.EMPTY);
		if (nbt.contains("count", NbtElement.INT_TYPE) && nbt.getInt("count") <= 0)
			return new Attempt<>(ItemStack.EMPTY);
		
		DataResult<Pair<ItemStack, NbtElement>> result = ItemStack.OPTIONAL_CODEC.decode(
				DynamicRegistryManagerHolder.get().getOps(NbtOps.INSTANCE), nbt.copy());
		return new Attempt<>(
				result.resultOrPartial().map(Pair::getFirst).map(item -> {
					if (item.contains(DataComponentTypes.MAX_DAMAGE) && item.getOrDefault(DataComponentTypes.MAX_STACK_SIZE, 1) > 1)
						item.remove(DataComponentTypes.MAX_DAMAGE);
					return item;
				}),
				result.error().map(DataResult.Error::message).orElse(null));
	}
	
	@Override
	public boolean hasNbt(ItemStack subject) {
		return !subject.getComponentChanges().isEmpty();
	}
	@Override
	public NbtCompound getNbt(ItemStack subject) {
		return (NbtCompound) ComponentChanges.CODEC.encodeStart(
				DynamicRegistryManagerHolder.get().getOps(NbtOps.INSTANCE), subject.getComponentChanges()).getOrThrow().copy();
	}
	@Override
	public NbtCompound getOrCreateNbt(ItemStack subject) {
		return getNbt(subject);
	}
	@Override
	public void setNbt(ItemStack subject, NbtCompound nbt) {
		ComponentChanges components = ComponentChanges.CODEC.decode(
				DynamicRegistryManagerHolder.get().getOps(NbtOps.INSTANCE), nbt.copy()).getPartialOrThrow().getFirst();
		Optional<? extends Integer> maxDamage = components.get(DataComponentTypes.MAX_DAMAGE);
		Optional<? extends Integer> maxStackSize = components.get(DataComponentTypes.MAX_STACK_SIZE);
		if (maxDamage != null && maxDamage.isPresent() &&
				(maxStackSize == null ?
						subject.getDefaultComponents().get(DataComponentTypes.MAX_STACK_SIZE) > 1 :
						maxStackSize.isPresent() && maxStackSize.get() > 1)) {
			components = components.withRemovedIf(component -> component == DataComponentTypes.MAX_DAMAGE);
		}
		MixinLink.setChanges(subject, components);
	}
	
	@Override
	public String getNbtString(ItemStack subject) {
		ComponentChanges components = subject.getComponentChanges();
		StringBuilder builder = new StringBuilder("[");
		boolean first = true;
		for (Map.Entry<ComponentType<?>, Optional<?>> entry : components.entrySet()) {
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
	private <T> DataResult<NbtElement> encodeComponent(ComponentType<T> component, Object value) {
		return component.getCodecOrThrow().encodeStart(
				DynamicRegistryManagerHolder.get().getOps(NbtOps.INSTANCE), (T) value);
	}
	
}
