package com.luneruniverse.minecraft.mod.nbteditor.multiversion.registry;

import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.server.NBTEditorServer;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryLoader;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.registry.entry.RegistryEntryOwner;
import net.minecraft.registry.tag.TagGroupLoader;
import net.minecraft.resource.LifecycledResourceManagerImpl;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReload;
import net.minecraft.resource.ResourceType;

public class DefaultRegistryManager {
	
	private static final Thread THREAD = new Thread(DefaultRegistryManager::loadImpl, "NBTEditor/Async/DefaultRegistryManager");
	private static final CompletableFuture<DynamicRegistryManager> FUTURE = new CompletableFuture<>();
	private static boolean loadStarted;
	private static volatile int stepsComplete;
	private static volatile int stepsTotal;
	private static volatile RegistryCache registryCache;
	
	public static ResourceReload load() {
		synchronized (DefaultRegistryManager.class) {
			if (loadStarted)
				throw new IllegalStateException("The default registry manager is already loading!");
			loadStarted = true;
		}
		
		THREAD.start();
		
		return new ResourceReload() {
			@Override
			public CompletableFuture<?> whenComplete() {
				return FUTURE;
			}
			@Override
			public float getProgress() {
				if (stepsTotal == 0)
					return 0;
				return (float) stepsComplete / stepsTotal;
			}
		};
	}
	
	public static void onLoad(Runnable callback) {
		FUTURE.whenComplete((manager, e) -> MixinLink.executeCrashableTask(callback));
	}
	
	public static CompletableFuture<DynamicRegistryManager> get() {
		return FUTURE;
	}
	
	public static boolean isOnLoadThread() {
		return Thread.currentThread() == THREAD;
	}
	
	private static final Supplier<Reflection.MethodInvoker> RegistryLoader_loadFromResource =
			Reflection.getOptionalMethod(RegistryLoader.class, "method_56515", MethodType.methodType(DynamicRegistryManager.Immutable.class, ResourceManager.class, DynamicRegistryManager.class, List.class));
	private static void loadImpl() {
		boolean loadTags = Version.<Boolean>newSwitch()
				.range("1.21.2", null, true)
				.range("1.20.5", "1.21.1", false)
				.get();
		
		stepsTotal = (loadTags ? 5 : 4);
		
		// Based on https://github.com/MineLittlePony/HDSkins/blob/f9c6b8e570cae03908598eb629bf92e2f4faf5b3/src/main/java/com/minelittlepony/hdskins/client/gui/player/DummyNetworkHandler.java#L49
		// and https://github.com/MineLittlePony/HDSkins/blob/a19fe3b0d7d98019bafc814a8782b7a263d090b9/src/main/java/com/minelittlepony/hdskins/client/gui/player/DummyNetworkHandler.java#L41
		
		CompletableFuture<ResourceManager> resourceManagerFuture = new CompletableFuture<>();
		MixinLink.executeCrashableTask(() -> {
			if (MainUtil.client.getResourcePackManager().getEnabledProfiles().isEmpty())
				MainUtil.client.getResourcePackManager().scanPacks();
			
			resourceManagerFuture.complete(new LifecycledResourceManagerImpl(
					ResourceType.SERVER_DATA, MainUtil.client.getResourcePackManager().createResourcePacks()));
		});
		ResourceManager resourceManager = resourceManagerFuture.join();
		
		stepsComplete++;
		
		CombinedDynamicRegistries<ServerDynamicRegistryType> combinedRegistries =
				ServerDynamicRegistryType.createCombinedDynamicRegistries();
		
		List<RegistryLoader.Entry<?>> entries = new ArrayList<>();
		entries.addAll(RegistryLoader.DYNAMIC_REGISTRIES);
		entries.addAll(RegistryLoader.DIMENSION_REGISTRIES);
		
		stepsComplete++;
		
		DynamicRegistryManager.Immutable dynamicRegistries;
		if (loadTags) {
			List<Registry.PendingTagLoad<?>> tags = TagGroupLoader.startReload(resourceManager, combinedRegistries.get(ServerDynamicRegistryType.STATIC));
			DynamicRegistryManager.Immutable preceding = combinedRegistries.getPrecedingRegistryManagers(ServerDynamicRegistryType.RELOADABLE);
			List<RegistryWrapper.Impl<?>> loadedRegistries = TagGroupLoader.collectRegistries(preceding, tags);
			
			stepsComplete++;
			
			dynamicRegistries = RegistryLoader.loadFromResource(resourceManager, loadedRegistries, entries);
		} else {
			dynamicRegistries = RegistryLoader_loadFromResource.get().invoke(null, resourceManager, combinedRegistries.getCombinedRegistryManager(), entries);
		}
		
		stepsComplete++;
		
		DynamicRegistryManager registryManager = combinedRegistries.with(ServerDynamicRegistryType.RELOADABLE, dynamicRegistries).getCombinedRegistryManager();
		
		stepsComplete++;
		
		FUTURE.complete(registryManager);
	}
	
	public static void setClientManager(DynamicRegistryManager clientManager) {
		for (DefaultRegistryEntry<?> entry : DefaultRegistryEntry.INSTANCES)
			entry.convert(clientManager);
	}
	
	private static final boolean getReadOnlyWrapperExists = Version.<Boolean>newSwitch()
			.range("1.21.2", null, false)
			.range(null, "1.21.1", true)
			.get();
	private static final Supplier<Reflection.MethodInvoker> Registry_getReadOnlyWrapper =
			Reflection.getOptionalMethod(Registry.class, "method_46771", MethodType.methodType(RegistryWrapper.Impl.class));
	public static <T> boolean isOwnedByOnlyDefaultManager(RegistryEntryOwner<T> owner, RegistryKey<T> key) {
		if (NBTEditorServer.isOnServerThread() || !FUTURE.isDone())
			return false;
		
		if (registryCache == null)
			registryCache = new RegistryCache(FUTURE.join());
		
		@SuppressWarnings("unchecked")
		Registry<T> registry = (Registry<T>) registryCache.getRegistry(key.getRegistry()).orElse(null);
		if (registry == null)
			return false;
		
		// Static registries are shared
		if (RegistryCache.isRegistryStatic(registry))
			return false;
		
		return owner.ownerEquals(getReadOnlyWrapperExists ? Registry_getReadOnlyWrapper.get().invoke(registry) : registry);
	}
	
}
