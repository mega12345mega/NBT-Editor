package com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.hideflags;

import java.util.LinkedHashMap;
import java.util.Map;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistry;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;

import net.minecraft.component.ComponentType;
import net.minecraft.text.Text;

public class TooltipDisplayComponentHideFlag extends HideFlag {
	
	public static final Map<ComponentType<?>, HideFlag> FLAGS = new LinkedHashMap<>();
	static {
		MVRegistry.getComponentsRegistry().getEntrySet().stream()
				.map(component -> Map.entry(component.getKey().toString(), component.getValue()))
				.sorted((a, b) -> a.getKey().compareToIgnoreCase(b.getKey()))
				.forEachOrdered(component -> {
					FLAGS.put(component.getValue(), new TooltipDisplayComponentHideFlag(
							TextInst.of(component.getKey()), component.getValue()));
				});
	}
	
	private final Text name;
	private final ComponentType<?> component;
	
	private TooltipDisplayComponentHideFlag(Text name, ComponentType<?> component) {
		this.name = name;
		this.component = component;
	}
	
	@Override
	public Text getName() {
		return name;
	}
	
	public ComponentType<?> getComponent() {
		return component;
	}
	
}
