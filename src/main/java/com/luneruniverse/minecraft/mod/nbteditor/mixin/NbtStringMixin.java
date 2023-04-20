package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.misc.NbtTypeModifier;

import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.NbtType;

@Mixin(NbtString.class)
public class NbtStringMixin {
	
	@Inject(at = @At(value = "HEAD"), method = "copy", cancellable = true)
    private void copy(CallbackInfoReturnable<NbtString> info) {
        info.setReturnValue(NbtString.of(
        		((NbtString) (Object) this).asString()
        	));
        info.cancel();
    }
	@Inject(at = @At(value = "HEAD"), method = "getNbtType", cancellable = true)
    private void getNbtType(CallbackInfoReturnable<NbtType<NbtString>> info) {
        info.setReturnValue(NbtTypeModifier.NBT_STRING_TYPE);
        info.cancel();
    }
	
}
