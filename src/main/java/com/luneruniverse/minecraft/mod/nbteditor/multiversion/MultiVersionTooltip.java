package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL20;

import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class MultiVersionTooltip {
	
	public static final MultiVersionTooltip EMPTY = new MultiVersionTooltip(new Text[0]);
	
	
	private final List<Text> lines;
	
	public MultiVersionTooltip(List<Text> lines) {
		this.lines = lines;
	}
	public MultiVersionTooltip(Text... lines) {
		this.lines = new ArrayList<>();
		for (Text line : lines) {
			Arrays.asList(line.getString().split("\n")).stream().map(part -> TextInst.literal(part).fillStyle(line.getStyle()))
					.forEach(this.lines::add);
		}
	}
	public MultiVersionTooltip(String... keys) {
		this(Arrays.asList(keys).stream().map(TextInst::translatable).toList().toArray(new EditableText[0]));
	}
	
	public List<Text> getLines() {
		return lines;
	}
	
	public boolean isEmpty() {
		return this == EMPTY || lines.isEmpty();
	}
	
	Tooltip toNewTooltip() {
		if (isEmpty())
			return null;
		
		EditableText combined = TextInst.literal("");
		for (int i = 0; i < lines.size(); i++) {
			if (i > 0)
				combined = combined.append(" ");
			combined = combined.append(lines.get(i));
		}
		Tooltip output = Tooltip.of(combined);
		// output.lines = lines
		try {
			Field field = Tooltip.class.getDeclaredField(Reflection.getFieldName(Tooltip.class, "field_41103", "Ljava/util/List;"));
			field.setAccessible(true);
			field.set(output, lines.stream().map(Text::asOrderedText).collect(Collectors.toList()));
		} catch (Exception e) {
			throw new RuntimeException("Error setting tooltip lines", e);
		}
		return output;
	}
	Object toOldTooltip() {
		if (isEmpty())
			return Reflection.getField(ButtonWidget.class, null, "field_25035", "Lnet/minecraft/class_4185$class_5316;"); // ButtonWidget.EMPTY
		
		return Proxy.newProxyInstance(MultiVersionMisc.class.getClassLoader(),
				new Class<?>[] {Reflection.getClass("net.minecraft.class_4185$class_5316")}, (obj, method, args) -> {
			if (args.length == 1) // supply
				return null;
			if (args.length != 4) // onTooltip
				throw new RuntimeException("Unexpected method call: " + method.getName());
			render((MatrixStack) args[1], (int) args[2], (int) args[3]);
			return null;
		});
	}
	
	private static final Supplier<Reflection.MethodInvoker> Matrix4f_writeColumnMajor =
			Reflection.getOptionalMethod(MultiVersionMisc.Matrix4f_class, "method_4932", MethodType.methodType(void.class, FloatBuffer.class));
	public void render(MatrixStack matrices, int mouseX, int mouseY) {
		// Undo translations and render at actual position
		// This allows Screen#renderTooltip to adjust for window height
		Object matrix = MultiVersionMisc.getPositionMatrix(matrices.peek());
		float[] translation = switch (Version.get()) {
			case v1_19_3 -> {
				Vector3f output = ((Matrix4f) matrix).getColumn(3, new Vector3f());
				yield new float[] {output.x, output.y, output.z};
			}
			case v1_19, v1_18 -> {
				FloatBuffer buffer = FloatBuffer.allocate(16);
				Matrix4f_writeColumnMajor.get().invoke(matrix, buffer); // matrix.writeColumnMajor(buffer)
				float[] output = new float[3];
				buffer.get(12, output);
				yield output;
			}
		};
		matrices.push();
		matrices.translate(-translation[0], -translation[1], 0.0);
		boolean scissor = GL20.glGetBoolean(GL20.GL_SCISSOR_TEST);
		if (scissor)
			GL20.glDisable(GL20.GL_SCISSOR_TEST);
		
		MainUtil.client.currentScreen.renderTooltip(matrices, lines, mouseX + (int) translation[0], mouseY + (int) translation[1]);
		
		if (scissor)
			GL20.glEnable(GL20.GL_SCISSOR_TEST);
		matrices.pop();
	}
	
}
