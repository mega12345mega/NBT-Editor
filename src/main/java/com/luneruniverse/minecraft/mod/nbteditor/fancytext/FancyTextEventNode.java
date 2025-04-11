package com.luneruniverse.minecraft.mod.nbteditor.fancytext;

import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;

public record FancyTextEventNode(TextAction event, String value, List<FancyTextNode> contents) implements FancyTextNode {
	
	@Override
	public Style modifyStyle(Style style) {
		if (event instanceof ClickAction clickAction)
			return style.withClickEvent(clickAction.toEvent(value == null ? "" : value));
		
		return switch ((HoverAction) event) {
			case SHOW_TEXT -> style.withHoverEvent(
					new HoverEvent(HoverEvent.Action.SHOW_TEXT, value == null ? TextInst.of("") : FancyText.parse(value)));
			case SHOW_ITEM -> {
				ItemStack item;
				try {
					item = MainUtil.client.player.getInventory().getStack(Integer.parseInt(value));
				} catch (NumberFormatException e) {
					try {
						item = ItemReference.getHeldItem().getItem();
					} catch (CommandSyntaxException e2) {
						item = ItemStack.EMPTY;
					}
				}
				yield style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackContent(item)));
			}
			case SHOW_ENTITY -> {
				Entity entity;
				try {
					if (value == null)
						throw new IllegalArgumentException();
					String uuid = value;
					if (!uuid.contains("-"))
						uuid = uuid.replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5");
					UUID uuidObj = UUID.fromString(uuid);
					entity = StreamSupport.stream(MainUtil.client.world.getEntities().spliterator(), false)
							.filter(testEntity -> testEntity.getUuid().equals(uuidObj)).findFirst()
							.orElseThrow(IllegalArgumentException::new);
				} catch (IllegalArgumentException e) {
					if (MainUtil.client.targetedEntity != null)
						entity = MainUtil.client.targetedEntity;
					else
						entity = MainUtil.client.player;
				}
				yield style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ENTITY,
						new HoverEvent.EntityContent(entity.getType(), entity.getUuid(), entity.getName())));
			}
			default -> style; // Impossible
		};
	}
	
	@Override
	public int getNumberOfTextNodes() {
		return contents.stream().mapToInt(FancyTextNode::getNumberOfTextNodes).sum();
	}
	
}
