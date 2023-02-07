package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import net.minecraft.client.gui.ParentElement;

/**
 * Changes {@link ParentElement} to use the pre 1.19.3 behavior <br>
 * This makes {@link ParentElement#mouseClicked(double, double, int)} short-circuit on the first true
 */
public interface OldEventBehavior {
	
}
