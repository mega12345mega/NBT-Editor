package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.UnaryOperator;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.util.Formatting;

/**
 * A wrapper for MutableText, since it is changed from an interface (1.18) to a class (1.19)
 */
public class EditableText implements Text {
	
	private MutableText value;
	
	public EditableText(MutableText value) {
		this.value = value;
	}
	
	public MutableText getInternalValue() {
		return value;
	}
	
	// Wrapper handler
	private final Cache<String, Reflection.MethodInvoker> methodCache = CacheBuilder.newBuilder().build();
	@SuppressWarnings("unchecked")
	private <R> R call(boolean mutable, String method, MethodType type, Object... args) {
		try {
			Object output = methodCache.get(method, () -> Reflection.getMethod(mutable ? MutableText.class : Text.class, method, type)).invoke(value, args);
			if (output instanceof MutableText && mutable) {
				if (output == value)
					output = this;
				else
					output = new EditableText(value);
			}
			return (R) output;
		} catch (ExecutionException e) {
			throw new RuntimeException("Error invoking method", e);
		}
	}
	
	// Text
	@Override
	public OrderedText asOrderedText() {
		return call(false, "method_30937", MethodType.methodType(OrderedText.class));
	}
	
	@Override
	public TextContent getContent() {
		return call(false, "method_10851", MethodType.methodType(TextContent.class));
	}
	
	@Override
	public List<Text> getSiblings() {
		return call(false, "method_10855", MethodType.methodType(List.class));
	}
	
	@Override
	public Style getStyle() {
		return call(false, "method_10866", MethodType.methodType(Style.class));
	}
	
	// 1.18 Text
	public String method_10851() { // asString
		return call(false, "method_10851", MethodType.methodType(String.class));
	}
	
	public MutableText method_27662() { // copy
		return call(false, "method_27662", MethodType.methodType(MutableText.class));
	}
	
	public MutableText method_27661() { // shallowCopy
		return call(false, "method_27661", MethodType.methodType(MutableText.class));
	}
	
	public <T> Optional<T> method_27660(StringVisitable.StyledVisitor<T> visitor, Style style) { // visitSelf
		return call(false, "method_27660", MethodType.methodType(Optional.class, StringVisitable.StyledVisitor.class, Style.class), visitor, style);
	}
	
	public <T> Optional<T> method_27659(StringVisitable.Visitor<T> visitor) { // visitSelf
		return call(false, "method_27659", MethodType.methodType(Optional.class, StringVisitable.Visitor.class), visitor);
	}
	
	// Mutable Text
	public EditableText setStyle(Style style) {
		return call(true, "method_10862", MethodType.methodType(MutableText.class, Style.class), style);
	}
	
	public EditableText append(String text) {
		return call(true, "method_27693", MethodType.methodType(MutableText.class, String.class), text);
	}
	
	public EditableText append(Text text) {
		return call(true, "method_10852", MethodType.methodType(MutableText.class, Text.class), text);
	}
	
	public EditableText styled(UnaryOperator<Style> styleUpdater) {
		return call(true, "method_27694", MethodType.methodType(MutableText.class, UnaryOperator.class), styleUpdater);
	}
	
	public EditableText fillStyle(Style styleOverride) {
		return call(true, "method_27696", MethodType.methodType(MutableText.class, Style.class), styleOverride);
	}
	
	public EditableText formatted(Formatting... formattings) {
		return call(true, "method_27695", MethodType.methodType(MutableText.class, Formatting[].class), (Object) formattings);
	}
	
	// Other
	@Override
	public boolean equals(Object obj) {
		return value.equals(obj);
	}
	
}
