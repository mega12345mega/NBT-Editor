package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import net.minecraft.client.gui.ParentElement;

/**
 * Changes {@link ParentElement} to use the pre 1.19.3 behavior <br>
 * This makes {@link ParentElement#mouseClicked(double, double, int)} short-circuit on the first true <br>
 * Since this introduces issues with text fields, text fields are automatically properly unfocused
 */
public interface OldEventBehavior {
	
}
