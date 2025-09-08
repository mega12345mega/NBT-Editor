package com.luneruniverse.minecraft.mod.nbteditor.mixin.toggled;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.NbtFormatter;

import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.StringNbtReader;

@Mixin(StringNbtReader.class)
public class StringNbtReaderMixin {
	@Inject(method = "method_10731(Ljava/lang/String;)Lnet/minecraft/class_2520;", at = @At("HEAD"), cancellable = true, remap = false)
	@SuppressWarnings("target")
	private void parsePrimitive(String input, CallbackInfoReturnable<NbtElement> info) {
		if (ConfigScreen.isSpecialNumbers() && MixinLink.specialNumbers.contains(Thread.currentThread())) {
			Number specialNum = NbtFormatter.SPECIAL_NUMS.get(input);
			if (specialNum != null) {
				if (specialNum instanceof Double d)
					info.setReturnValue(NbtDouble.of(d));
				else if (specialNum instanceof Float f)
					info.setReturnValue(NbtFloat.of(f));
				else
					throw new IllegalStateException("Number of invalid type: " + specialNum.getClass().getName());
			}
		}
	}
}
