package com.luneruniverse.minecraft.mod.nbteditor.screens;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditor;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVScreen;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.util.math.MatrixStack;

public class LoadingScreen extends MVScreen {
	
	private static final int MAX_FREEZE_TIME = 200;
	private static final int MIN_LOADING_TIME = 200; // Prevent flicker
	
	public static <T> void show(CompletableFuture<T> future, Runnable onLoading, BiConsumer<Boolean, T> onFinish, BiConsumer<Boolean, Throwable> onException) {
		try {
			onFinish.accept(false, future.get(MAX_FREEZE_TIME, TimeUnit.MILLISECONDS));
			return;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (ExecutionException e) {
			onException.accept(false, e.getCause());
			return;
		} catch (CancellationException e) {
			onException.accept(false, e);
			return;
		} catch (TimeoutException e) {
			// Expected
		}
		
		onLoading.run();
		MainUtil.client.setScreen(new LoadingScreen(future, value -> onFinish.accept(true, value), e -> onException.accept(true, e)));
	}
	public static <T> void show(CompletableFuture<T> future, Runnable onLoading, BiConsumer<Boolean, T> onFinish) {
		show(future, onLoading, onFinish, (loaded, e) -> NBTEditor.LOGGER.error("Error processing something", e));
	}
	public static <T> void show(CompletableFuture<T> future, Consumer<T> onFinish) {
		show(future, () -> {}, (loaded, value) -> onFinish.accept(value));
	}
	
	private final CompletableFuture<?> future;
	private final Consumer<Object> onFinish;
	private final Consumer<Throwable> onException;
	private final long startTime;
	
	@SuppressWarnings("unchecked")
	private <T> LoadingScreen(CompletableFuture<T> future, Consumer<T> onFinish, Consumer<Throwable> onException) {
		super(TextInst.of("Loading"));
		
		this.future = future;
		this.onFinish = result -> onFinish.accept((T) result);
		this.onException = onException;
		this.startTime = System.currentTimeMillis();
	}
	
	@Override
	protected void init() {
		addDrawableChild(MVMisc.newButton(width / 2 - 75, height / 2, 150, 20, TextInst.translatable("nbteditor.hide"), btn -> close()));
	}
	
	@Override
	public void tick() {
		super.tick();
		
		if (future.isDone() && System.currentTimeMillis() - startTime >= MIN_LOADING_TIME) {
			try {
				onFinish.accept(future.get());
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} catch (ExecutionException e) {
				onException.accept(e.getCause());
			} catch (CancellationException e) {
				onException.accept(e);
			}
		}
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		super.renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
		MainUtil.renderLogo(matrices);
		
		MVDrawableHelper.drawCenteredTextWithShadow(matrices, textRenderer, TextInst.translatable("nbteditor.loading"),
				width / 2, height / 2 - textRenderer.fontHeight / 2 - 10, -1);
	}
	
}
