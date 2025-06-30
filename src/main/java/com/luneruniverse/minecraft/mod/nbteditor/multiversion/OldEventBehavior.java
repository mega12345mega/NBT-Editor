package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import net.minecraft.client.gui.ParentElement;

/**
 * Makes {@link ParentElement#mouseClicked(double, double, int)} short-circuit on the first true <br>
 * Since this introduces issues with text fields, text fields are automatically properly unfocused <br>
 * <br>
 * This matches the behavior in <= 1.21.3, except for 1.19.3 which has no short-circuiting <br>
 * In 1.21.4, the behavior was changed to only click on the first hovered element
 */
public interface OldEventBehavior {
	
}
