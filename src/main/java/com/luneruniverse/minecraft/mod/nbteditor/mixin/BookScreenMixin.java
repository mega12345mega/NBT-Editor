package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.commands.factories.BookCommand;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.ScreenTexts;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.BlockReference;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ContainerItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.screens.OverlaySupportingScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.client.gui.screen.ingame.BookScreen.Contents;
import net.minecraft.client.gui.screen.ingame.LecternScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Formatting;

@Mixin(BookScreen.class)
public class BookScreenMixin extends Screen {
	
	@Shadow
	private Contents contents;
	@Shadow
	private int pageIndex;
	
	private boolean renderLogo;
	private ButtonWidget openBtn;
	private ButtonWidget convertBtn;
	
	protected BookScreenMixin() {
		super(null);
	}
	
	private CompletableFuture<Optional<ItemReference>> getReference() {
		if ((Object) this instanceof LecternScreen) {
			return BlockReference.getLecternBlock().thenApply(optionalRef -> {
				if (optionalRef.isEmpty()) {
					MainUtil.client.player.sendMessage(TextInst.translatable("nbteditor.no_ref.unknown"), false);
					return Optional.empty();
				}
				return optionalRef.map(ref -> new ContainerItemReference<>(ref, 0));
			});
		}
		
		try {
			return CompletableFuture.completedFuture(Optional.of(ItemReference.getHeldItem()));
		} catch (CommandSyntaxException e) {
			MainUtil.client.player.sendMessage(TextInst.literal(e.getMessage()).formatted(Formatting.RED), false);
			return CompletableFuture.completedFuture(Optional.empty());
		}
	}
	private void getReference(Consumer<ItemReference> consumer) {
		getReference().thenAccept(ref -> MainUtil.client.execute(() -> ref.ifPresent(consumer)));
	}
	
	private void updateButtons(Contents contents) {
		boolean editable = (!((Object) this instanceof LecternScreen) || NBTEditorClient.SERVER_CONN.isEditingExpanded()) &&
				NBTEditorClient.SERVER_CONN.isEditingAllowed() && MVMisc.isWrittenBookContents(contents);
		renderLogo = editable;
		openBtn.visible = editable;
		convertBtn.visible = editable;
	}
	
	@Inject(method = "init", at = @At("TAIL"))
	private void init(CallbackInfo info) {
		if (MainUtil.client.currentScreen instanceof
				com.luneruniverse.minecraft.mod.nbteditor.screens.factories.BookScreen) { // Preview mode
			renderLogo = true;
			return;
		}
		
		openBtn = addDrawableChild(MVMisc.newButton(16, 64, 100, 20, TextInst.translatable("nbteditor.book.open"), btn -> {
			getReference(ref -> {
				if ((Object) this instanceof LecternScreen)
					MainUtil.client.player.closeHandledScreen();
				MainUtil.client.setScreen(
						new com.luneruniverse.minecraft.mod.nbteditor.screens.factories.BookScreen(ref, Math.max(0, pageIndex)));
			});
		}));
		convertBtn = addDrawableChild(MVMisc.newButton(16, 64 + 24, 100, 20, TextInst.translatable("nbteditor.book.convert"),
				btn -> getReference(itemRef -> {
					if (BookCommand.convertBookToWritable(itemRef)) {
						openBtn.visible = false;
						convertBtn.visible = false;
						if (!((Object) this instanceof LecternScreen))
							close();
					}
				})));
		
		updateButtons(contents);
	}
	
	@Inject(method = "setPageProvider", at = @At("HEAD"))
	private void setPageProvider(Contents contents, CallbackInfo info) {
		updateButtons(contents);
	}
	
	@Inject(method = "addCloseButton", at = @At("HEAD"), cancellable = true)
	private void addCloseButton(CallbackInfo info) {
		if (MainUtil.client.currentScreen instanceof
				com.luneruniverse.minecraft.mod.nbteditor.screens.factories.BookScreen) { // Preview mode
			info.cancel();
			addDrawableChild(MVMisc.newButton(width / 2 - 100, 196, 200, 20, ScreenTexts.DONE,
					btn -> OverlaySupportingScreen.setOverlayStatic(null)));
		}
	}
	
	@Inject(method = "render", at = @At("TAIL"))
	@Group(name = "render", min = 1)
	private void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo info) {
		if (renderLogo)
			MainUtil.renderLogo(MVDrawableHelper.getMatrices(context));
	}
	@Inject(method = "method_25394(Lnet/minecraft/class_4587;IIF)V", at = @At("TAIL"))
	@Group(name = "render", min = 1)
	@SuppressWarnings("target")
	private void render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo info) {
		if (renderLogo)
			MainUtil.renderLogo(matrices);
	}
	
}
