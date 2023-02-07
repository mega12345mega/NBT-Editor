package com.luneruniverse.minecraft.mod.nbteditor.mixin.source;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.util.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

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
	protected BookScreenMixin() {
		super(null);
	}
	
	@Inject(method = "init", at = @At("TAIL"))
	private void init(CallbackInfo info) {
		addDrawableChild(MultiVersionMisc.newButton(16, 64, 100, 20, TextInst.translatable("nbteditor.book.convert"), btn -> {
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
					ref.saveItem(item, () -> {});
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
