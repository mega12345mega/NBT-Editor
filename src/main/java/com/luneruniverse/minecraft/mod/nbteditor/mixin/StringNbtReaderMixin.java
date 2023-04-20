package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.screens.nbtmenugenerators.StringMenuGenerator;

import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.StringNbtReader;

@Mixin(StringNbtReader.class)
public class StringNbtReaderMixin {
	@Inject(method = "parsePrimitive", at = @At("HEAD"), cancellable = true)
	private void parsePrimitive(String input, CallbackInfoReturnable<NbtElement> info) {
		if (StringMenuGenerator.STR_BOOL_REQUESTED.contains(Thread.currentThread())) {
			if ("true".equalsIgnoreCase(input))
				info.setReturnValue(NbtString.of("true"));
			else if ("false".equalsIgnoreCase(input))
				info.setReturnValue(NbtString.of("false"));
		}
	}
}
