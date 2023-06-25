package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.nbtmenugenerators.StringMenuGenerator;
import com.luneruniverse.minecraft.mod.nbteditor.util.NbtFormatter;

import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
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
