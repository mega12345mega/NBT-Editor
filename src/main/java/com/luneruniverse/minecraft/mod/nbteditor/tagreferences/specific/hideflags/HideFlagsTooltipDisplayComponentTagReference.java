package com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.hideflags;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general.TagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.hideflags.HideFlag;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.hideflags.TooltipDisplayComponentHideFlag;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.hideflags.TooltipHideFlag;

import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.item.ItemStack;

public class HideFlagsTooltipDisplayComponentTagReference implements TagReference<Map<HideFlag, Boolean>, ItemStack> {
	
	@Override
	public Map<HideFlag, Boolean> get(ItemStack object) {
		TooltipDisplayComponent display = object.get(DataComponentTypes.TOOLTIP_DISPLAY);
		Map<HideFlag, Boolean> output = new HashMap<>();
		
		output.put(TooltipHideFlag.INSTANCE, display.hideTooltip());
		
		for (Map.Entry<ComponentType<?>, HideFlag> component : TooltipDisplayComponentHideFlag.FLAGS.entrySet())
			output.put(component.getValue(), MVMisc.hiddenComponents(display).contains(component.getKey()));
		
		return output;
	}
	
	@Override
	public void set(ItemStack object, Map<HideFlag, Boolean> value) {
		if (value.isEmpty())
			return;
		
		TooltipDisplayComponent display = object.get(DataComponentTypes.TOOLTIP_DISPLAY);
		boolean hideTooltip = display.hideTooltip();
		LinkedHashSet<ComponentType<?>> hiddenComponents = new LinkedHashSet<>(MVMisc.hiddenComponents(display));
		
		for (Map.Entry<HideFlag, Boolean> flag : value.entrySet()) {
			if (flag.getKey() == TooltipHideFlag.INSTANCE) {
				hideTooltip = flag.getValue();
				continue;
			}
			
			ComponentType<?> component = ((TooltipDisplayComponentHideFlag) flag.getKey()).getComponent();
			if (flag.getValue())
				hiddenComponents.add(component);
			else
				hiddenComponents.remove(component);
		}
		
		object.set(DataComponentTypes.TOOLTIP_DISPLAY,
				(TooltipDisplayComponent) MVMisc.newTooltipDisplayComponent(hideTooltip, hiddenComponents));
	}
	
}
