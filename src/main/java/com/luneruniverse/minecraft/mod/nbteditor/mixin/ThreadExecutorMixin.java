package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import java.util.WeakHashMap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import net.minecraft.util.thread.ThreadExecutor;

@Mixin(ThreadExecutor.class)
public class ThreadExecutorMixin {
	private static final Cache<Runnable, Exception> stackTraces = CacheBuilder.newBuilder().weakKeys().build();
	
	@Inject(method = "send", at = @At("HEAD"))
	private void send(Runnable runnable, CallbackInfo info) {
		stackTraces.put(runnable, new Exception("Stack trace"));
	}
	
	private final WeakHashMap<Thread, Runnable> executeTask_task = new WeakHashMap<>();
	@Inject(method = "executeTask", at = @At("HEAD"))
	private void executeTask(Runnable task, CallbackInfo info) {
		executeTask_task.put(Thread.currentThread(), task);
	}
	@ModifyVariable(method = "executeTask", at = @At("STORE"))
	private Exception executeTask(Exception exception) {
		exception.addSuppressed(stackTraces.getIfPresent(executeTask_task.remove(Thread.currentThread())));
		return exception;
	}
}
