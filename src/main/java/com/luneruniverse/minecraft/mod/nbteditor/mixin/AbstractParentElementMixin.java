package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVElement;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.OldEventBehavior;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.TextFieldWidget;

@Mixin(AbstractParentElement.class)
public class AbstractParentElementMixin {
	@Inject(method = "setFocused", at = @At("RETURN"))
	private void setFocused(Element element, CallbackInfo info) {
		boolean oldEvents = MainUtil.client.currentScreen instanceof OldEventBehavior;
		for (Element child : ((AbstractParentElement) (Object) this).children()) {
			if (child instanceof MVElement multiChild)
				multiChild.onFocusChange(child == element);
			if (oldEvents && child instanceof TextFieldWidget textChild)
				textChild.setFocused(child == element);
		}
	}
}
