package com.luneruniverse.minecraft.mod.nbteditor.multiversion.mixin.toggled;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;

import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;

@Mixin(BookScreen.Contents.class)
public class BookScreenContentsMixin {
	@Inject(method = "create", at = @At("RETURN"))
	private static void create(ItemStack item, CallbackInfoReturnable<BookScreen.Contents> info) {
		if (item.get(DataComponentTypes.WRITTEN_BOOK_CONTENT) != null)
			MixinLink.WRITTEN_BOOK_CONTENTS.put(info.getReturnValue(), true);
	}
}
