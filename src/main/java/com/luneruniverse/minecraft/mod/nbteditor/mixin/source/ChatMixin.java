package com.luneruniverse.minecraft.mod.nbteditor.mixin.source;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;

@Mixin(ChatScreen.class)
public class ChatMixin {
	@ModifyArg(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/TextFieldWidget;setMaxLength(I)V"), index = 0)
	private int setMaxLength(int length) {
		if (ConfigScreen.isChatLimitExtended())
			return Integer.MAX_VALUE;
		return length;
	}
	@Inject(method = "render", at = @At("HEAD"))
	private void render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo info) {
		if (!ConfigScreen.isChatLimitExtended())
			return;
		
		ChatScreen source = (ChatScreen) (Object) this;
		TextFieldWidget chatField = ((ChatScreenAccessor) source).getChatField();
		if (chatField.getText().length() > 256) {
			DrawableHelper.fill(matrices, source.width - 202, source.height - 40, source.width - 2, source.height - 14, 0xAAFFAA00);
			TextRenderer textRenderer = MainUtil.client.textRenderer;
			DrawableHelper.drawCenteredTextWithShadow(matrices, textRenderer, TextInst.translatable("nbteditor.chat_length_warning_1").asOrderedText(), source.width - 102, source.height - 40 + textRenderer.fontHeight / 2, 0xFFAA5500);
			DrawableHelper.drawCenteredTextWithShadow(matrices, textRenderer, TextInst.translatable("nbteditor.chat_length_warning_2").asOrderedText(), source.width - 102, source.height - 28 + textRenderer.fontHeight / 2, 0xFFAA5500);
		}
	}
	
	@Inject(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V"), cancellable = true)
	private void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> info) {
		if (!(MainUtil.client.currentScreen instanceof ChatScreen)) {
			info.setReturnValue(true);
			info.cancel();
		}
	}
}
