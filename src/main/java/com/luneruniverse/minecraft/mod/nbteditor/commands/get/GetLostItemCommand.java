package com.luneruniverse.minecraft.mod.nbteditor.commands.get;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.item.ItemStack;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.ClickEvent.Action;
import net.minecraft.text.HoverEvent;

public class GetLostItemCommand extends ClientCommand {
	
	private static ItemStack LOST_ITEM;
	public static void loseItem(ItemStack item) {
		LOST_ITEM = item;
		MainUtil.client.player.sendMessage(TextInst.translatable("nbteditor.get.lost_item").append(TextInst.literal("§6/get lostitem")
				.styled(style -> style.withClickEvent(new ClickEvent(Action.RUN_COMMAND, "/get lostitem"))
						.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextInst.translatable("nbteditor.get.lost_item.hover"))))), false);
	}
	
	@Override
	public String getName() {
		return "lostitem";
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
		builder.executes(context -> {
			if (LOST_ITEM == null)
				MainUtil.client.player.sendMessage(TextInst.translatable("nbteditor.get.lost_item.none"), false);
			else
				MainUtil.getWithMessage(LOST_ITEM);
			return Command.SINGLE_SUCCESS;
		});
	}
	
}
