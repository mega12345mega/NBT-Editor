package com.luneruniverse.minecraft.mod.nbteditor.containers;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

public class DynamicSizeContainerIO extends MultiTargetContainerIO {
	
	public DynamicSizeContainerIO(Target target) {
		super(target);
	}
	
	@Override
	public boolean isReadable(ItemStack container) {
		return target.getItemsParent(container.copy()).getList("Items", NbtElement.COMPOUND_TYPE).size() <= 27;
	}
	
	@Override
	public ItemStack[] readItems(ItemStack container) {
		List<ItemStack> output = target.getItemsParent(container).getList("Items", NbtElement.COMPOUND_TYPE).stream()
				.map(item -> ItemStack.fromNbt((NbtCompound) item)).collect(Collectors.toList());
		while (output.size() < 27)
			output.add(ItemStack.EMPTY);
		return output.toArray(ItemStack[]::new);
	}
	
	@Override
	public void writeItems(ItemStack container, ItemStack[] contents) {
		target.getItemsParent(container).put("Items", Stream.of(contents)
				.filter(item -> item != null && !item.isEmpty())
				.map(item -> item.writeNbt(new NbtCompound()))
				.reduce(new NbtList(), (list, item) -> {
					list.add(item);
					return list;
				}, (list1, list2) -> {
					list1.addAll(list2);
					return list1;
				}));
	}
	
}
