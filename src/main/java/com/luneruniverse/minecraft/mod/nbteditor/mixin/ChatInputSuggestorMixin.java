package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import java.awt.Point;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.SuggestingTextFieldWidget;

import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.Formatting;

@Mixin(ChatInputSuggestor.class)
public class ChatInputSuggestorMixin {
	@Shadow
	TextFieldWidget textField;
	
	@ModifyArgs(method = "show", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ChatInputSuggestor$SuggestionWindow;<init>(Lnet/minecraft/client/gui/screen/ChatInputSuggestor;IIILjava/util/List;Z)V"))
	private void SuggestionWindow(Args args) {
		if (!(textField instanceof SuggestingTextFieldWidget suggestor))
			return;
		
		if (suggestor.isDropdownOnly()) {
			Point pos = suggestor.getSpecialDropdownPos();
			args.set(1, pos.x);
			args.set(2, pos.y);
		} else
			args.set(2, textField.y + textField.getHeight() + 2);
	}
	
	@Inject(method = "showUsages", at = @At("HEAD"), cancellable = true)
	@Group(name = "showUsages", min = 1)
	private void showUsages(Formatting formatting, CallbackInfoReturnable<Boolean> info) {
		if (!(textField instanceof SuggestingTextFieldWidget))
			return;
		
		info.setReturnValue(true);
	}
	@Inject(method = "method_23929(Lnet/minecraft/class_124;)V", at = @At("HEAD"), cancellable = true)
	@Group(name = "showUsages", min = 1)
	@SuppressWarnings("target")
	private void showUsages(Formatting formatting, CallbackInfo info) {
		if (!(textField instanceof SuggestingTextFieldWidget))
			return;
		
		info.cancel();
	}
}
