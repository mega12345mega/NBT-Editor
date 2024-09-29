package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.text.OrderedText;

@Mixin(Tooltip.class)
public class TooltipMixin {
	
	@Shadow
	private List<OrderedText> lines;
	
	@Inject(method = "getLines", at = @At("HEAD"), cancellable = true)
	private void getLines(MinecraftClient client, CallbackInfoReturnable<List<OrderedText>> info) {
		if (MixinLink.NEW_TOOLTIPS.containsKey((Tooltip) (Object) this))
			info.setReturnValue(lines);
	}
	
}
