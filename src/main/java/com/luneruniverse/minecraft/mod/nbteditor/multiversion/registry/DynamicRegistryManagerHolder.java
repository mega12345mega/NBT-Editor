package com.luneruniverse.minecraft.mod.nbteditor.multiversion.registry;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.server.NBTEditorServer;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;

public class DynamicRegistryManagerHolder {
	
	private static final Set<Thread> defaultManagerForced = ConcurrentHashMap.newKeySet();
	private static volatile DynamicRegistryManager clientManager;
	private static volatile DynamicRegistryManager serverManager;
	
	public static DynamicRegistryManager getManager() {
		if (NBTEditorServer.isOnServerThread()) {
			if (serverManager == null)
				throw new IllegalStateException("The server manager hasn't been set yet!");
			return serverManager;
		}
		
		if (hasClientManager())
			return clientManager;
		
		if (MixinLink.isOnMainThread() && !DefaultRegistryManager.get().isDone())
			throw new RuntimeException("Cannot synchronously load the default manager on the main thread");
		return DefaultRegistryManager.get().join();
	}
	public static RegistryWrapper.WrapperLookup get() {
		return getManager();
	}
	
	public static void setClientManager(PacketListener listener) {
		clientManager = (listener == null ? null : ((ClientPlayNetworkHandler) listener).getRegistryManager());
		DefaultRegistryManager.setClientManager(clientManager);
	}
	public static void setServerManager(MinecraftServer server) {
		serverManager = server.getRegistryManager();
	}
	
	public static boolean hasClientManager() {
		return !defaultManagerForced.contains(Thread.currentThread()) && clientManager != null;
	}
	
	public static <T> T withDefaultManager(Supplier<T> callback) {
		if (NBTEditorServer.isOnServerThread())
			throw new IllegalStateException("Cannot use withDefaultManager on the server!");
		
		defaultManagerForced.add(Thread.currentThread());
		try {
			return callback.get();
		} finally {
			defaultManagerForced.remove(Thread.currentThread());
		}
	}
	public static void withDefaultManager(Runnable callback) {
		withDefaultManager(() -> {
			callback.run();
			return null;
		});
	}
	
}
