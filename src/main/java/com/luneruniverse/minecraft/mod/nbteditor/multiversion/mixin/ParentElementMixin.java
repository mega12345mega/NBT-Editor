package com.luneruniverse.minecraft.mod.nbteditor.multiversion.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.OldEventBehavior;

import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;

@Mixin(ParentElement.class)
public interface ParentElementMixin {
	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	private void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> info) {
		if (!(this instanceof OldEventBehavior))
			return;
		
		ParentElement source = (ParentElement) (Object) this;
		
		for (Element element : source.children()) {
			if (element.mouseClicked(mouseX, mouseY, button)) {
				source.setFocused(element);
				if (button == 0)
					source.setDragging(true);
				info.setReturnValue(true);
				return;
			}
		}
		info.setReturnValue(false);
	}
}
