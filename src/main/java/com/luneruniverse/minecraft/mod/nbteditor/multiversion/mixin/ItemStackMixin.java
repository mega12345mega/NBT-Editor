package com.luneruniverse.minecraft.mod.nbteditor.multiversion.mixin;

import java.lang.invoke.MethodType;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVComponentType;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.MVItemStackParent;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.subjectio.IntegratedSubjectIO;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.subjectio.SubjectIOs;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

@Mixin(ItemStack.class)
public class ItemStackMixin implements IntegratedSubjectIO, MVItemStackParent {
	@Override
	public NbtCompound nbte$serialize(boolean requireSuccess) {
		return SubjectIOs.ITEM.serialize((ItemStack) (Object) this, requireSuccess);
	}
	
	@Override
	public boolean nbte$hasNbt() {
		return SubjectIOs.ITEM.hasNbt((ItemStack) (Object) this);
	}
	@Override
	public NbtCompound nbte$getNbt() {
		return SubjectIOs.ITEM.getNbt((ItemStack) (Object) this);
	}
	@Override
	public NbtCompound nbte$getOrCreateNbt() {
		return SubjectIOs.ITEM.getOrCreateNbt((ItemStack) (Object) this);
	}
	@Override
	public void nbte$setNbt(NbtCompound nbt) {
		SubjectIOs.ITEM.setNbt((ItemStack) (Object) this, nbt);
	}
	
	
	private static final Supplier<Reflection.MethodInvoker> ItemStack_hasCustomName =
			Reflection.getOptionalMethod(ItemStack.class, "method_7938", MethodType.methodType(boolean.class));
	@Override
	public boolean nbte$hasCustomName() {
		if (SubjectIOs.COMPONENTS_EXIST)
			return ((ItemStack) (Object) this).contains(MVComponentType.CUSTOM_NAME);
		else
			return ItemStack_hasCustomName.get().invoke(this);
	}
	private static final Supplier<Reflection.MethodInvoker> ItemStack_setCustomName =
			Reflection.getOptionalMethod(ItemStack.class, "method_7977", MethodType.methodType(ItemStack.class, Text.class));
	@Override
	public ItemStack nbte$setCustomName(Text name) {
		if (SubjectIOs.COMPONENTS_EXIST)
			((ItemStack) (Object) this).set(MVComponentType.CUSTOM_NAME, name);
		else
			ItemStack_setCustomName.get().invoke(this, name);
		return (ItemStack) (Object) this;
	}
}
