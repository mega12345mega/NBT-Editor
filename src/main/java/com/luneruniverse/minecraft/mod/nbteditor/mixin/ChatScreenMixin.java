package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.math.MatrixStack;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
	@ModifyArg(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/TextFieldWidget;setMaxLength(I)V"), index = 0)
	private int setMaxLength(int length) {
		if (ConfigScreen.isChatLimitExtended())
			return Integer.MAX_VALUE;
		return length;
	}
	@Inject(method = "render", at = @At("HEAD"))
	@Group(name = "render", min = 1)
	private void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo info) {
		MixinLink.renderChatLimitWarning((ChatScreen) (Object) this, MVDrawableHelper.getMatrices(context));
	}
	@Inject(method = "method_25394(Lnet/minecraft/class_4587;IIF)V", at = @At("HEAD"))
	@Group(name = "render", min = 1)
	@SuppressWarnings("target")
	private void render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo info) {
		MixinLink.renderChatLimitWarning((ChatScreen) (Object) this, matrices);
	}
	
	@Inject(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V"), cancellable = true)
	private void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> info) {
		if (!(MainUtil.client.currentScreen instanceof ChatScreen)) {
			info.setReturnValue(true);
			info.cancel();
		}
	}
	
	@Inject(method = "normalize", at = @At(value = "HEAD"), cancellable = true, require = 0)
	private void normalize(String msg, CallbackInfoReturnable<String> info) {
		if (ConfigScreen.isChatLimitExtended())
			info.setReturnValue(StringUtils.normalizeSpace(msg));
	}
}
