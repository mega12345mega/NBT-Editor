package com.luneruniverse.minecraft.mod.nbteditor.screens;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget.TooltipSupplier;
import net.minecraft.client.option.SimpleOption.TooltipFactoryGetter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

public class SimpleTooltip implements TooltipSupplier {
	
	public static <T> TooltipFactoryGetter<T> of(Text... msg) {
		List<OrderedText> tooltip = new SimpleTooltip(null, msg).msg.stream().map(Text::asOrderedText).toList();
		return client -> value -> tooltip;
	}
	public static <T> TooltipFactoryGetter<T> of(String... msg) {
		List<OrderedText> tooltip = new SimpleTooltip(null, msg).msg.stream().map(Text::asOrderedText).toList();
		return client -> value -> tooltip;
	}
	public static <T> TooltipFactoryGetter<T> empty() {
		List<OrderedText> tooltip = List.of();
		return client -> value -> tooltip;
	}
	
	
	
	private final Screen screen;
	private final List<Text> msg;
	
	public SimpleTooltip(Screen screen, Text... msg) {
		this.screen = screen;
		this.msg = new ArrayList<>();
		for (Text line : msg) {
			Arrays.asList(line.getString().split("\n")).stream().map(part -> Text.literal(part).fillStyle(line.getStyle()))
					.forEach(this.msg::add);
		}
	}
	public SimpleTooltip(Screen screen, String... keys) {
		this(screen, Arrays.asList(keys).stream().map(Text::translatable).toList().toArray(new MutableText[0]));
	}
	
	@Override
	public void onTooltip(ButtonWidget btn, MatrixStack matrices, int mouseX, int mouseY) {
		screen.renderTooltip(matrices, msg, mouseX, mouseY);
	}
	
}
