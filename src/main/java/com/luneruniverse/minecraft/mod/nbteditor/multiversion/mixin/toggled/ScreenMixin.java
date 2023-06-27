package com.luneruniverse.minecraft.mod.nbteditor.multiversion.mixin.toggled;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVTooltip;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.text.OrderedText;

@Mixin(Screen.class)
public class ScreenMixin {
	@Inject(method = "setTooltip(Ljava/util/List;Lnet/minecraft/client/gui/tooltip/TooltipPositioner;Z)V", at = @At("HEAD"), cancellable = true)
	private void setTooltip(List<OrderedText> tooltip, TooltipPositioner positioner, boolean focused, CallbackInfo info) {
		if (MVTooltip.setExternalOneTooltip(tooltip))
			info.cancel();
	}
}
