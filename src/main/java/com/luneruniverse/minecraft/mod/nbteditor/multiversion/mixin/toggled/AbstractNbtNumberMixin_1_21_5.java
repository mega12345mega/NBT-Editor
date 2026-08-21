package com.luneruniverse.minecraft.mod.nbteditor.multiversion.mixin.toggled;

import org.spongepowered.asm.mixin.Mixin;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.MVAbstractNbtNumberParent;

import net.minecraft.nbt.AbstractNbtNumber;

@Mixin(AbstractNbtNumber.class)
public interface AbstractNbtNumberMixin_1_21_5 extends MVAbstractNbtNumberParent {
	
	public default byte nbte$byteValue() {
		return ((AbstractNbtNumber) (Object) this).byteValue();
	}
	
	public default short nbte$shortValue() {
		return ((AbstractNbtNumber) (Object) this).shortValue();
	}
	
	public default int nbte$intValue() {
		return ((AbstractNbtNumber) (Object) this).intValue();
	}
	
	public default long nbte$longValue() {
		return ((AbstractNbtNumber) (Object) this).longValue();
	}
	
	public default float nbte$floatValue() {
		return ((AbstractNbtNumber) (Object) this).floatValue();
	}
	
	public default double nbte$doubleValue() {
		return ((AbstractNbtNumber) (Object) this).doubleValue();
	}
	
	public default Number nbte$numberValue() {
		return ((AbstractNbtNumber) (Object) this).numberValue();
	}
	
}
