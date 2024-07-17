package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class MVDrawableHelper {
	
	private static final Map<MatrixStack, DrawContext> drawContexts = Collections.synchronizedMap(new WeakHashMap<>());
	public static MatrixStack getMatrices(DrawContext context) {
		MatrixStack matrices = context.getMatrices();
		drawContexts.put(matrices, context);
		return matrices;
	}
	public static DrawContext getDrawContext(MatrixStack matrices) {
		return drawContexts.get(matrices);
	}
	
	public static void super_render(Class<?> callerClass, Drawable caller, MatrixStack matrices, int mouseX, int mouseY, float delta) {
		try {
			Class<?> matrixType;
			Object matrixValue;
			if (Version.<Boolean>newSwitch()
					.range("1.20.0", null, true)
					.range(null, "1.19.4", false)
					.get()) {
				matrixType = DrawContext.class;
				matrixValue = getDrawContext(matrices);
			} else {
				matrixType = MatrixStack.class;
				matrixValue = matrices;
			}
			MethodType methodType = MethodType.methodType(void.class, matrixType, int.class, int.class, float.class);
			String methodName = Reflection.getMethodName(Drawable.class, "method_25394", methodType);
			MethodHandles.privateLookupIn(callerClass, MethodHandles.lookup()).findSpecial(callerClass.getSuperclass(),
					methodName, methodType, callerClass).invokeWithArguments(caller, matrixValue, mouseX, mouseY, delta);
		} catch (Throwable e) {
			throw new RuntimeException("Error calling super.render (" + callerClass.getName() + ")", e);
		}
	}
	
	private static final Supplier<Reflection.MethodInvoker> Drawable_render =
			Reflection.getOptionalMethod(Drawable.class, "method_25394", MethodType.methodType(void.class, MatrixStack.class, int.class, int.class, float.class));
	public static void render(Drawable caller, MatrixStack matrices, int mouseX, int mouseY, float delta) {
		Version.newSwitch()
				.range("1.20.0", null, () -> caller.render(MVDrawableHelper.getDrawContext(matrices), mouseX, mouseY, delta))
				.range(null, "1.19.4", () -> Drawable_render.get().invoke(caller, matrices, mouseX, mouseY, delta))
				.run();
	}
	
	
	private static final Cache<String, Reflection.MethodInvoker> methodCache = CacheBuilder.newBuilder().build();
	@SuppressWarnings("unchecked")
	private static <R> R call(String method, Class<?> rtype, Class<?>[] ptypes, MatrixStack matrices, Object... args) {
		try {
			DrawContext context;
			MethodType type;
			if (Version.<Boolean>newSwitch()
					.range("1.20.0", null, true)
					.range(null, "1.19.4", false)
					.get()) {
				context = MVDrawableHelper.getDrawContext(matrices);
				type = MethodType.methodType(rtype, ptypes);
			} else {
				context = null;
				type = MethodType.methodType(rtype, MatrixStack.class, ptypes);
				Object[] newArgs = new Object[args.length + 1];
				newArgs[0] = matrices;
				System.arraycopy(args, 0, newArgs, 1, args.length);
				args = newArgs;
			}
			return (R) methodCache.get(method, () -> Reflection.getMethod(DrawContext.class, method, type)).invoke(context, args);
		} catch (ExecutionException | UncheckedExecutionException e) {
			throw new RuntimeException("Error invoking method", e);
		}
	}
	
	
	public static void fill(MatrixStack matrices, int x1, int y1, int x2, int y2, int color) {
		call("method_25294", void.class, new Class<?>[] {int.class, int.class, int.class, int.class, int.class}, matrices, x1, y1, x2, y2, color);
	}
	
	public static void drawText(MatrixStack matrices, TextRenderer textRenderer, Text text, int x, int y, int color, boolean shadow) {
		if (shadow)
			drawTextWithShadow(matrices, textRenderer, text, x, y, color);
		else
			drawTextWithoutShadow(matrices, textRenderer, text, x, y, color);
	}
	
	private static final Supplier<Reflection.MethodInvoker> TextRenderer_draw =
			Reflection.getOptionalMethod(TextRenderer.class, "method_30883", MethodType.methodType(int.class, MatrixStack.class, Text.class, float.class, float.class, int.class));
	public static void drawTextWithoutShadow(MatrixStack matrices, TextRenderer textRenderer, Text text, int x, int y, int color) {
		Version.newSwitch()
				.range("1.20.0", null, () -> getDrawContext(matrices).drawText(textRenderer, text, x, y, color, false))
				.range(null, "1.19.4", () -> TextRenderer_draw.get().invoke(textRenderer, matrices, text, x, y, color))
				.run();
	}
	
	public static void drawTextWithShadow(MatrixStack matrices, TextRenderer textRenderer, Text text, int x, int y, int color) {
		call("method_27535", Version.<Class<?>>newSwitch()
				.range("1.20.0", null, int.class)
				.range(null, "1.19.4", void.class)
				.get(), new Class<?>[] {TextRenderer.class, Text.class, int.class, int.class, int.class}, matrices, textRenderer, text, x, y, color);
	}
	
	public static void drawCenteredTextWithShadow(MatrixStack matrices, TextRenderer textRenderer, Text text, int x, int y, int color) {
		call("method_27534", void.class, new Class<?>[] {TextRenderer.class, Text.class, int.class, int.class, int.class}, matrices, textRenderer, text, x, y, color);
	}
	
	public static void drawTexture(MatrixStack matrices, Identifier texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
		Version.newSwitch()
				.range("1.20.0", null, () -> getDrawContext(matrices).drawTexture(texture, x, y, u, v, width, height, textureWidth, textureHeight))
				.range(null, "1.19.4", () -> {
					RenderSystem.setShader(GameRenderer::getPositionTexProgram);
					RenderSystem.setShaderTexture(0, texture);
					RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
					call("method_25290", void.class,
							new Class<?>[] {int.class, int.class, float.class, float.class, int.class, int.class, int.class, int.class},
							matrices, x, y, u, v, width, height, textureWidth, textureHeight);
				})
				.run();
	}
	public static void drawTexture(MatrixStack matrices, Identifier texture, int x, int y, float u, float v, int width, int height) {
		drawTexture(matrices, texture, x, y, u, v, width, height, 256, 256);
	}
	
	private static final Supplier<Reflection.MethodInvoker> Screen_renderTooltip_Text =
			Reflection.getOptionalMethod(Screen.class, "method_25424", MethodType.methodType(void.class, MatrixStack.class, Text.class, int.class, int.class));
	public static void renderTooltip(MatrixStack matrices, Text text, int x, int y) {
		Version.newSwitch()
				.range("1.20.0", null, () -> getDrawContext(matrices).drawTooltip(MainUtil.client.textRenderer, text, x, y))
				.range(null, "1.19.4", () -> Screen_renderTooltip_Text.get().invoke(MainUtil.client.currentScreen, matrices, text, x, y))
				.run();
	}
	
	private static final Supplier<Reflection.MethodInvoker> Screen_renderTooltip_List =
			Reflection.getOptionalMethod(Screen.class, "method_25417", MethodType.methodType(void.class, MatrixStack.class, List.class, int.class, int.class));
	public static void renderTooltip(MatrixStack matrices, List<OrderedText> lines, int x, int y) {
		Version.newSwitch()
				.range("1.20.0", null, () -> getDrawContext(matrices).drawOrderedTooltip(MainUtil.client.textRenderer, lines, x, y))
				.range(null, "1.19.4", () -> Screen_renderTooltip_List.get().invoke(MainUtil.client.currentScreen, matrices, lines, x, y))
				.run();
	}
	
	private static final Supplier<Reflection.MethodInvoker> ItemRenderer_renderInGuiWithOverrides_MatrixStack =
			Reflection.getOptionalMethod(ItemRenderer.class, "method_4023", MethodType.methodType(void.class, MatrixStack.class, ItemStack.class, int.class, int.class));
	private static final Supplier<Reflection.MethodInvoker> ItemRenderer_renderGuiItemOverlay_MatrixStack =
			Reflection.getOptionalMethod(ItemRenderer.class, "method_4025", MethodType.methodType(void.class, MatrixStack.class, TextRenderer.class, ItemStack.class, int.class, int.class));
	private static final Supplier<Reflection.MethodInvoker> DrawableHelper_setZOffset =
			Reflection.getOptionalMethod(DrawContext.class, "method_25304", MethodType.methodType(void.class, int.class));
	private static final Supplier<Reflection.FieldReference> ItemRenderer_zOffset =
			Reflection.getOptionalField(ItemRenderer.class, "field_4730", "F");
	private static final Supplier<Reflection.MethodInvoker> ItemRenderer_renderInGuiWithOverrides =
			Reflection.getOptionalMethod(ItemRenderer.class, "method_4023", MethodType.methodType(void.class, ItemStack.class, int.class, int.class));
	private static final Supplier<Reflection.MethodInvoker> ItemRenderer_renderGuiItemOverlay =
			Reflection.getOptionalMethod(ItemRenderer.class, "method_4025", MethodType.methodType(void.class, TextRenderer.class, ItemStack.class, int.class, int.class));
	public static final void renderItem(MatrixStack matrices, float zOffset, boolean setScreenZOffset, ItemStack item, int x, int y) {
		ItemRenderer itemRenderer = MainUtil.client.getItemRenderer();
		TextRenderer textRenderer = MainUtil.client.textRenderer;
		Version.newSwitch()
				.range("1.20.0", null, () -> {
					DrawContext context = getDrawContext(matrices);
					context.drawItem(item, x, y);
					context.drawItemInSlot(textRenderer, item, x, y);
				})
				.range("1.19.4", "1.19.4", () -> {
					ItemRenderer_renderInGuiWithOverrides_MatrixStack.get().invoke(itemRenderer, matrices, item, x, y);
					ItemRenderer_renderGuiItemOverlay_MatrixStack.get().invoke(itemRenderer, matrices, textRenderer, item, x, y);
				})
				.range(null, "1.19.3", () -> {
					if (setScreenZOffset)
						DrawableHelper_setZOffset.get().invoke(MainUtil.client.currentScreen, (int) zOffset);
					ItemRenderer_zOffset.get().set(itemRenderer, zOffset);
					ItemRenderer_renderInGuiWithOverrides.get().invoke(itemRenderer, item, x, y);
					ItemRenderer_renderGuiItemOverlay.get().invoke(itemRenderer, textRenderer, item, x, y);
					ItemRenderer_zOffset.get().set(itemRenderer, 0.0F);
					if (setScreenZOffset)
						DrawableHelper_setZOffset.get().invoke(MainUtil.client.currentScreen, 0);
				})
				.run();
	}
	
	private static final Supplier<Reflection.MethodInvoker> Screen_renderBackground_MatrixStack =
			Reflection.getOptionalMethod(Screen.class, "method_25420", MethodType.methodType(void.class, MatrixStack.class));
	private static final Supplier<Reflection.MethodInvoker> Screen_renderBackground_DrawContext =
			Reflection.getOptionalMethod(Screen.class, "method_25420", MethodType.methodType(void.class, DrawContext.class));
	public static void renderBackground(Screen screen, MatrixStack matrices) {
		int[] mousePos = MainUtil.getMousePos();
		Version.newSwitch()
				.range("1.20.5", null, () -> screen.renderInGameBackground(getDrawContext(matrices)))
				.range("1.20.2", "1.20.4", () -> screen.renderBackground(getDrawContext(matrices), mousePos[0], mousePos[1], MainUtil.client.getTickDelta()))
				.range("1.20.0", "1.20.1", () -> Screen_renderBackground_DrawContext.get().invoke(screen, MVDrawableHelper.getDrawContext(matrices)))
				.range(null, "1.19.4", () -> Screen_renderBackground_MatrixStack.get().invoke(screen, matrices))
				.run();
	}
	
}
