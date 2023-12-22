package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.lwjgl.opengl.GL20;

import com.luneruniverse.minecraft.mod.nbteditor.util.TextUtil;

import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

public class MVTooltip {
	
	public static final MVTooltip EMPTY = new MVTooltip(new Text[0]);
	private static boolean oneTooltip = false;
	private static boolean lastTooltip = false;
	private static MVTooltip theOneTooltip;
	
	public static MVTooltip setOneTooltip(boolean oneTooltip, boolean lastTooltip) {
		MVTooltip.oneTooltip = oneTooltip;
		MVTooltip.lastTooltip = lastTooltip;
		MVTooltip output = theOneTooltip;
		theOneTooltip = null;
		return output;
	}
	public static boolean isOneTooltip() {
		return oneTooltip;
	}
	public static boolean isLastTooltip() {
		return lastTooltip;
	}
	public static MVTooltip getTheOneTooltip() {
		return theOneTooltip;
	}
	public static boolean setExternalOneTooltip(List<OrderedText> tooltip) {
		if (isOneTooltip()) {
			if (lastTooltip || theOneTooltip == null)
				theOneTooltip = new MVTooltip(tooltip, null);
			return true;
		}
		return false;
	}
	
	private static Text combine(List<Text> lines) {
		EditableText combined = TextInst.literal("");
		for (int i = 0; i < lines.size(); i++) {
			if (i > 0)
				combined = combined.append(" ");
			combined = combined.append(lines.get(i));
		}
		return combined;
	}
	
	private final List<OrderedText> lines;
	private final Text combined;
	
	private MVTooltip(List<OrderedText> lines, Text combined) {
		this.lines = lines;
		this.combined = combined;
	}
	public MVTooltip(List<Text> lines) {
		this(lines.stream().map(Text::asOrderedText).collect(Collectors.toList()), combine(lines));
	}
	public MVTooltip(Text... lines) {
		this(Arrays.stream(lines).flatMap(line -> TextUtil.splitText(line).stream()).toList());
	}
	public MVTooltip(String... keys) {
		this(Arrays.asList(keys).stream().map(TextInst::translatable).toList().toArray(new EditableText[0]));
	}
	
	public List<OrderedText> getLines() {
		return lines;
	}
	
	public Text getCombined() {
		return combined;
	}
	
	public boolean isEmpty() {
		return this == EMPTY || lines.isEmpty();
	}
	
	Tooltip toNewTooltip() {
		if (isEmpty())
			return null;
		
		Tooltip output = Tooltip.of(combined);
		Reflection.getField(Tooltip.class, "field_41103", "Ljava/util/List;").set(output, lines);
		return output;
	}
	Object toOldTooltip() {
		if (isEmpty())
			return Reflection.getField(ButtonWidget.class, "field_25035", "Lnet/minecraft/class_4185$class_5316;").get(null); // ButtonWidget.EMPTY
		
		return Proxy.newProxyInstance(MVMisc.class.getClassLoader(),
				new Class<?>[] {Reflection.getClass("net.minecraft.class_4185$class_5316")}, (obj, method, args) -> {
			if (args.length == 1) // supply
				return null;
			if (args.length != 4) // onTooltip
				throw new RuntimeException("Unexpected method call: " + method.getName());
			render((MatrixStack) args[1], (int) args[2], (int) args[3]);
			return null;
		});
	}
	
	public void render(MatrixStack matrices, int mouseX, int mouseY) {
		if (oneTooltip) {
			if (lastTooltip || theOneTooltip == null)
				theOneTooltip = this;
			return;
		}
		
		// Undo translations and render at actual position
		// This allows Screen#renderTooltip to adjust for window height
		float[] translation = MVMisc.getTranslation(matrices);
		matrices.push();
		matrices.translate(-translation[0], -translation[1], 0.0);
		boolean scissor = GL20.glGetBoolean(GL20.GL_SCISSOR_TEST);
		if (scissor)
			GL20.glDisable(GL20.GL_SCISSOR_TEST);
		
		MVDrawableHelper.renderTooltip(matrices, lines, mouseX + (int) translation[0], mouseY + (int) translation[1]);
		
		if (scissor)
			GL20.glEnable(GL20.GL_SCISSOR_TEST);
		matrices.pop();
	}
	
}
