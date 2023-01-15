package com.luneruniverse.minecraft.mod.nbteditor.mixin.source;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.util.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Items;
import net.minecraft.util.Formatting;

// TODO unused 1.19.3 update
@Mixin(BookScreen.class)
public class BookScreenMixin extends Screen {
	protected BookScreenMixin() {
		super(null);
	}
	
	@Shadow
	private BookScreen.Contents contents;
	
	@Inject(method = "init", at = @At("TAIL"))
	private void init(CallbackInfo info) {
		addDrawableChild(MultiVersionMisc.newButton(16, 64, 100, 20, TextInst.translatable("nbteditor.book.edit"), btn -> {
			System.out.println("hi");
		}));
		addDrawableChild(MultiVersionMisc.newButton(16, 88, 100, 20, TextInst.translatable("nbteditor.book.convert"), btn -> {
			try {
				ItemReference ref = MainUtil.getHeldItem();
				ref.saveItem(MainUtil.setType(Items.WRITABLE_BOOK, ref.getItem()), () -> {});
			} catch (CommandSyntaxException e) {
				MainUtil.client.player.sendMessage(TextInst.literal(e.getMessage()).formatted(Formatting.RED), false);
			}
		}));
	}
	
	@Inject(method = "render", at = @At("TAIL"))
	private void render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo info) {
		MainUtil.renderLogo(matrices);
	}
}
