package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditor;
import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;

@Mixin(Screen.class)
public class ScreenMixin {
	@Inject(method = "clearChildren", at = @At("RETURN"))
	private void clearChildren(CallbackInfo info) {
		MixinLink.addCreativeTabs((Screen) (Object) this);
	}
	@Inject(method = "init(Lnet/minecraft/client/MinecraftClient;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;init()V"), require = 0)
	private void init(MinecraftClient client, int width, int height, CallbackInfo info) {
		switch (Version.get()) {
			case v1_19_4 -> MixinLink.addCreativeTabs((Screen) (Object) this);
			case v1_19_3, v1_19, v1_18 -> {}
		}
	}
	
	@Inject(method = "filesDragged", at = @At("HEAD"))
	private void filesDragged(List<Path> paths, CallbackInfo info) {
		if (((Object) this) instanceof HandledScreen) {
			for (Path path : paths) {
				File file = path.toFile();
				if (file.isFile() && file.getName().endsWith(".nbt")) {
					try (FileInputStream in = new FileInputStream(file)) {
						ItemStack item = ItemStack.fromNbt(MainUtil.readNBT(in));
						if (!item.isEmpty())
							MainUtil.getWithMessage(item);
					} catch (Exception e) {
						NBTEditor.LOGGER.error("Error while importing a .nbt file", e);
						MainUtil.client.player.sendMessage(TextInst.literal(e.getClass().getName() + ": " + e.getMessage()).formatted(Formatting.RED), false);
					}
				}
			}
		}
	}
	
	@Inject(method = "handleTextClick", at = @At("HEAD"), cancellable = true)
	private void handleTextClick(Style style, CallbackInfoReturnable<Boolean> info) {
		if (style != null && !Screen.hasShiftDown() && style.getClickEvent() != null &&
				style.getClickEvent().getAction() == ClickEvent.Action.OPEN_FILE &&
				MixinLink.tryRunClickEvent(style.getClickEvent().getValue())) {
			info.setReturnValue(true);
		}
	}
}
