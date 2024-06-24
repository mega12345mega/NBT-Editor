package com.luneruniverse.minecraft.mod.nbteditor.multiversion.mixin.toggled;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.EditableText;

import net.minecraft.text.Text;

@Mixin(Text.Serialization.class)
public class TextSerializerMixin {
	@ModifyVariable(method = "method_10874", at = @At("HEAD"), ordinal = 0, remap = false)
	private Text serialize(Text text) {
		if (text instanceof EditableText editable)
			return editable.getInternalValue();
		return text;
	}
}
