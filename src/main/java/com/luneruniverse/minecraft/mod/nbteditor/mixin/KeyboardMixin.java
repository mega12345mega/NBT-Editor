package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.FormattedTextFieldWidget;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.Keyboard;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.util.NarratorManager;

@Mixin(Keyboard.class)
public class KeyboardMixin {
	@Redirect(method = "onKey", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/NarratorManager;isActive()Z"))
	private boolean isActive(NarratorManager manager) {
		if (MainUtil.client.currentScreen != null) {
			Element focused = MainUtil.client.currentScreen.getFocused();
			while (focused != null) {
				if (focused instanceof FormattedTextFieldWidget)
					return false;
				else if (focused instanceof ParentElement parent)
					focused = parent.getFocused();
				else
					break;
			}
		}
		return manager.isActive();
	}
}
