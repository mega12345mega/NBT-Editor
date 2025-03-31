package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ImportScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;

@Mixin(Screen.class)
public class ScreenMixin {
	@Inject(method = "clearChildren", at = @At("RETURN"))
	private void clearChildren(CallbackInfo info) {
		MixinLink.addCreativeTabs((Screen) (Object) this);
	}
	@Inject(method = "init(Lnet/minecraft/client/MinecraftClient;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;init()V"), require = 0)
	private void init(MinecraftClient client, int width, int height, CallbackInfo info) {
		Version.newSwitch()
				.range("1.19.4", null, () -> MixinLink.addCreativeTabs((Screen) (Object) this))
				.range(null, "1.19.3", () -> {})
				.run();
	}
	
	@Inject(method = "onFilesDropped", at = @At("HEAD"))
	private void onFilesDropped(List<Path> paths, CallbackInfo info) {
		Screen source = (Screen) (Object) this;
		if (source instanceof HandledScreen || source instanceof GameMenuScreen)
			ImportScreen.importFiles(paths, Optional.empty());
	}
	
	@Inject(method = "handleTextClick", at = @At("HEAD"), cancellable = true)
	private void handleTextClick(Style style, CallbackInfoReturnable<Boolean> info) {
		if (style != null && !Screen.hasShiftDown() && style.getClickEvent() != null &&
				style.getClickEvent().getAction() == ClickEvent.Action.OPEN_FILE &&
				MixinLink.tryRunClickEvent(style.getClickEvent().getValue())) {
			info.setReturnValue(true);
		}
	}
	
	// See toggled.ScreenMixin#renderTooltipFromComponents, toggled.DrawContextMixin#drawTooltip
	@Inject(method = "method_32633(Lnet/minecraft/class_4587;Ljava/util/List;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_4587;method_22903()V", shift = At.Shift.AFTER), remap = false, require = 0)
	@SuppressWarnings("target")
	private void renderTooltipFromComponents(MatrixStack matrices, List<TooltipComponent> tooltip, int x, int y, CallbackInfo info) {
		if (!ConfigScreen.isTooltipOverflowFix())
			return;
		
		int[] size = MixinLink.getTooltipSize(tooltip);
		int width = size[0];
		int height = size[1];
		int screenWidth = MainUtil.client.currentScreen.width;
		int screenHeight = MainUtil.client.currentScreen.height;
		
		x += 12;
		y -= 12;
		if (x + width > screenWidth)
			x -= 28 + width;
		if (y + height + 6 > screenHeight)
			y = screenHeight - height - 6;
		
		MixinLink.renderTooltipFromComponents(matrices, x, y, width, height, screenWidth, screenHeight);
	}
}
