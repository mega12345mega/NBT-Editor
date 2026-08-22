package com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.elementio;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.elementio.list.AbstractNbtListIO;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.elementio.list.OldAbstractNbtListIO;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.elementio.list.NewAbstractNbtListIO;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.elementio.number.AbstractNbtNumberIO;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.elementio.number.OldAbstractNbtNumberIO;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.elementio.number.NewAbstractNbtNumberIO;

public class ElementIOs {
	public static final boolean NBT_REFACTORED;
	public static final AbstractNbtListIO LIST;
	public static final AbstractNbtNumberIO NUMBER;
	
	static {
		NBT_REFACTORED = Version.<Boolean>newSwitch()
				.range("1.21.5", null, true)
				.range(null, "1.21.4", false)
				.get();
		if (NBT_REFACTORED) {
			LIST = new NewAbstractNbtListIO();
			NUMBER = new NewAbstractNbtNumberIO();
		} else {
			LIST = new OldAbstractNbtListIO();
			NUMBER = new OldAbstractNbtNumberIO();
		}
	}
}
