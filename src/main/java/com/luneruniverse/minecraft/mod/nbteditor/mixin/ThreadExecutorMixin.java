package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import java.util.WeakHashMap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.luneruniverse.minecraft.mod.nbteditor.NBTEditor;
import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;

import net.minecraft.util.thread.ThreadExecutor;

@Mixin(ThreadExecutor.class)
public class ThreadExecutorMixin {
	private static final Cache<Runnable, Exception> stackTraces = CacheBuilder.newBuilder().weakKeys().build();
	
	@Inject(method = "send", at = @At("HEAD"))
	@Group(name = "send", min = 1)
	private void send_new(Runnable runnable, CallbackInfo info) {
		stackTraces.put(runnable, new Exception("Stack trace"));
	}
	@Inject(method = "method_16901(Ljava/lang/Object;)V", at = @At("HEAD"), remap = false)
	@Group(name = "send", min = 1)
	@SuppressWarnings("target")
	private void send_old(Object runnable, CallbackInfo info) {
		stackTraces.put((Runnable) runnable, new Exception("Stack trace"));
	}
	
	private final WeakHashMap<Thread, Runnable> executeTask_task = new WeakHashMap<>();
	@Inject(method = "executeTask", at = @At("HEAD"))
	private void executeTask(Runnable task, CallbackInfo info) {
		executeTask_task.put(Thread.currentThread(), task);
	}
	@ModifyVariable(method = "executeTask", at = @At("STORE"))
	private Exception executeTask(Exception exception) {
		Runnable task = executeTask_task.remove(Thread.currentThread());
		Exception stackTrace = stackTraces.getIfPresent(task);
		if (stackTrace == null)
			NBTEditor.LOGGER.warn("Missing additional #executeTask stack trace for exception");
		else
			exception.addSuppressed(stackTrace);
		if (MixinLink.CATCH_BYPASSING_TASKS.remove(task) != null) {
			if (exception instanceof RuntimeException e)
				throw e;
			throw new RuntimeException("Failed to execute crashable task", exception); // Impossible
		}
		return exception;
	}
}
