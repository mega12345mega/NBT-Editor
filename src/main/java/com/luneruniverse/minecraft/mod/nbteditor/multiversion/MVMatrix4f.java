package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.lang.invoke.MethodType;
import java.nio.FloatBuffer;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import org.joml.Matrix4f;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.UncheckedExecutionException;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

public class MVMatrix4f {
	
	public static final Class<?> Matrix4f_class = Version.<Class<?>>newSwitch()
			.range("1.19.3", null, () -> Reflection.getClass("org.joml.Matrix4f"))
			.range(null, "1.19.2", () -> Reflection.getClass("net.minecraft.class_1159"))
			.get();
	public static final Class<?> Matrix4fc_class = Version.<Class<?>>newSwitch()
			.range("1.19.3", null, () -> Reflection.getClass("org.joml.Matrix4fc"))
			.range(null, "1.19.2", Matrix4f_class)
			.get();
	
	private static final Reflection.MethodInvoker MatrixStack_Entry_getPositionMatrix =
			Reflection.getMethod(MatrixStack.Entry.class, "method_23761", MethodType.methodType(Matrix4f_class));
	public static MVMatrix4f getPositionMatrix(MatrixStack.Entry matrix) {
		return new MVMatrix4f(MatrixStack_Entry_getPositionMatrix.<Object>invoke(matrix));
	}
	
	private static final Supplier<Reflection.MethodInvoker> Matrix4f_writeColumnMajor =
			Reflection.getOptionalMethod(Matrix4f_class, "method_4932", MethodType.methodType(void.class, FloatBuffer.class));
	public static float[] getTranslation(MatrixStack matrices) {
		Object matrix = getPositionMatrix(matrices.peek()).getInternalValue();
		return Version.<float[]>newSwitch()
				.range("1.19.3", null, () -> {
					Vector3f output = ((Matrix4f) matrix).getColumn(3, new Vector3f());
					return new float[] {output.x, output.y, output.z};
				})
				.range(null, "1.19.2", () -> {
					FloatBuffer buffer = FloatBuffer.allocate(16);
					Matrix4f_writeColumnMajor.get().invoke(matrix, buffer); // matrix.writeColumnMajor(buffer)
					float[] output = new float[3];
					buffer.get(12, output);
					return output;
				})
				.get();
	}
	
	private static final Supplier<Reflection.MethodInvoker> Matrix4f_scale =
			Reflection.getOptionalMethod(Matrix4f_class, "method_24019", MethodType.methodType(Matrix4f_class, float.class, float.class, float.class));
	public static MVMatrix4f ofScale(float x, float y, float z) {
		return new MVMatrix4f(Version.<Object>newSwitch()
				.range("1.19.3", null, () -> {
					Object matrix = Reflection.newInstance(Matrix4f_class, new Class<?>[] {});
					((Matrix4f) matrix).scale(x, y, z);
					return matrix;
				})
				.range(null, "1.19.2", () -> Matrix4f_scale.get().invoke(null, x, y, z))
				.get());
	}
	
	private static final Supplier<Reflection.MethodInvoker> Matrix4f_loadIdentity =
			Reflection.getOptionalMethod(Matrix4f_class, "method_22668", MethodType.methodType(void.class));
	private static Object newMatrix4f() {
		Object matrix = Reflection.newInstance(Matrix4f_class, new Class<?>[] {});
		Version.newSwitch()
				.range("1.19.3", null, () -> {})
				.range(null, "1.19.2", () -> Matrix4f_loadIdentity.get().invoke(matrix))
				.run();
		return matrix;
	}
	
	private static Object toMatrix4f(MVQuaternionf quat) {
		return Version.<Object>newSwitch()
				.range("1.19.3", null, () -> {
					Object matrix = Reflection.newInstance(Matrix4f_class, new Class<?>[] {});
					((Matrix4f) matrix).set((Quaternionfc) quat.getInternalValue());
					return matrix;
				})
				.range(null, "1.19.2", () -> Reflection.newInstance(Matrix4f_class, new Class<?>[] {MVQuaternionf.Quaternionf_class}, quat.getInternalValue()))
				.get();
	}
	
	private Object value;
	
	public MVMatrix4f(Object value) {
		this.value = value;
	}
	
	public MVMatrix4f() {
		this(newMatrix4f());
	}
	public MVMatrix4f(MVQuaternionf quat) {
		this(toMatrix4f(quat));
	}
	
	public Object getInternalValue() {
		return value;
	}
	
	// Wrapper handler
	private final Cache<String, Reflection.MethodInvoker> methodCache = CacheBuilder.newBuilder().build();
	@SuppressWarnings("unchecked")
	private <R> R call(String oldMethod, String newMethod, MethodType type, Object... args) {
		String method = Version.<String>newSwitch()
				.range("1.19.3", null, () -> newMethod)
				.range(null, "1.19.2", () -> oldMethod)
				.get();
		try {
			return (R) methodCache.get(method, () -> Reflection.getMethod(Matrix4f_class, method, type)).invoke(value, args);
		} catch (ExecutionException | UncheckedExecutionException e) {
			throw new RuntimeException("Error invoking method", e);
		}
	}
	
	public MVMatrix4f multiply(MVMatrix4f right) {
		call("method_22672", "mul", MethodType.methodType(
				Version.<Class<?>>newSwitch()
						.range("1.19.3", null, () -> Matrix4f_class)
						.range(null, "1.19.2", void.class)
						.get(),
				Matrix4fc_class), right);
		return this;
	}
	
	public MVMatrix4f multiply(MVQuaternionf right) {
		return multiply(new MVMatrix4f(right));
	}
	
	public MVMatrix4f scale(float x, float y, float z) {
		return multiply(ofScale(x, y, z));
	}
	
	private static final Supplier<Reflection.MethodInvoker> Matrix4f_copy =
			Reflection.getOptionalMethod(Matrix4f_class, "method_22673", MethodType.methodType(Matrix4f_class));
	public MVMatrix4f copy() {
		return new MVMatrix4f(Version.<Object>newSwitch()
				.range("1.19.3", null, () -> Reflection.newInstance(Matrix4f_class, new Class<?>[] {Matrix4fc_class}, value)) // new Matrix4f((Matrix4f) matrix)
				.range(null, "1.19.2", () -> Matrix4f_copy.get().invoke(value))
				.get());
	}
	
	private static final Supplier<Reflection.MethodInvoker> MatrixStack_multiplyPositionMatrix =
			Reflection.getOptionalMethod(MatrixStack.class, "method_34425", MethodType.methodType(void.class, Matrix4f_class));
	public void applyToPositionMatrix(MatrixStack matrices) {
		Version.newSwitch()
				.range("1.19.3", null, () -> matrices.multiplyPositionMatrix((Matrix4f) value))
				.range(null, "1.19.2", () -> MatrixStack_multiplyPositionMatrix.get().invoke(matrices, value))
				.run();
	}
	
	private static final Reflection.MethodInvoker VertexConsumer_vertex =
			Reflection.getMethod(VertexConsumer.class, "method_22918", MethodType.methodType(VertexConsumer.class, Matrix4f_class, float.class, float.class, float.class));
	public VertexConsumer applyToVertex(VertexConsumer buffer, float x, float y, float z) {
		return VertexConsumer_vertex.invoke(buffer, value, x, y, z);
	}
	
}
