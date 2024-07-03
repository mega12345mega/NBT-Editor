package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import com.luneruniverse.minecraft.mod.nbteditor.server.NBTEditorServer;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.registry.DynamicRegistryManager;

public class DynamicRegistryManagerHolder {
	
	private static final DynamicRegistryManager defaultManager = DynamicRegistryManager.of(MVRegistry.REGISTRIES.getInternalValue());
	private static DynamicRegistryManager serverManager;
	
	public static DynamicRegistryManager get() {
		if (NBTEditorServer.IS_DEDICATED)
			return serverManager == null ? defaultManager : serverManager;
		
		ClientPlayNetworkHandler networkHandler = MainUtil.client.getNetworkHandler();
		return networkHandler == null ? defaultManager : networkHandler.getRegistryManager();
	}
	
	public static void setServerManager(DynamicRegistryManager serverManager) {
		DynamicRegistryManagerHolder.serverManager = serverManager;
	}
	
}
