package com.luneruniverse.minecraft.mod.nbteditor.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import com.google.gson.JsonParseException;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.ClickEvent.Action;
import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable.StyledVisitor;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

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
		
		return new ItemReference(hand);
	}
	public static ItemReference getHeldItem() throws CommandSyntaxException {
		return getHeldItem(item -> true, Text.translatable("nbteditor.noitem"));
	}
	public static ItemReference getHeldItemAirable() {
		try {
			return getHeldItem();
		} catch (CommandSyntaxException e) {
			return new ItemReference(Hand.MAIN_HAND);
		}
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
		client.player.getInventory().setStack(slot, item.copy());
		client.interactionManager.clickCreativeStack(item, slot < 9 ? slot + 36 : slot);
	}
	public static void saveItemInvSlot(int slot, ItemStack item) {
		saveItem(slot == 45 ? 45 : (slot >= 36 ? slot - 36 : slot), item);
	}
	
	
	
	private static final Identifier LOGO = new Identifier("nbteditor", "textures/logo.png");
	public static void renderLogo(MatrixStack matrices) {
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, LOGO);
		Screen.drawTexture(matrices, 16, 16, 0, 0, 32, 32, 32, 32);
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
				Screen.drawCenteredTextWithShadow(matrices, renderer, Text.of(line).asOrderedText(), x, y + offsetY, color);
			else
				Screen.drawTextWithShadow(matrices, renderer, Text.of(line), x, y + offsetY, color);
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
	
	
	public static MutableText getLongTranslatableText(String key) {
		MutableText output = Text.translatable(key + "_1");
		for (int i = 2; true; i++) {
			Text line = Text.translatable(key + "_" + i);
			String str = line.getString();
			if (str.equals(key + "_" + i) || i > 50)
				break;
			if (str.startsWith("[LINK] ")) {
				String url = str.substring("[LINK] ".length());
				line = Text.literal(url).styled(style -> style.withClickEvent(new ClickEvent(Action.OPEN_URL, url))
						.withUnderline(true).withItalic(true).withColor(Formatting.GOLD));
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
		MutableText output = Text.literal("");
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
					output.append(Text.literal(str).fillStyle(style));
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
	
	
	private static final int itemX = 16 + 32 + 8;
	private static final int itemY = 16;
	// First edited from HandledScreen#drawItem to NBTEdtiorScreen#drawItem to include the scale
	// Then moved here with default arguments
	public static void renderItem(ItemStack stack) {
		// Args
		int x = itemX;
		int y = itemY;
		int scaleX = 2;
		int scaleY = 2;
		
		// Other variables
		Screen screen = client.currentScreen;
		ItemRenderer itemRenderer = client.getItemRenderer();
		TextRenderer textRenderer = client.textRenderer;
		
		// Function
		x /= scaleX;
		y /= scaleY;
		
		MatrixStack matrixStack = RenderSystem.getModelViewStack();
		matrixStack.push();
		matrixStack.translate(0.0D, 0.0D, 32.0D);
		matrixStack.scale(scaleX, scaleY, 1);
		RenderSystem.applyModelViewMatrix();
		screen.setZOffset(200);
		itemRenderer.zOffset = 200.0F;
		itemRenderer.renderInGuiWithOverrides(stack, x, y);
		itemRenderer.renderGuiItemOverlay(textRenderer, stack, x, y, null);
		screen.setZOffset(0);
		itemRenderer.zOffset = 0.0F;
		matrixStack.pop();
		RenderSystem.applyModelViewMatrix();
	}
	
	
	public static void addEnchants(Map<Enchantment, Integer> enchants, ItemStack stack) {
		String key = (stack.getItem() == Items.ENCHANTED_BOOK ? EnchantedBookItem.STORED_ENCHANTMENTS_KEY : "Enchantments");
		NbtList enchantsNbt = stack.getOrCreateNbt().getList(key, NbtType.COMPOUND);
		enchants.forEach((type, lvl) -> enchantsNbt.add(EnchantmentHelper.createNbt(Registry.ENCHANTMENT.getId(type), lvl)));
		stack.getOrCreateNbt().put(key, enchantsNbt);
	}
	
	
	public static ItemStack copyAirable(ItemStack item) {
		ItemStack output = new ItemStack(item.getItem(), item.getCount());
		output.setBobbingAnimationTime(item.getBobbingAnimationTime());
		if (item.getNbt() != null)
			output.setNbt(item.getNbt().copy());
		return output;
	}
	
}
