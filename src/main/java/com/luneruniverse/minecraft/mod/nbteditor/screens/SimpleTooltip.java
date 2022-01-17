package com.luneruniverse.minecraft.mod.nbteditor.screens;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget.TooltipSupplier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class SimpleTooltip implements TooltipSupplier {
	
	private final Screen screen;
	private final List<Text> msg;
	
	public SimpleTooltip(Screen screen, Text... msg) {
		this.screen = screen;
		this.msg = new ArrayList<>();
		for (Text line : msg) {
			Arrays.asList(line.getString().split("\n")).stream().map(part -> new LiteralText(part).fillStyle(line.getStyle()))
					.forEach(this.msg::add);
		}
	}
	public SimpleTooltip(Screen screen, String... keys) {
		this(screen, Arrays.asList(keys).stream().map(TranslatableText::new).toList().toArray(new TranslatableText[0]));
	}
	
	@Override
	public void onTooltip(ButtonWidget btn, MatrixStack matrices, int mouseX, int mouseY) {
		screen.renderTooltip(matrices, msg, mouseX, mouseY);
	}
	
}
