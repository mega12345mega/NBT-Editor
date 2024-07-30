package com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.mixin.client;

import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandInternals;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.StringHelper;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
	@Shadow
	protected TextFieldWidget chatField;
	
	@Inject(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ChatScreen;sendMessage(Ljava/lang/String;Z)V"), cancellable = true)
	@Group(name = "keyPressed", min = 1)
	private void enterPressed_new(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> info) {
		enterPressed_impl(info);
	}
	@Inject(method = "method_25404(III)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_408;method_25427(Ljava/lang/String;)V"), cancellable = true, remap = false)
	@Group(name = "keyPressed", min = 1)
	@SuppressWarnings("target")
	private void enterPressed_old(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> info) {
		enterPressed_impl(info);
	}
	private void enterPressed_impl(CallbackInfoReturnable<Boolean> info) {
		String text = StringUtils.normalizeSpace(chatField.getText().trim());
		if (text.isEmpty() || text.length() <= 256)
			return;
		if (text.charAt(0) == '/' && ClientCommandInternals.executeCommand(text.substring(1))) {
			MainUtil.client.inGameHud.getChatHud().addToMessageHistory(text);
			if (MainUtil.client.currentScreen instanceof ChatScreen)
				MainUtil.client.setScreen(null);
			info.setReturnValue(true);
		} else
			chatField.text = StringHelper.truncateChat(text);
	}
}
