package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.EditableText;

import net.minecraft.text.Text;

@Mixin(Text.Serializer.class)
public class TextSerializerMixin {
	@ModifyVariable(method = "serialize", at = @At("HEAD"), ordinal = 0)
	private Text serialize(Text text) {
		if (text instanceof EditableText editable)
			return editable.getInternalValue();
		return text;
	}
}
