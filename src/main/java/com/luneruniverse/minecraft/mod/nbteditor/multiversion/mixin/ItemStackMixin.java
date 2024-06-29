package com.luneruniverse.minecraft.mod.nbteditor.multiversion.mixin;

import org.spongepowered.asm.mixin.Mixin;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.IntegratedNBTManager;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.NBTManagers;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

@Mixin(ItemStack.class)
public class ItemStackMixin implements IntegratedNBTManager {
	@Override
	public NbtCompound manager$serialize() {
		return NBTManagers.ITEM.serialize((ItemStack) (Object) this);
	}
	
	@Override
	public boolean manager$hasNbt() {
		return NBTManagers.ITEM.hasNbt((ItemStack) (Object) this);
	}
	@Override
	public NbtCompound manager$getNbt() {
		return NBTManagers.ITEM.getNbt((ItemStack) (Object) this);
	}
	@Override
	public NbtCompound manager$getOrCreateNbt() {
		return NBTManagers.ITEM.getOrCreateNbt((ItemStack) (Object) this);
	}
	@Override
	public void manager$setNbt(NbtCompound nbt) {
		NBTManagers.ITEM.setNbt((ItemStack) (Object) this, nbt);
	}
}
