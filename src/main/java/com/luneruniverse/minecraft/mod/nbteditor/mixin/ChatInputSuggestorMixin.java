package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.SuggestingTextFieldWidget;

import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;

@Mixin(ChatInputSuggestor.class)
public class ChatInputSuggestorMixin {
	@Shadow
	TextFieldWidget textField;
	@ModifyArgs(method = "show", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ChatInputSuggestor$SuggestionWindow;<init>(Lnet/minecraft/client/gui/screen/ChatInputSuggestor;IIILjava/util/List;Z)V"))
	private void SuggestionWindow(Args args) {
		if (textField instanceof SuggestingTextFieldWidget) {
			args.set(1, textField.x + 1);
			args.set(2, textField.y + textField.getHeight() + 2);
		}
	}
}
