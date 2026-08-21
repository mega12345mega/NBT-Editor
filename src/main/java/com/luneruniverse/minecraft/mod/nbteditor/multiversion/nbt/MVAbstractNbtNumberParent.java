package com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt;

public interface MVAbstractNbtNumberParent {
	
	public default byte nbte$byteValue() {
		throw new RuntimeException("Missing implementation for MVAbstractNbtNumberParent#nbte$byteValue");
	}
	
	public default short nbte$shortValue() {
		throw new RuntimeException("Missing implementation for MVAbstractNbtNumberParent#nbte$shortValue");
	}
	
	public default int nbte$intValue() {
		throw new RuntimeException("Missing implementation for MVAbstractNbtNumberParent#nbte$intValue");
	}
	
	public default long nbte$longValue() {
		throw new RuntimeException("Missing implementation for MVAbstractNbtNumberParent#nbte$longValue");
	}
	
	public default float nbte$floatValue() {
		throw new RuntimeException("Missing implementation for MVAbstractNbtNumberParent#nbte$floatValue");
	}
	
	public default double nbte$doubleValue() {
		throw new RuntimeException("Missing implementation for MVAbstractNbtNumberParent#nbte$doubleValue");
	}
	
	public default Number nbte$numberValue() {
		throw new RuntimeException("Missing implementation for MVAbstractNbtNumberParent#nbte$numberValue");
	}
	
}
