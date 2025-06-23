package com.luneruniverse.minecraft.mod.nbteditor.fancytext;

import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.IdentifierInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.util.InvalidIdentifierException;

public record FancyTextStyleOptionNode(StyleOption option, String value, List<FancyTextNode> contents) implements FancyTextNode {
	
	@Override
	public Style modifyStyle(Style style) {
		return switch (option) {
			case OPEN_URL, RUN_COMMAND, SUGGEST_COMMAND, CHANGE_PAGE, COPY_TO_CLIPBOARD -> style.withClickEvent(
					new ClickEvent(ClickEvent.Action.valueOf(option.name()), value == null ? "" : value));
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
			case INSERTION -> style.withInsertion(value);
			case FONT -> {
				try {
					yield style.withFont(IdentifierInst.of(value));
				} catch (InvalidIdentifierException e) {
					yield style.withFont(Style.DEFAULT_FONT_ID);
				}
			}
		};
	}
	
	@Override
	public int getNumberOfTextNodes() {
		return contents.stream().mapToInt(FancyTextNode::getNumberOfTextNodes).sum();
	}
	
}
