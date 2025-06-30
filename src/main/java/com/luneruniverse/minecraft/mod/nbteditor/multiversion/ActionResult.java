package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

public enum ActionResult {
	/**
	 * Immediately stop calling callbacks and continue with normal behavior
	 */
	SUCCESS,
	/**
	 * Continue calling callbacks (if there are no more callbacks, continue with normal behavior)
	 */
	PASS,
	/**
	 * Immediately stop calling callbacks and cancel normal behavior
	 */
	FAIL
}
