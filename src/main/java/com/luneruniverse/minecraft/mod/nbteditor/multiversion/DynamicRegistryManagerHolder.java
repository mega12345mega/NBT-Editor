package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.lang.invoke.MethodType;
import java.util.ArrayList;
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
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryLoader;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagGroupLoader;
import net.minecraft.resource.LifecycledResourceManagerImpl;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReload;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.MinecraftServer;

public class DynamicRegistryManagerHolder {
	
	private static final CompletableFutureCache<DynamicRegistryManager> defaultManagerCache =
			new CompletableFutureCache<>(DynamicRegistryManagerHolder::loadDefaultManagerImpl);
	private static final Set<Thread> defaultManagerForced = ConcurrentHashMap.newKeySet();
	private static volatile RegistryCache defaultManagerRegistryCache;
	
	private static volatile DynamicRegistryManager clientManager;
	private static volatile DynamicRegistryManager serverManager;
	
	private static CompletableFuture<DynamicRegistryManager> loadDefaultManagerImpl() {
		CompletableFuture<DynamicRegistryManager> future = new CompletableFuture<>();
		MixinLink.executeCrashableTask(() -> {
			if (MainUtil.client.getResourcePackManager().getEnabledProfiles().isEmpty())
				MainUtil.client.getResourcePackManager().scanPacks();
			
			// Based on https://github.com/MineLittlePony/HDSkins/blob/f9c6b8e570cae03908598eb629bf92e2f4faf5b3/src/main/java/com/minelittlepony/hdskins/client/gui/player/DummyNetworkHandler.java#L49
			
			CombinedDynamicRegistries<ServerDynamicRegistryType> combinedRegistries =
					ServerDynamicRegistryType.createCombinedDynamicRegistries();
			ResourceManager resourceManager = new LifecycledResourceManagerImpl(
					ResourceType.SERVER_DATA, MainUtil.client.getResourcePackManager().createResourcePacks());
			
			List<Registry.PendingTagLoad<?>> tags = TagGroupLoader.startReload(resourceManager, combinedRegistries.get(ServerDynamicRegistryType.STATIC));
			DynamicRegistryManager.Immutable preceding = combinedRegistries.getPrecedingRegistryManagers(ServerDynamicRegistryType.RELOADABLE);
			List<RegistryWrapper.Impl<?>> loadedRegistries = TagGroupLoader.collectRegistries(preceding, tags);
			
			List<RegistryLoader.Entry<?>> entries = new ArrayList<>();
			entries.addAll(RegistryLoader.DYNAMIC_REGISTRIES);
			entries.addAll(RegistryLoader.DIMENSION_REGISTRIES);
			DynamicRegistryManager.Immutable dynamicRegistries = RegistryLoader.loadFromResource(resourceManager, loadedRegistries, entries);
			
			future.complete(combinedRegistries.with(ServerDynamicRegistryType.RELOADABLE, dynamicRegistries).getCombinedRegistryManager());
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
				return future.isDone() ? 1 : 0;
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
		
		if (MixinLink.isOnMainThread() && defaultManagerCache.getStatus() != CompletableFutureCache.Status.LOADED)
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
	
	private static final boolean getReadOnlyWrapperExists = Version.<Boolean>newSwitch()
			.range("1.21.2", null, false)
			.range(null, "1.21.1", true)
			.get();
	private static final Supplier<Reflection.MethodInvoker> Registry_getReadOnlyWrapper =
			Reflection.getOptionalMethod(Registry.class, "method_46771", MethodType.methodType(RegistryWrapper.Impl.class));
	public static <T> boolean isOwnedByDefaultManager(RegistryEntry.Reference<T> entry) {
		if (NBTEditorServer.isOnServerThread() || defaultManagerCache.getStatus() != CompletableFutureCache.Status.LOADED)
			return false;
		
		if (defaultManagerRegistryCache == null)
			defaultManagerRegistryCache = new RegistryCache(defaultManagerCache.get().join());
		
		@SuppressWarnings("unchecked")
		Registry<T> registry = (Registry<T>) defaultManagerRegistryCache.getRegistry(entry.registryKey().getRegistry()).orElse(null);
		if (registry == null)
			return false;
		
		// Check for static registries
		// Attempting to convert references in static registries to the current registry manager
		// causes a stack overflow as the reference isn't changed
		if (Registries.REGISTRIES.get(registry.getKey().getValue()) != null) {
			return false;
		}
		
		return entry.owner.ownerEquals(getReadOnlyWrapperExists ? Registry_getReadOnlyWrapper.get().invoke(registry) : registry);
	}
	
}
