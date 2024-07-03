package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;

import net.minecraft.item.WrittenBookItem;
import net.minecraft.nbt.NbtCompound;

@Mixin(WrittenBookItem.class)
public class WrittenBookItemMixin {
	@Inject(method = "isValid", at = @At("HEAD"), cancellable = true, require = 0)
	private static void isValid(NbtCompound nbt, CallbackInfoReturnable<Boolean> info) {
		if (MixinLink.ACTUAL_BOOK_CONTENTS.contains(Thread.currentThread()))
			info.setReturnValue(true);
	}
}
