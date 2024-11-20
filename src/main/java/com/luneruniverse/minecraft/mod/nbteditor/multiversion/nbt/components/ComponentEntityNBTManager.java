package com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.components;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.Attempt;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.NBTManager;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;

public class ComponentEntityNBTManager implements NBTManager<Entity> {
	
	@Override
	public Attempt<NbtCompound> trySerialize(Entity subject) {
		NbtCompound nbt = new NbtCompound();
		nbt.putString("id", EntityType.getId(subject.getType()).toString());
		subject.writeNbt(nbt);
		return new Attempt<>(nbt);
	}
	
	@Override
	public boolean hasNbt(Entity subject) {
		return true;
	}
	@Override
	public NbtCompound getNbt(Entity subject) {
		return subject.writeNbt(new NbtCompound());
	}
	@Override
	public NbtCompound getOrCreateNbt(Entity subject) {
		return getNbt(subject);
	}
	@Override
	public void setNbt(Entity subject, NbtCompound nbt) {
		subject.readNbt(nbt);
	}
	
}
