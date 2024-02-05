package com.luneruniverse.minecraft.mod.nbteditor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;

public class NBTEditor implements ModInitializer {
	
	public static final Logger LOGGER = LogManager.getLogger("nbteditor");
	public static NBTEditorServer SERVER;
	
	@Override
	public void onInitialize() {
		SERVER = new NBTEditorServer();
	}
	
}
