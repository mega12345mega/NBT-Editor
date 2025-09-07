package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.lang.invoke.MethodType;
import java.util.function.Supplier;

import com.mojang.blaze3d.opengl.GlStateManager;

public class MVGlStateManager {
	
	private static final boolean OPEN_GL = Version.<Boolean>newSwitch()
			.range("1.21.5", null, true)
			.range(null, "1.21.4", false)
			.get();
	
	private static final Supplier<Class<?>> platform_GlStateManager =
			Reflection.getOptionalClass("com.mojang.blaze3d.platform.GlStateManager");
	
	private static final Supplier<Reflection.MethodInvoker> GlStateManager__disableCull =
			Reflection.getOptionalMethod(platform_GlStateManager, () -> "_disableCull", () -> MethodType.methodType(void.class));
	public static void _disableCull() {
		if (OPEN_GL)
			GlStateManager._disableCull();
		else
			GlStateManager__disableCull.get().invoke(null);
	}
	
	private static final Supplier<Reflection.MethodInvoker> GlStateManager__enableCull =
			Reflection.getOptionalMethod(platform_GlStateManager, () -> "_enableCull", () -> MethodType.methodType(void.class));
	public static void _enableCull() {
		if (OPEN_GL)
			GlStateManager._enableCull();
		else
			GlStateManager__enableCull.get().invoke(null);
	}
	
	private static final Supplier<Reflection.MethodInvoker> GlStateManager__disableDepthTest =
			Reflection.getOptionalMethod(platform_GlStateManager, () -> "_disableDepthTest", () -> MethodType.methodType(void.class));
	public static void _disableDepthTest() {
		if (OPEN_GL)
			GlStateManager._disableDepthTest();
		else
			GlStateManager__disableDepthTest.get().invoke(null);
	}
	
	private static final Supplier<Reflection.MethodInvoker> GlStateManager__enableDepthTest =
			Reflection.getOptionalMethod(platform_GlStateManager, () -> "_enableDepthTest", () -> MethodType.methodType(void.class));
	public static void _enableDepthTest() {
		if (OPEN_GL)
			GlStateManager._enableDepthTest();
		else
			GlStateManager__enableDepthTest.get().invoke(null);
	}
	
	private static final Supplier<Reflection.MethodInvoker> GlStateManager__colorMask =
			Reflection.getOptionalMethod(platform_GlStateManager, () -> "_colorMask", () -> MethodType.methodType(void.class, boolean.class, boolean.class, boolean.class, boolean.class));
	public static void _colorMask(boolean red, boolean green, boolean blue, boolean alpha) {
		if (OPEN_GL)
			GlStateManager._colorMask(red, green, blue, alpha);
		else
			GlStateManager__colorMask.get().invoke(null, red, green, blue, alpha);
	}
	
	private static final Supplier<Class<?>> GlStateManager$ScissorTestState = Reflection.getOptionalClass("com.mojang.blaze3d.platform.GlStateManager$class_5518");
	private static final Supplier<Class<?>> GlStateManager$CapabilityTracker = Reflection.getOptionalClass("com.mojang.blaze3d.platform.GlStateManager$class_1018");
	private static final Supplier<Reflection.FieldReference> GlStateManager_SCISSOR =
			Reflection.getOptionalField(platform_GlStateManager, () -> "SCISSOR", () -> "Lcom/mojang/blaze3d/platform/GlStateManager$class_5518;");
	private static final Supplier<Reflection.FieldReference> GlStateManager$ScissorTestState_capState =
			Reflection.getOptionalField(GlStateManager$ScissorTestState, () -> "field_26840", () -> "Lcom/mojang/blaze3d/platform/GlStateManager$class_1018;");
	private static final Supplier<Reflection.FieldReference> GlStateManager$CapabilityTracker_state =
			Reflection.getOptionalField(GlStateManager$CapabilityTracker, () -> "field_5051", () -> "Z");
	public static boolean isScissorEnabled() {
		if (OPEN_GL)
			return GlStateManager.SCISSOR.capState.state;
		else
			return GlStateManager$CapabilityTracker_state.get().get(GlStateManager$ScissorTestState_capState.get().get(GlStateManager_SCISSOR.get().get(null)));
	}
	
}
