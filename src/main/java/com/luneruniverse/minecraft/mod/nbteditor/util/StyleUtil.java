package com.luneruniverse.minecraft.mod.nbteditor.util;

import java.util.Objects;

import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalBlock;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalEntity;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalItem;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalNBT;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;

import net.minecraft.text.Style;
import net.minecraft.util.Formatting;

public class StyleUtil {
	
	public static final boolean SHADOW_COLOR_EXISTS = Version.<Boolean>newSwitch()
			.range("1.21.4", null, true)
			.range(null, "1.21.3", false)
			.get();
	
	public static final Style RESET_STYLE = Style.EMPTY.withColor(Formatting.WHITE)
			.withBold(false).withItalic(false).withUnderline(false).withStrikethrough(false).withObfuscated(false);
	
	public static Style getBaseNameStyle(LocalNBT localNBT, boolean itemName) {
		Style baseNameStyle = Style.EMPTY;
		if (localNBT instanceof LocalItem item) {
			if (!itemName)
				baseNameStyle = baseNameStyle.withFormatting(Formatting.ITALIC);
			baseNameStyle = baseNameStyle.withFormatting(item.getEditableItem().getRarity().formatting);
		} else if (localNBT instanceof LocalBlock)
			;
		else if (localNBT instanceof LocalEntity)
			baseNameStyle = baseNameStyle.withFormatting(Formatting.WHITE);
		else
			throw new IllegalStateException("Cannot get base name style for " + localNBT.getClass().getName());
		
		return baseNameStyle;
	}
	
	public static final Style BASE_LORE_STYLE = Style.EMPTY.withFormatting(Formatting.ITALIC, Formatting.DARK_PURPLE);
	
	public static final Style BOOK_STYLE = Style.EMPTY.withFormatting(Formatting.BLACK);
	
	public static boolean identical(Style a, Style b) {
		boolean output = Objects.equals(a.getColor(), b.getColor()) &&
				a.bold == b.bold &&
				a.italic == b.italic &&
				a.underlined == b.underlined &&
				a.strikethrough == b.strikethrough &&
				a.obfuscated == b.obfuscated &&
				Objects.equals(a.getClickEvent(), b.getClickEvent()) &&
				Objects.equals(a.getHoverEvent(), b.getHoverEvent()) &&
				Objects.equals(a.getInsertion(), b.getInsertion()) &&
				Objects.equals(a.font, b.font);
		
		if (SHADOW_COLOR_EXISTS)
			output &= Objects.equals(a.getShadowColor(), b.getShadowColor());
		
		return output;
	}
	
	public static boolean hasFormatting(Style style, Formatting formatting) {
		return identical(style, style.withFormatting(formatting));
	}
	
	public static boolean hasFormatting(Style style, Style base) {
		return !identical(style.withParent(base), base);
	}
	
	public static Style minus(Style style, Style base) {
		Style output = Style.EMPTY;
		
		if (style.getColor() != null && !style.getColor().equals(base.getColor()))
			output = output.withColor(style.getColor());
		if (style.bold != null && !style.bold.equals(base.bold))
			output = output.withBold(style.bold);
		if (style.italic != null && !style.italic.equals(base.italic))
			output = output.withItalic(style.italic);
		if (style.underlined != null && !style.underlined.equals(base.underlined))
			output = output.withUnderline(style.underlined);
		if (style.strikethrough != null && !style.strikethrough.equals(base.strikethrough))
			output = output.withStrikethrough(style.strikethrough);
		if (style.obfuscated != null && !style.obfuscated.equals(base.obfuscated))
			output = output.withObfuscated(style.obfuscated);
		if (style.bold != null && !style.bold.equals(base.bold))
			output = output.withBold(style.bold);
		if (style.getClickEvent() != null && !style.getClickEvent().equals(base.getClickEvent()))
			output = output.withClickEvent(style.getClickEvent());
		if (style.getHoverEvent() != null && !style.getHoverEvent().equals(base.getHoverEvent()))
			output = output.withHoverEvent(style.getHoverEvent());
		if (style.getInsertion() != null && !style.getInsertion().equals(base.getInsertion()))
			output = output.withInsertion(style.getInsertion());
		if (style.font != null && !style.font.equals(base.font))
			output = output.withFont(style.font);
		
		if (SHADOW_COLOR_EXISTS && style.getShadowColor() != null && !style.getShadowColor().equals(base.getShadowColor()))
			output = output.withShadowColor(style.getShadowColor());
		
		return output;
	}
	
	public static Style minusFormatting(Style style, Style base, Formatting formatting) {
		if (formatting == Formatting.RESET)
			return base;
		if (formatting.isColor())
			return style.withColor(base.getColor());
		return switch (formatting) {
			case BOLD -> style.withBold(base.bold);
			case ITALIC -> style.withItalic(base.italic);
			case UNDERLINE -> style.withUnderline(base.underlined);
			case STRIKETHROUGH -> style.withStrikethrough(base.strikethrough);
			case OBFUSCATED -> style.withObfuscated(base.obfuscated);
			default -> throw new IllegalArgumentException("Unknown formatting: " + formatting);
		};
	}
	
}
