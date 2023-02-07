package com.luneruniverse.minecraft.mod.nbteditor.addons;

/**
 * Addon mods should implement this class
 */
public interface NBTEditorAddon {
	
	/**
	 * Called when NBT Editor is done initializing<br>
	 * Use {@link NBTEditorAPI} to register most of your features here
	 */
	public void onInit();
	
}
