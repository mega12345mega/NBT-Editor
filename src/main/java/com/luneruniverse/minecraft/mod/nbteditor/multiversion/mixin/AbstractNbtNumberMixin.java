package com.luneruniverse.minecraft.mod.nbteditor.multiversion.mixin;

import org.spongepowered.asm.mixin.Mixin;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.MVAbstractNbtNumberParent;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.MVNbtCompoundParent;

import net.minecraft.nbt.AbstractNbtNumber;

@Mixin(AbstractNbtNumber.class)
public interface AbstractNbtNumberMixin extends MVAbstractNbtNumberParent {
	
	public default byte nbte$byteValue() {
		if (MVNbtCompoundParent.NBT_CODE_REFACTORED)
			return ((AbstractNbtNumber) (Object) this).byteValue();
		return AbstractNbtNumber_byteValue.get().invoke(this);
	}
	
	public default short nbte$shortValue() {
		if (MVNbtCompoundParent.NBT_CODE_REFACTORED)
			return ((AbstractNbtNumber) (Object) this).shortValue();
		return AbstractNbtNumber_shortValue.get().invoke(this);
	}
	
	public default int nbte$intValue() {
		if (MVNbtCompoundParent.NBT_CODE_REFACTORED)
			return ((AbstractNbtNumber) (Object) this).intValue();
		return AbstractNbtNumber_intValue.get().invoke(this);
	}
	
	public default long nbte$longValue() {
		if (MVNbtCompoundParent.NBT_CODE_REFACTORED)
			return ((AbstractNbtNumber) (Object) this).longValue();
		return AbstractNbtNumber_longValue.get().invoke(this);
	}
	
	public default float nbte$floatValue() {
		if (MVNbtCompoundParent.NBT_CODE_REFACTORED)
			return ((AbstractNbtNumber) (Object) this).floatValue();
		return AbstractNbtNumber_floatValue.get().invoke(this);
	}
	
	public default double nbte$doubleValue() {
		if (MVNbtCompoundParent.NBT_CODE_REFACTORED)
			return ((AbstractNbtNumber) (Object) this).doubleValue();
		return AbstractNbtNumber_doubleValue.get().invoke(this);
	}
	
	public default Number nbte$numberValue() {
		if (MVNbtCompoundParent.NBT_CODE_REFACTORED)
			return ((AbstractNbtNumber) (Object) this).numberValue();
		return AbstractNbtNumber_numberValue.get().invoke(this);
	}
	
}
