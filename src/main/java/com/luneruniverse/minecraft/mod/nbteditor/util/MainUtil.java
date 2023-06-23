package com.luneruniverse.minecraft.mod.nbteditor.util;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.zip.ZipException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.luneruniverse.minecraft.mod.nbteditor.NBTEditor;
import com.luneruniverse.minecraft.mod.nbteditor.async.UpdateCheckerThread;
import com.luneruniverse.minecraft.mod.nbteditor.commands.arguments.FancyTextArgumentType;
import com.luneruniverse.minecraft.mod.nbteditor.itemreferences.HandItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.EditableText;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionRegistry;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.screens.util.FancyConfirmScreen;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.ClickEvent.Action;
import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable.StyledVisitor;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

public class MainUtil {
	
	public static final MinecraftClient client = MinecraftClient.getInstance();
	
	public static ItemReference getHeldItem(Predicate<ItemStack> isAllowed, Text failText) throws CommandSyntaxException {
		ItemStack item = client.player.getMainHandStack();
		Hand hand = Hand.MAIN_HAND;
		if (item == null || item.isEmpty() || !isAllowed.test(item)) {
			item = client.player.getOffHandStack();
			hand = Hand.OFF_HAND;
		}
		if (item == null || item.isEmpty() || !isAllowed.test(item))
			throw new SimpleCommandExceptionType(failText).create();
		
		return new HandItemReference(hand);
	}
	public static ItemReference getHeldItem() throws CommandSyntaxException {
		return getHeldItem(item -> true, TextInst.translatable("nbteditor.no_hand.no_item.to_edit"));
	}
	public static ItemReference getHeldItemAirable() {
		try {
			return getHeldItem();
		} catch (CommandSyntaxException e) {
			return new HandItemReference(Hand.MAIN_HAND);
		}
	}
	public static ItemReference getHeldAir() throws CommandSyntaxException {
		if (MainUtil.client.player.getMainHandStack().isEmpty())
			return new HandItemReference(Hand.MAIN_HAND);
		if (MainUtil.client.player.getOffHandStack().isEmpty())
			return new HandItemReference(Hand.OFF_HAND);
		throw new SimpleCommandExceptionType(TextInst.translatable("nbteditor.no_hand.all_item")).create();
	}
	
	public static void saveItem(Hand hand, ItemStack item) {
		client.player.setStackInHand(hand, item.copy());
		if (client.interactionManager.getCurrentGameMode().isCreative())
			client.getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(hand == Hand.OFF_HAND ? 45 : client.player.getInventory().selectedSlot + 36, item));
	}
	public static void saveItem(EquipmentSlot equipment, ItemStack item) {
		if (equipment == EquipmentSlot.MAINHAND)
			saveItem(Hand.MAIN_HAND, item);
		else if (equipment == EquipmentSlot.OFFHAND)
			saveItem(Hand.OFF_HAND, item);
		else {
			client.player.getInventory().armor.set(equipment.getEntitySlotId(), item.copy());
			client.interactionManager.clickCreativeStack(item, 8 - equipment.getEntitySlotId());
		}
	}
	
	public static void saveItem(int slot, ItemStack item) {
		client.player.getInventory().setStack(slot == 45 ? 40 : slot, item.copy());
		client.interactionManager.clickCreativeStack(item, slot < 9 ? slot + 36 : slot);
	}
	public static void saveItemInvSlot(int slot, ItemStack item) {
		saveItem(slot == 45 ? 45 : (slot >= 36 ? slot - 36 : slot), item);
	}
	
	public static void get(ItemStack item, boolean dropIfNoSpace) {
		PlayerInventory inv = client.player.getInventory();
		item = item.copy();
		
		int slot = inv.getOccupiedSlotWithRoomForStack(item);
		if (slot == -1)
			slot = inv.getEmptySlot();
		if (slot == -1) {
			if (dropIfNoSpace) {
				if (item.getCount() > item.getMaxCount())
					item.setCount(item.getMaxCount());
				client.interactionManager.dropCreativeStack(item);
			}
		} else {
			item.setCount(item.getCount() + inv.getStack(slot).getCount());
			int overflow = 0;
			if (item.getCount() > item.getMaxCount()) {
				overflow = item.getCount() - item.getMaxCount();
				item.setCount(item.getMaxCount());
			}
			saveItem(slot, item);
			if (overflow != 0) {
				item.setCount(overflow);
				get(item, false);
			}
		}
	}
	public static void getWithMessage(ItemStack item) {
		get(item, true);
		client.player.sendMessage(TextInst.translatable("nbteditor.get.item").append(item.toHoverableText()), false);
	}
	
	
	
	private static final Identifier LOGO = new Identifier("nbteditor", "textures/logo.png");
	private static final Identifier LOGO_UPDATE_AVAILABLE = new Identifier("nbteditor", "textures/logo_update_available.png");
	public static void renderLogo(MatrixStack matrices) {
		MultiVersionDrawableHelper.drawTexture(matrices,
				UpdateCheckerThread.UPDATE_AVAILABLE ? LOGO_UPDATE_AVAILABLE : LOGO, 16, 16, 0, 0, 32, 32, 32, 32);
	}
	
	
	
	public static void drawWrappingString(MatrixStack matrices, TextRenderer renderer, String text, int x, int y, int maxWidth, int color, boolean centerHorizontal, boolean centerVertical) {
		maxWidth = Math.max(maxWidth, renderer.getWidth("ww"));
		
		// Split into breaking spots
		List<String> parts = new ArrayList<>();
		List<Integer> spaces = new ArrayList<>();
		StringBuilder currentPart = new StringBuilder();
		boolean wasUpperCase = false;
		for (char c : text.toCharArray()) {
			if (c == ' ') {
				wasUpperCase = false;
				parts.add(currentPart.toString());
				currentPart.setLength(0);
				spaces.add(parts.size());
				continue;
			}
			
			boolean upperCase = Character.isUpperCase(c);
			if (upperCase != wasUpperCase && !currentPart.isEmpty()) { // Handle NBTEditor; output NBT, Editor; not N, B, T, Editor AND Handle MinionYT; output Minion YT
				if (wasUpperCase) {
					parts.add(currentPart.substring(0, currentPart.length() - 1));
					currentPart.delete(0, currentPart.length() - 1);
				} else {
					parts.add(currentPart.toString());
					currentPart.setLength(0);
				}
			}
			wasUpperCase = upperCase;
			currentPart.append(c);
		}
		if (!currentPart.isEmpty())
			parts.add(currentPart.toString());
		
		// Generate lines, maximizing the number of parts per line
		List<String> lines = new ArrayList<>();
		String line = "";
		int i = 0;
		for (String part : parts) {
			String partAddition = (!line.isEmpty() && spaces.contains(i) ? " " : "") + part;
			if (renderer.getWidth(line + partAddition) > maxWidth) {
				if (!line.isEmpty()) {
					lines.add(line);
					line = "";
				}
				
				if (renderer.getWidth(part) > maxWidth) {
					while (true) {
						int numChars = 1;
						while (renderer.getWidth(part.substring(0, numChars)) < maxWidth)
							numChars++;
						numChars--;
						lines.add(part.substring(0, numChars));
						part = part.substring(numChars);
						if (renderer.getWidth(part) < maxWidth) {
							line = part;
							break;
						}
					}
				} else
					line = part;
			} else
				line += partAddition;
			i++;
		}
		if (!line.isEmpty())
			lines.add(line);
		
		
		// Draw the lines
		for (i = 0; i < lines.size(); i++) {
			line = lines.get(i);
			int offsetY = i * renderer.fontHeight + (centerVertical ? -renderer.fontHeight * lines.size() / 2 : 0);
			if (centerHorizontal)
				MultiVersionDrawableHelper.drawCenteredTextWithShadow(matrices, renderer, TextInst.of(line), x, y + offsetY, color);
			else
				MultiVersionDrawableHelper.drawTextWithShadow(matrices, renderer, TextInst.of(line), x, y + offsetY, color);
		}
	}
	
	
	public static String colorize(String text) {
		StringBuilder output = new StringBuilder();
		boolean colorCode = false;
		for (char c : text.toCharArray()) {
			if (c == '&')
				colorCode = true;
			else {
				if (colorCode) {
					colorCode = false;
					if ((c + "").replaceAll("[0-9a-fA-Fk-oK-OrR]", "").isEmpty())
						output.append('§');
					else
						output.append('&');
				}
				
				output.append(c);
			}
		}
		if (colorCode)
			output.append('&');
		return output.toString();
	}
	public static String stripColor(String text) {
		return text.replaceAll("\\xA7[0-9a-fA-Fk-oK-OrR]", "");
	}
	
	
	public static Text getItemNameSafely(ItemStack item) {
		NbtCompound nbtCompound = item.getSubNbt(ItemStack.DISPLAY_KEY);
        if (nbtCompound != null && nbtCompound.contains(ItemStack.NAME_KEY, 8)) {
            try {
                MutableText text = Text.Serializer.fromJson(nbtCompound.getString(ItemStack.NAME_KEY));
                if (text != null) {
                    return text;
                }
            }
            catch (JsonParseException text) {
            }
        }
        return item.getItem().getName(item);
	}
	
	
	public static EditableText getLongTranslatableText(String key) {
		EditableText output = TextInst.translatable(key + "_1");
		for (int i = 2; true; i++) {
			Text line = TextInst.translatable(key + "_" + i);
			String str = line.getString();
			if (str.equals(key + "_" + i) || i > 50)
				break;
			if (str.startsWith("[LINK] ")) {
				String url = str.substring("[LINK] ".length());
				line = TextInst.literal(url).styled(style -> style.withClickEvent(new ClickEvent(Action.OPEN_URL, url))
						.withUnderline(true).withItalic(true).withColor(Formatting.GOLD));
			}
			if (str.startsWith("[FORMAT] ")) {
				String toFormat = str.substring("[FORMAT] ".length());
				line = parseFormattedText(toFormat);
			}
			output.append("\n").append(line);
		}
		return output;
	}
	
	
	public static DyeColor getDyeColor(Formatting color) {
		switch (color) {
			case AQUA:
				return DyeColor.LIGHT_BLUE;
			case BLACK:
				return DyeColor.BLACK;
			case BLUE:
				return DyeColor.BLUE;
			case DARK_AQUA:
				return DyeColor.CYAN;
			case DARK_BLUE:
				return DyeColor.BLUE;
			case DARK_GRAY:
				return DyeColor.GRAY;
			case DARK_GREEN:
				return DyeColor.GREEN;
			case DARK_PURPLE:
				return DyeColor.PURPLE;
			case DARK_RED:
				return DyeColor.RED;
			case GOLD:
				return DyeColor.ORANGE;
			case GRAY:
				return DyeColor.LIGHT_GRAY;
			case GREEN:
				return DyeColor.LIME;
			case LIGHT_PURPLE:
				return DyeColor.PINK;
			case RED:
				return DyeColor.RED;
			case WHITE:
				return DyeColor.WHITE;
			case YELLOW:
				return DyeColor.YELLOW;
			default:
				return DyeColor.BROWN;
		}
	}
	
	
	public static Text substring(Text text, int start, int end) {
		EditableText output = TextInst.literal("");
		text.visit(new StyledVisitor<Boolean>() {
			private int i;
			@Override
			public Optional<Boolean> accept(Style style, String str) {
				if (i + str.length() <= start) {
					i += str.length();
					return Optional.empty();
				}
				if (i >= start) {
					if (end >= 0 && i + str.length() > end)
						return accept(style, str.substring(0, end - i));
					output.append(TextInst.literal(str).fillStyle(style));
					i += str.length();
					if (end >= 0 && i == end)
						return Optional.of(true);
					return Optional.empty();
				} else {
					str = str.substring(start - i);
					i = start;
					accept(style, str);
					return Optional.empty();
				}
			}
		}, Style.EMPTY);
		return output;
	}
	public static Text substring(Text text, int start) {
		return substring(text, start, -1);
	}
	
	
	public static ItemStack copyAirable(ItemStack item) {
		ItemStack output = new ItemStack(item.getItem(), item.getCount());
		output.setBobbingAnimationTime(item.getBobbingAnimationTime());
		if (item.getNbt() != null)
			output.setNbt(item.getNbt().copy());
		return output;
	}
	
	
	public static Text parseFormattedText(String text) {
		try {
			return FancyTextArgumentType.fancyText(false).parse(new StringReader(text));
		} catch (CommandSyntaxException e) {
			return TextInst.literal(text);
		}
	}
	public static Text parseTranslatableFormatted(String key) {
		return parseFormattedText(TextInst.translatable(key).getString());
	}
	
	
	public static ItemStack setType(Item type, ItemStack item, int count) {
		NbtCompound fullData = new NbtCompound();
		item.writeNbt(fullData);
		fullData.putString("id", MultiVersionRegistry.ITEM.getId(type).toString());
		fullData.putInt("Count", count);
		return ItemStack.fromNbt(fullData);
	}
	public static ItemStack setType(Item type, ItemStack item) {
		return setType(type, item, item.getCount());
	}
	
	
	@SuppressWarnings("unchecked")
	public static <T> Event<T> newEvent(Class<T> clazz) {
		return EventFactory.createArrayBacked(clazz, listeners -> {
			return (T) Proxy.newProxyInstance(MainUtil.class.getClassLoader(), new Class<?>[] {clazz}, (obj, method, args) -> {
				for (T listener : listeners) {
					ActionResult result = (ActionResult) method.invoke(listener, args);
					if (result != ActionResult.PASS)
						return result;
				}
				return ActionResult.PASS;
			});
		});
	}
	
	
	public static NbtCompound readNBT(InputStream in) throws IOException {
		byte[] data = in.readAllBytes();
		DataInputStream resetableIn = new DataInputStream(new ByteArrayInputStream(data));
		NbtCompound nbt;
		try {
			nbt = NbtIo.readCompressed(resetableIn);
		} catch (ZipException e) {
			resetableIn.reset();
			nbt = NbtIo.read(resetableIn);
		}
		return nbt;
	}
	
	
	public static boolean isTextFormatted(Text text, boolean allowNonNull) {
		return isTextFormatted(Text.Serializer.toJsonTree(text).getAsJsonObject(), allowNonNull);
	}
	private static boolean isTextFormatted(JsonObject data, boolean allowNonNull) {
		if (data.has("extra")) {
			for (JsonElement part : data.get("extra").getAsJsonArray()) {
				if (isTextFormatted(part.getAsJsonObject(), allowNonNull))
					return true;
			}
		}
		
		if (!allowNonNull)
			return data.keySet().stream().anyMatch(key -> !key.equals("text") && !key.equals("extra"));
		
		if (data.has("bold") && data.get("bold").getAsBoolean())
			return true;
		if (data.has("italic") && data.get("italic").getAsBoolean())
			return true;
		if (data.has("underlined") && data.get("underlined").getAsBoolean())
			return true;
		if (data.has("strikethrough") && data.get("strikethrough").getAsBoolean())
			return true;
		if (data.has("obfuscated") && data.get("obfuscated").getAsBoolean())
			return true;
		if (data.has("color") && !data.get("color").getAsString().equals("white"))
			return true;
		if (data.has("insertion") && data.get("insertion").getAsBoolean())
			return true;
		if (data.has("clickEvent"))
			return true;
		if (data.has("hoverEvent"))
			return true;
		if (data.has("font") && !data.get("font").getAsString().equals(Style.DEFAULT_FONT_ID.toString()))
			return true;
		
		return false;
	}
	
	
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss", Locale.ROOT);
	public static String getFormattedCurrentTime() {
		return DATE_TIME_FORMATTER.format(ZonedDateTime.now());
	}
	
	
	public static Text attachFileTextOptions(EditableText link, File file) {
		return link.append(" ").append(TextInst.translatable("nbteditor.file_options.show").styled(style ->
				style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE,
						file.getAbsoluteFile().getParentFile().getAbsolutePath()))))
				.append(" ").append(TextInst.translatable("nbteditor.file_options.delete").styled(style ->
				MixinLink.withRunClickEvent(style, () -> client.setScreen(
						new FancyConfirmScreen(confirmed -> {
							if (confirmed) {
								if (file.exists()) {
									try {
										Files.deleteIfExists(file.toPath());
										client.player.sendMessage(TextInst.translatable("nbteditor.file_options.delete.success", "§6" + file.getName()), false);
									} catch (IOException e) {
										NBTEditor.LOGGER.error("Error deleting file", e);
										client.player.sendMessage(TextInst.translatable("nbteditor.file_options.delete.error", "§6" + file.getName()), false);
									}
								} else
									client.player.sendMessage(TextInst.translatable("nbteditor.file_options.delete.missing", "§6" + file.getName()), false);
							}
							client.setScreen(null);
						}, TextInst.translatable("nbteditor.file_options.delete.title", file.getName()),
								TextInst.translatable("nbteditor.file_options.delete.desc", file.getName()))))));
	}
	
	
	public static boolean equals(double a, double b, double epsilon) {
		return Math.abs(a - b) <= epsilon;
	}
	public static boolean equals(double a, double b) {
		return equals(a, b, 1E-5);
	}
	
	
	public static BufferedImage scaleImage(BufferedImage img, int width, int height) {
		Image temp = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
		BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = output.createGraphics();
		g.drawImage(temp, 0, 0, null);
		g.dispose();
		return output;
	}
	
	
	public static int[] getMousePos() {
		double scale = (double) client.getWindow().getScaledWidth() / client.getWindow().getWidth();
		int x = (int) (client.mouse.getX() * scale);
		int y = (int) (client.mouse.getY() * scale);
		return new int[] {x, y};
	}
	
	
	public static void mapMatrices(MatrixStack matrices,
			int fromX, int fromY, int fromWidth, int fromHeight,
			int toX, int toY, int toWidth, int toHeight) {
		matrices.translate(toX, toY, 0.0);
		matrices.scale((float) toWidth / fromWidth, (float) toHeight / fromHeight, 1);
		matrices.translate(-fromX, -fromY, 0.0);
	}
	
	
	public static Predicate<String> intPredicate(Supplier<Integer> min, Supplier<Integer> max, boolean allowEmpty) {
		return str -> {
			if (str.isEmpty())
				return allowEmpty;
			try {
				int value = Integer.parseInt(str);
				return (min == null || min.get() <= value) && (max == null || value <= max.get());
			} catch (NumberFormatException e) {
				return false;
			}
		};
	}
	public static Predicate<String> intPredicate(Integer min, Integer max, boolean allowEmpty) {
		return intPredicate(() -> min, () -> max, allowEmpty);
	}
	public static Predicate<String> intPredicate() {
		return intPredicate((Supplier<Integer>) null, null, true);
	}
	
	public static Integer parseOptionalInt(String str) {
		try {
			return Integer.parseInt(str);
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
	
	public static boolean styleEqualsExact(Style a, Style b) {
		return Objects.equals(a.getColor(), b.getColor()) &&
				a.bold == b.bold &&
				a.italic == b.italic &&
				a.underlined == b.underlined &&
				a.strikethrough == b.strikethrough &&
				a.obfuscated == b.obfuscated &&
				Objects.equals(a.getClickEvent(), b.getClickEvent()) &&
				Objects.equals(a.getHoverEvent(), b.getHoverEvent()) &&
				Objects.equals(a.getInsertion(), b.getInsertion()) &&
				Objects.equals(a.getFont(), b.getFont());
	}
	
	public static boolean hasFormatting(Style style, Formatting formatting) {
		return styleEqualsExact(style, style.withFormatting(formatting));
	}
	
	public static Style removeFormatting(Style style, Formatting formatting, boolean force) {
		if (formatting == Formatting.RESET)
			return style;
		if (formatting.isColor())
			return style.withColor(force ? Formatting.WHITE : null);
		Boolean newValue = (force ? false : null);
		return switch (formatting) {
			case BOLD -> style.withBold(newValue);
			case ITALIC -> style.withItalic(newValue);
			case UNDERLINE -> style.withUnderline(newValue);
			case STRIKETHROUGH -> style.withStrikethrough(newValue);
			case OBFUSCATED -> style.withObfuscated(newValue);
			default -> throw new IllegalArgumentException("Unknown formatting: " + formatting);
		};
	}
	
	public static Style forceReset(Style base) {
		return Style.EMPTY
				.withColor(base.getColor() == null ||
						base.getColor().equals(TextColor.fromFormatting(Formatting.WHITE)) ? null : Formatting.WHITE)
				.withBold(base.bold == null || !base.bold ? null : false)
				.withItalic(base.italic == null || !base.italic ? null : false)
				.withUnderline(base.underlined == null || !base.underlined ? null : false)
				.withStrikethrough(base.strikethrough == null || !base.strikethrough ? null : false)
				.withObfuscated(base.obfuscated == null || !base.obfuscated ? null : false);
	}
	
	
	public static Text stripInvalidChars(Text text, boolean allowLineBreaks) {
		EditableText output = TextInst.literal("");
		text.visit((style, str) -> {
			output.append(TextInst.literal(MultiVersionMisc.stripInvalidChars(str, allowLineBreaks)).setStyle(style));
			return Optional.empty();
		}, Style.EMPTY);
		return output;
	}
	
	public static int lastIndexOf(Text text, int ch) {
		AtomicInteger output = new AtomicInteger(-1);
		AtomicInteger pos = new AtomicInteger(0);
		text.visit(str -> {
			int i = str.lastIndexOf(ch);
			if (i != -1)
				output.setPlain(pos.getPlain() + i);
			pos.setPlain(pos.getPlain() + str.length());
			return Optional.empty();
		});
		return output.getPlain();
	}
	
	public static Text deleteCharAt(Text text, int index) {
		EditableText output = TextInst.literal("");
		AtomicInteger pos = new AtomicInteger(0);
		text.visit((style, str) -> {
			int strLen = str.length();
			if (pos.getPlain() <= index && index < pos.getPlain() + strLen)
				str = new StringBuilder(str).deleteCharAt(index - pos.getPlain()).toString();
			if (!str.isEmpty())
				output.append(TextInst.literal(str).setStyle(style));
			pos.setPlain(pos.getPlain() + strLen);
			return Optional.empty();
		}, Style.EMPTY);
		return output;
	}
	
	public static Text joinLines(List<Text> lines) {
		EditableText output = TextInst.literal("");
		for (int i = 0; i < lines.size(); i++) {
			if (i > 0)
				output.append("\n");
			output.append(lines.get(i));
		}
		return output;
	}
	public static List<Text> splitText(Text text) {
		List<Text> output = new ArrayList<>();
		int i;
		while ((i = text.getString().indexOf('\n')) != -1) {
			output.add(MainUtil.substring(text, 0, i));
			text = MainUtil.substring(text, i + 1);
		}
		output.add(text);
		return output;
	}
	
}
