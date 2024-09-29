package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.server.NBTEditorServer;
import com.luneruniverse.minecraft.mod.nbteditor.util.CompletableFutureCache;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryLoader;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.LifecycledResourceManagerImpl;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReload;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.SimpleResourceReload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;

public class DynamicRegistryManagerHolder {
	
	private static final DynamicRegistryManager basicDefaultManager =
			DynamicRegistryManager.of(MVRegistry.REGISTRIES.getInternalValue());
	private static final CompletableFutureCache<DynamicRegistryManager> defaultManagerCache =
			new CompletableFutureCache<>(DynamicRegistryManagerHolder::loadDefaultManagerImpl);
	private static volatile ResourceReload defaultManagerResourcesMonitor;
	private static final Set<Thread> defaultManagerForced = ConcurrentHashMap.newKeySet();
	
	private static volatile DynamicRegistryManager clientManager;
	private static volatile DynamicRegistryManager serverManager;
	
	private static CompletableFuture<DynamicRegistryManager> loadDefaultManagerImpl() {
		CompletableFuture<DynamicRegistryManager> future = new CompletableFuture<>();
		MixinLink.executeCrashableTask(() -> {
			if (MainUtil.client.getResourcePackManager().getEnabledProfiles().isEmpty())
				MainUtil.client.getResourcePackManager().scanPacks();
			
			ResourceManager resourceManager = new LifecycledResourceManagerImpl(
					ResourceType.SERVER_DATA, MainUtil.client.getResourcePackManager().createResourcePacks());
			defaultManagerResourcesMonitor = SimpleResourceReload.start(resourceManager, List.of(new RecipeManager(basicDefaultManager)),
					Util.getMainWorkerExecutor(), MainUtil.client, CompletableFuture.completedFuture(Unit.INSTANCE), false);
			
			defaultManagerResourcesMonitor.whenComplete().thenRun(() -> future.complete(
					RegistryLoader.loadFromResource(resourceManager, basicDefaultManager, RegistryLoader.DYNAMIC_REGISTRIES)));
		});
		return future;
	}
	public static ResourceReload loadDefaultManager() {
		CompletableFuture<DynamicRegistryManager> future = defaultManagerCache.get();
		
		return new ResourceReload() {
			@Override
			public CompletableFuture<?> whenComplete() {
				return future;
			}
			@Override
			public float getProgress() {
				if (defaultManagerResourcesMonitor == null)
					return 0;
				if (future.isDone())
					return 1;
				// Registries need to be loaded after resources are
				return defaultManagerResourcesMonitor.getProgress() * 0.5f;
			}
		};
	}
	public static void onDefaultManagerLoad(Runnable callback) {
		defaultManagerCache.get().whenComplete((manager, e) -> MixinLink.executeCrashableTask(callback));
	}
	
	public static DynamicRegistryManager getManager() {
		if (NBTEditorServer.isOnServerThread()) {
			if (serverManager == null)
				throw new IllegalStateException("The server manager hasn't been set yet!");
			return serverManager;
		}
		
		if (hasClientManager())
			return clientManager;
		
		if (MainUtil.client.isOnThread() && defaultManagerCache.getStatus() != CompletableFutureCache.Status.LOADED)
			throw new RuntimeException("Cannot synchronously load the default manager on the main thread");
		return defaultManagerCache.get().join();
	}
	public static RegistryWrapper.WrapperLookup get() {
		return getManager();
	}
	
	public static void setClientManager(PacketListener listener) {
		clientManager = (listener == null ? null : ((ClientPlayNetworkHandler) listener).getRegistryManager());
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
	
	public static <T> boolean isOwnedByDefaultManager(RegistryEntry.Reference<T> entry) {
		if (NBTEditorServer.isOnServerThread() || defaultManagerCache.getStatus() != CompletableFutureCache.Status.LOADED)
			return false;
		
		return withDefaultManager(() -> {
			return entry.getKey()
					.map(RegistryKey::getRegistryRef)
					.flatMap(registryKey -> getManager().getOptionalWrapper(registryKey))
					.map(entry.owner::ownerEquals)
					.orElse(false);
		});
	}
	
}
