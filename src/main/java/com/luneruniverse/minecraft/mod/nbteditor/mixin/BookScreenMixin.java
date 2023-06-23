package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.luneruniverse.minecraft.mod.nbteditor.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.ScreenTexts;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.screens.OverlaySupportingScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Mixin(BookScreen.class)
public class BookScreenMixin extends Screen {
	@Shadow
	private int pageIndex;
	
	protected BookScreenMixin() {
		super(null);
	}
	
	@Inject(method = "init", at = @At("TAIL"))
	private void init(CallbackInfo info) {
		if (MainUtil.client.currentScreen instanceof com.luneruniverse.minecraft.mod.nbteditor.screens.factories.BookScreen) // Preview mode
			return;
		
		addDrawableChild(MultiVersionMisc.newButton(16, 64, 100, 20, TextInst.translatable("nbteditor.book.open"), btn -> {
			try {
				MainUtil.client.setScreen(new com.luneruniverse.minecraft.mod.nbteditor.screens.factories.BookScreen(MainUtil.getHeldItem(), pageIndex));
			} catch (CommandSyntaxException e) {
				MainUtil.client.player.sendMessage(TextInst.literal(e.getMessage()).formatted(Formatting.RED), false);
			}
		}));
		addDrawableChild(MultiVersionMisc.newButton(16, 64 + 24, 100, 20, TextInst.translatable("nbteditor.book.convert"), btn -> {
			try {
				ItemReference ref = MainUtil.getHeldItem();
				ItemStack item = MainUtil.setType(Items.WRITABLE_BOOK, ref.getItem(), 1);
				boolean formatted = false;
				if (item.hasNbt() && item.getNbt().contains("pages", NbtElement.LIST_TYPE)) {
					NbtList convertedPages = new NbtList();
					for (NbtElement page : item.getNbt().getList("pages", NbtElement.STRING_TYPE)) {
						Text text = TextArgumentType.text().parse(new StringReader(((NbtString) page).value));
						if (!formatted && MainUtil.isTextFormatted(text, true))
							formatted = true;
						convertedPages.add(NbtString.of(text.getString()));
					}
					item.getNbt().put("pages", convertedPages);
				}
				if (formatted) {
					MainUtil.client.player.sendMessage(TextInst.translatable("nbteditor.book.convert.formatting_saved"), false);
					MainUtil.get(item, true);
				} else
					ref.saveItem(item);
			} catch (CommandSyntaxException e) {
				MainUtil.client.player.sendMessage(TextInst.literal(e.getMessage()).formatted(Formatting.RED), false);
			}
		}));
	}
	
	@Inject(method = "addCloseButton", at = @At("HEAD"), cancellable = true)
	private void addCloseButton(CallbackInfo info) {
		if (MainUtil.client.currentScreen instanceof com.luneruniverse.minecraft.mod.nbteditor.screens.factories.BookScreen) { // Preview mode
			info.cancel();
			addDrawableChild(MultiVersionMisc.newButton(width / 2 - 100, 196, 200, 20, ScreenTexts.DONE,
					btn -> OverlaySupportingScreen.setOverlayStatic(null)));
		}
	}
	
	@Inject(method = "render", at = @At("TAIL"))
	@Group(name = "render", min = 1)
	private void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo info) {
		MainUtil.renderLogo(MultiVersionDrawableHelper.getMatrices(context));
	}
	@Inject(method = "method_25394(Lnet/minecraft/class_4587;IIF)V", at = @At("TAIL"))
	@Group(name = "render", min = 1)
	@SuppressWarnings("target")
	private void render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo info) {
		MainUtil.renderLogo(matrices);
	}
}
