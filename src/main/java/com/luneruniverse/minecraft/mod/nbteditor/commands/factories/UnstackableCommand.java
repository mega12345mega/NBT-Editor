package com.luneruniverse.minecraft.mod.nbteditor.commands.factories;

import java.util.UUID;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.item.ItemStack;
import net.minecraft.text.HoverEvent;

public class UnstackableCommand extends ClientCommand {
	
	@Override
	public String getName() {
		return "unstackable";
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
		builder.executes(context -> {
			ItemReference ref = MainUtil.getHeldItem();
			ItemStack item = ref.getItem();
			item.getOrCreateNbt().putUuid("UUID", UUID.randomUUID());
			ref.saveItem(item, TextInst.translatable("nbteditor.unstackable.msg").styled(style ->
					style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextInst.translatable("nbteditor.unstackable.hover_msg")))));
			return Command.SINGLE_SUCCESS;
		});
	}
	
}
