package com.luneruniverse.minecraft.mod.nbteditor.screens;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.lwjgl.opengl.GL20;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.EditableText;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget.TooltipSupplier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Matrix4f;

public class SimpleTooltip implements TooltipSupplier {
	
	private final List<Text> msg;
	
	public SimpleTooltip(Text... msg) {
		this.msg = new ArrayList<>();
		for (Text line : msg) {
			Arrays.asList(line.getString().split("\n")).stream().map(part -> TextInst.literal(part).fillStyle(line.getStyle()))
					.forEach(this.msg::add);
		}
	}
	public SimpleTooltip(String... keys) {
		this(Arrays.asList(keys).stream().map(TextInst::translatable).toList().toArray(new EditableText[0]));
	}
	
	@Override
	public void onTooltip(ButtonWidget btn, MatrixStack matrices, int mouseX, int mouseY) {
		// Undo translations and render at actual position
		// This allows Screen#renderTooltip to adjust for window height
		Matrix4f matrix = matrices.peek().getPositionMatrix();
		FloatBuffer buffer = FloatBuffer.allocate(16);
		matrix.writeColumnMajor(buffer);
		float[] translation = new float[3];
		buffer.get(12, translation);
		matrices.push();
		matrices.translate(-translation[0], -translation[1], 0);
		boolean scissor = GL20.glGetBoolean(GL20.GL_SCISSOR_TEST);
		if (scissor)
			GL20.glDisable(GL20.GL_SCISSOR_TEST);
		
		MainUtil.client.currentScreen.renderTooltip(matrices, msg, mouseX + (int) translation[0], mouseY + (int) translation[1]);
		
		if (scissor)
			GL20.glEnable(GL20.GL_SCISSOR_TEST);
		matrices.pop();
	}
	
}
