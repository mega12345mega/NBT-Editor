package com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.hideflags;

import java.util.HashMap;
import java.util.Map;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVComponentType;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general.TagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.hideflags.ComponentsHideFlag;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.hideflags.HideFlag;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.hideflags.TooltipHideFlag;

import net.minecraft.component.Component;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Unit;

public class HideFlagsComponentsTagReference implements TagReference<Map<HideFlag, Boolean>, ItemStack> {
	
	@Override
	public Map<HideFlag, Boolean> get(ItemStack object) {
		Map<HideFlag, Boolean> output = new HashMap<>();
		
		output.put(TooltipHideFlag.INSTANCE, object.contains(MVComponentType.HIDE_TOOLTIP_1_20_5_1_21_4));
		
		for (Component<?> component : object.getComponents()) {
			HideFlag flag = ComponentsHideFlag.FLAGS.get(component.type());
			if (flag != null)
				output.put(flag, !((ComponentsHideFlag) flag).getShowInTooltip(object));
		}
		
		return output;
	}
	
	@Override
	public void set(ItemStack object, Map<HideFlag, Boolean> value) {
		for (Map.Entry<HideFlag, Boolean> flag : value.entrySet()) {
			if (flag.getKey() == TooltipHideFlag.INSTANCE) {
				if (flag.getValue())
					object.set(MVComponentType.HIDE_TOOLTIP_1_20_5_1_21_4, Unit.INSTANCE);
				else
					object.remove(MVComponentType.HIDE_TOOLTIP_1_20_5_1_21_4);
				continue;
			}
			
			((ComponentsHideFlag) flag.getKey()).setShowInTooltip(object, !flag.getValue());
		}
	}
	
}
