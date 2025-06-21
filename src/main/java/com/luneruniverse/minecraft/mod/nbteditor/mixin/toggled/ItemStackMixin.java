package com.luneruniverse.minecraft.mod.nbteditor.mixin.toggled;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;

import net.minecraft.component.ComponentChanges;
import net.minecraft.component.MergedComponentMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

@Mixin(ItemStack.class)
public class ItemStackMixin {
	
	@Inject(method = "getTooltip", at = @At("RETURN"))
	private void getTooltip(Item.TooltipContext context, PlayerEntity player, TooltipType type, CallbackInfoReturnable<List<Text>> info) {
		MixinLink.modifyTooltip((ItemStack) (Object) this, info.getReturnValue());
	}
	
	@Shadow
	private @Final MergedComponentMap components;
	
	@Inject(method = "applyChanges", at = @At(value = "INVOKE", target = "Lnet/minecraft/component/MergedComponentMap;applyChanges(Lnet/minecraft/component/ComponentChanges;)V"))
	private void applyChanges(ComponentChanges changes, CallbackInfo info) {
		if (MixinLink.SET_CHANGES.contains(Thread.currentThread())) {
			MixinLink.SET_CHANGES.remove(Thread.currentThread());
			components.setChanges(ComponentChanges.EMPTY);
		}
	}
	
}
