package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.lang.invoke.MethodType;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import org.joml.Quaternionf;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.UncheckedExecutionException;

import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;

public class MVQuaternionf {
	
	public static final Class<?> Quaternionf_class = Version.<Class<?>>newSwitch()
			.range("1.19.3", null, () -> Reflection.getClass("org.joml.Quaternionf"))
			.range(null, "1.19.2", () -> Reflection.getClass("net.minecraft.class_1158"))
			.get();
	public static final Class<?> Quaternionfc_class = Version.<Class<?>>newSwitch()
			.range("1.19.3", null, () -> Reflection.getClass("org.joml.Quaternionfc"))
			.range(null, "1.19.2", Quaternionf_class)
			.get();
	private static final Class<?> return_class = Version.<Class<?>>newSwitch()
			.range("1.19.3", null, () -> Quaternionf_class)
			.range(null, "1.19.2", void.class)
			.get();
	
	public static MVQuaternionf ofAxisRotation(float angle, float x, float y, float z) {
		return new MVQuaternionf(
				Version.<Object>newSwitch()
						.range("1.19.3", null, () -> {
							Object quat = Reflection.newInstance(Quaternionf_class, new Class<?>[] {});
							((Quaternionf) quat).rotationAxis(angle, x, y, z);
							return quat;
						})
						.range(null, "1.19.2", () -> {
							Class<?> Vec3f_class = Reflection.getClass("net.minecraft.class_1160");
							Object axis = Reflection.newInstance(Vec3f_class, new Class<?>[] {float.class, float.class, float.class}, x, y, z);
							return Reflection.newInstance(Quaternionf_class, new Class<?>[] {Vec3f_class, float.class, boolean.class}, axis, angle, false);
						})
						.get());
	}
	public static MVQuaternionf ofXRotation(float angle) {
		return ofAxisRotation(angle, 1, 0, 0);
	}
	public static MVQuaternionf ofYRotation(float angle) {
		return ofAxisRotation(angle, 0, 1, 0);
	}
	public static MVQuaternionf ofZRotation(float angle) {
		return ofAxisRotation(angle, 0, 0, 1);
	}
	
	private Object value;
	
	public MVQuaternionf(Object value) {
		this.value = value;
	}
	
	public MVQuaternionf() {
		this(Reflection.newInstance(Quaternionf_class, new Class<?>[] {float.class, float.class, float.class, float.class}, 0, 0, 0, 1));
	}
	
	public Object getInternalValue() {
		return value;
	}
	
	private final Cache<String, Reflection.MethodInvoker> methodCache = CacheBuilder.newBuilder().build();
	@SuppressWarnings("unchecked")
	private <R> R call(String oldMethod, String newMethod, Supplier<MethodType> type, Object... args) {
		String method = Version.<String>newSwitch()
				.range("1.19.3", null, () -> newMethod)
				.range(null, "1.19.2", () -> oldMethod)
				.get();
		try {
			return (R) methodCache.get(method, () -> Reflection.getMethod(Quaternionf_class, method, type.get())).invoke(value, args);
		} catch (ExecutionException | UncheckedExecutionException e) {
			throw new RuntimeException("Error invoking method", e);
		}
	}
	
	public MVQuaternionf multiply(MVQuaternionf right) {
		call("method_4925", "mul", () -> MethodType.methodType(return_class, Quaternionfc_class), right.getInternalValue());
		return this;
	}
	
	public MVQuaternionf rotateAxis(float angle, float x, float y, float z) {
		return multiply(ofAxisRotation(angle, x, y, z));
	}
	public MVQuaternionf rotateX(float angle) {
		return multiply(ofXRotation(angle));
	}
	public MVQuaternionf rotateY(float angle) {
		return multiply(ofYRotation(angle));
	}
	public MVQuaternionf rotateZ(float angle) {
		return multiply(ofZRotation(angle));
	}
	
	public MVQuaternionf conjugate() {
		call("method_4926", "conjugate", () -> MethodType.methodType(return_class));
		return this;
	}
	
	private static final Supplier<Reflection.MethodInvoker> Quaternionf_copy =
			Reflection.getOptionalMethod(Quaternionf_class, "method_23695", MethodType.methodType(Quaternionf_class));
	public MVQuaternionf copy() {
		return new MVQuaternionf(Version.<Object>newSwitch()
				.range("1.19.3", null, () -> Reflection.newInstance(Quaternionf_class, new Class<?>[] {Quaternionfc_class}, value))
				.range(null, "1.19.2", () -> Quaternionf_copy.get().invoke(value))
				.get());
	}
	
	private static final Supplier<Reflection.MethodInvoker> MatrixStack_multiply =
			Reflection.getOptionalMethod(MatrixStack.class, "method_22907", MethodType.methodType(void.class, Quaternionf_class));
	public void applyToMatrixStack(MatrixStack matrices) {
		Version.newSwitch()
				.range("1.19.3", null, () -> matrices.multiply((Quaternionf) value))
				.range(null, "1.19.2", () -> MatrixStack_multiply.get().invoke(matrices, value))
				.run();
	}
	
	private static final Supplier<Reflection.MethodInvoker> EntityRenderDispatcher_setRotation =
			Reflection.getOptionalMethod(EntityRenderDispatcher.class, "method_24196", MethodType.methodType(void.class, Quaternionf_class));
	public void applyToEntityRenderDispatcher(EntityRenderDispatcher dispatcher) {
		Version.newSwitch()
				.range("1.19.3", null, () -> dispatcher.setRotation((Quaternionf) value))
				.range(null, "1.19.2", () -> EntityRenderDispatcher_setRotation.get().invoke(dispatcher, value))
				.run();
	}
	
}
