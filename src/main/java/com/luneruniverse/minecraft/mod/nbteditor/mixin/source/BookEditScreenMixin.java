package com.luneruniverse.minecraft.mod.nbteditor.mixin.source;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.BookEditScreen;
import net.minecraft.client.util.math.MatrixStack;

// TODO unused 1.19.3 update
@Mixin(BookEditScreen.class)
public class BookEditScreenMixin extends Screen {
	protected BookEditScreenMixin() {
		super(null);
	}
	
	@Inject(method = "init", at = @At("TAIL"))
	private void init(CallbackInfo info) {
		addDrawableChild(MultiVersionMisc.newButton(16, 64, 100, 20, TextInst.translatable("nbteditor.book.edit"), btn -> {
			System.out.println("hi");
		}));
	}
	
	@Inject(method = "render", at = @At("TAIL"))
	private void render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo info) {
		MainUtil.renderLogo(matrices);
	}
}
