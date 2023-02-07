package com.luneruniverse.minecraft.mod.nbteditor.commands.get;

import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.literal;

import java.util.LinkedList;

import com.luneruniverse.minecraft.mod.nbteditor.addons.events.ItemLostCallback;
import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.screens.CursorHistoryScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.item.ItemStack;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.ClickEvent.Action;
import net.minecraft.text.HoverEvent;
import net.minecraft.util.ActionResult;

public class GetLostItemCommand extends ClientCommand {
	
	private static ItemStack LOST_ITEM;
	public static void loseItem(ItemStack item) {
		if (ItemLostCallback.EVENT.invoker().onItemLost(item) == ActionResult.FAIL)
			return;
		LOST_ITEM = item;
		MainUtil.client.player.sendMessage(TextInst.translatable("nbteditor.get.lost_item").append(TextInst.literal("§6/get lostitem")
				.styled(style -> style.withClickEvent(new ClickEvent(Action.RUN_COMMAND, "/get lostitem"))
						.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextInst.translatable("nbteditor.get.lost_item.hover"))))), false);
	}
	
	private static final LinkedList<ItemStack> history = new LinkedList<>();
	public static void addToHistory(ItemStack item) {
		history.addFirst(item.copy());
		if (history.size() > 54)
			history.removeLast();
	}
	
	@Override
	public String getName() {
		return "lostitem";
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
		builder.then(literal("history").executes(context -> {
			CursorHistoryScreen.show(history);
			return Command.SINGLE_SUCCESS;
		})).executes(context -> {
			MainUtil.client.player.sendMessage(MainUtil.parseTranslatableFormatted("nbteditor.get.lost_item.history_hint"), false);
			if (LOST_ITEM == null)
				MainUtil.client.player.sendMessage(TextInst.translatable("nbteditor.get.lost_item.none"), false);
			else
				MainUtil.getWithMessage(LOST_ITEM);
			return Command.SINGLE_SUCCESS;
		});
	}
	
}
