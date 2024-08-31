package com.luneruniverse.minecraft.mod.nbteditor.multiversion.mixin;

import java.lang.invoke.MethodType;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVComponentType;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.IntegratedNBTManager;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.MVItemStackParent;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.NBTManagers;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

@Mixin(ItemStack.class)
public class ItemStackMixin implements IntegratedNBTManager, MVItemStackParent {
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
	
	
	private static final Supplier<Reflection.MethodInvoker> ItemStack_hasCustomName =
			Reflection.getOptionalMethod(ItemStack.class, "method_7938", MethodType.methodType(boolean.class));
	@Override
	public boolean manager$hasCustomName() {
		if (NBTManagers.COMPONENTS_EXIST)
			return ((ItemStack) (Object) this).contains(MVComponentType.CUSTOM_NAME);
		else
			return ItemStack_hasCustomName.get().invoke(this);
	}
	private static final Supplier<Reflection.MethodInvoker> ItemStack_setCustomName =
			Reflection.getOptionalMethod(ItemStack.class, "method_7977", MethodType.methodType(ItemStack.class, Text.class));
	@Override
	public ItemStack manager$setCustomName(Text name) {
		if (NBTManagers.COMPONENTS_EXIST)
			((ItemStack) (Object) this).set(MVComponentType.CUSTOM_NAME, name);
		else
			ItemStack_setCustomName.get().invoke(this, name);
		return (ItemStack) (Object) this;
	}
}
