package com.luneruniverse.minecraft.mod.nbteditor.mixin.toggled;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.NbtFormatter;
import com.mojang.serialization.DynamicOps;

import net.minecraft.nbt.SnbtParsing;

@Mixin(SnbtParsing.class)
public class SnbtParsingMixin {
	@Redirect(method = "method_68722", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/DynamicOps;createString(Ljava/lang/String;)Ljava/lang/Object;", remap = false))
	private static Object createParser$method_68722_createString(DynamicOps<?> ops, String str) {
		if (ConfigScreen.isSpecialNumbers() && MixinLink.specialNumbers.contains(Thread.currentThread())) {
			Number specialNum = NbtFormatter.SPECIAL_NUMS.get(str);
			if (specialNum != null) {
				if (specialNum instanceof Double d)
					return ops.createDouble(d);
				else if (specialNum instanceof Float f)
					return ops.createFloat(f);
				else
					throw new IllegalStateException("Number of invalid type: " + specialNum.getClass().getName());
			}
		}
		return ops.createString(str);
	}
}
