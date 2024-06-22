package com.luneruniverse.minecraft.mod.nbteditor.misc;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.WeakHashMap;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.commands.get.GetLostItemCommand;
import com.luneruniverse.minecraft.mod.nbteditor.mixin.ChatScreenAccessor;
import com.luneruniverse.minecraft.mod.nbteditor.mixin.HandledScreenAccessor;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ServerItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.CreativeTab;
import com.luneruniverse.minecraft.mod.nbteditor.screens.containers.ClientHandledScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.Enchants;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.BookScreen.WrittenBookContents;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.screen.LecternScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;

// Non-mixin classes in the mixin package doesn't work well
public class MixinLink {
	
	public static boolean CLIENT_LOADED = false;
	
	public static void addCreativeTabs(Screen source) {
		int i = -1;
		List<CreativeTab> tabs = new ArrayList<>();
		for (CreativeTab.CreativeTabData tab : CreativeTab.TABS) {
			if (tab.whenToShow().test(source))
				tabs.add(new CreativeTab(source, (++i) * (CreativeTab.WIDTH + 2) + 10, tab.item(), tab.onClick()));
		}
		if (!tabs.isEmpty())
			source.addDrawableChild(new CreativeTab.CreativeTabGroup(tabs));
	}
	
	
	private static final Map<String, Runnable> events = new HashMap<>();
	public static Style withRunClickEvent(Style style, Runnable onClick) {
		String id = "\0nbteditor_runnable@" + new Random().nextLong(); // \0 is not valid in file paths on most OSs
		events.put(id, onClick);
		return style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, id));
	}
	public static boolean tryRunClickEvent(String id) {
		Runnable onClick = events.get(id);
		if (onClick != null) {
			onClick.run();
			return true;
		}
		return false;
	}
	
	
	public static File screenshotTarget;
	
	
	public static int[] getTooltipSize(List<TooltipComponent> tooltip) {
		int width = 0;
		int height = (tooltip.size() == 1 ? -2 : 0);
		for (TooltipComponent line : tooltip) {
			width = Math.max(width, line.getWidth(MainUtil.client.textRenderer));
			height += line.getHeight();
		}
		return new int[] {width, height};
	}
	public static void renderTooltipFromComponents(MatrixStack matrices, int x, int y, int width, int height, int screenWidth, int screenHeight) {
		x -= 5;
		y -= 5;
		width += 10;
		height += 10;
		
		int newX = x;
		int newY = y;
		int newWidth = width;
		int newHeight = height;
		
		if (width > screenWidth || height > screenHeight) {
			double scale = Math.min((double) screenWidth / width, (double) screenHeight / height);
			newWidth = (int) (width * scale);
			newHeight = (int) (height * scale);
			
			int[] mousePos = MainUtil.getMousePos();
			newX = mousePos[0] + 12;
			newY = mousePos[1] - 12;
		}
		
		if (newX < 0)
			newX = 0;
		else if (newX + newWidth > screenWidth)
			newX = screenWidth - newWidth;
		
		if (newY < 0)
			newY = 0;
		else if (newY + newHeight > screenHeight)
			newY = screenHeight - newHeight;
		
		MainUtil.mapMatrices(matrices, x, y, width, height, newX, newY, newWidth, newHeight);
	}
	
	
	public static final Set<Thread> hiddenExceptionHandlers = Collections.synchronizedSet(new HashSet<>());
	@SuppressWarnings("serial")
	public static class HiddenException extends RuntimeException {
		public HiddenException(String msg, Throwable cause) {
			super(msg, cause);
		}
	}
	public interface DangerousRunnable {
		public void run() throws Throwable;
	}
	public static void throwHiddenException(DangerousRunnable toRun) throws Throwable {
		hiddenExceptionHandlers.add(Thread.currentThread());
		try {
			toRun.run();
		} catch (HiddenException e) {
			throw e.getCause();
		} finally {
			hiddenExceptionHandlers.remove(Thread.currentThread());
		}
	}
	
	
	public static final Set<Thread> actualBookContents = Collections.synchronizedSet(new HashSet<>());
	public static WrittenBookContents getActualContents(ItemStack item) {
		actualBookContents.add(Thread.currentThread());
		try {
			return new WrittenBookContents(item);
		} finally {
			actualBookContents.remove(Thread.currentThread());
		}
	}
	
	
	public static void renderChatLimitWarning(ChatScreen source, MatrixStack matrices) {
		if (!ConfigScreen.isChatLimitExtended())
			return;
		
		TextFieldWidget chatField = ((ChatScreenAccessor) source).getChatField();
		if (chatField.getText().length() > 256) {
			MVDrawableHelper.fill(matrices, source.width - 202, source.height - 40, source.width - 2, source.height - 14, 0xAAFFAA00);
			TextRenderer textRenderer = MainUtil.client.textRenderer;
			MVDrawableHelper.drawCenteredTextWithShadow(matrices, textRenderer, TextInst.translatable("nbteditor.chat_length_warning_1"), source.width - 102, source.height - 40 + textRenderer.fontHeight / 2, 0xFFAA5500);
			MVDrawableHelper.drawCenteredTextWithShadow(matrices, textRenderer, TextInst.translatable("nbteditor.chat_length_warning_2"), source.width - 102, source.height - 28 + textRenderer.fontHeight / 2, 0xFFAA5500);
		}
	}
	
	
	public static final Set<Thread> specialNumbers = Collections.synchronizedSet(new HashSet<>());
	public static NbtElement parseSpecialElement(StringReader reader) throws CommandSyntaxException {
		specialNumbers.add(Thread.currentThread());
		try {
			return new StringNbtReader(reader).parseElement();
		} finally {
			specialNumbers.remove(Thread.currentThread());
		}
	}
	
	
	public static void onMouseClick(HandledScreen<?> source, Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo info) {
		if (!source.getScreenHandler().getCursorStack().isEmpty())
			GetLostItemCommand.addToHistory(source.getScreenHandler().getCursorStack());
		
		boolean creativeInv = (source instanceof CreativeInventoryScreen);
		
		if (!creativeInv && !NBTEditorClient.SERVER_CONN.isScreenEditable())
			return;
		
		if (!Screen.hasControlDown())
			return;
		
		if (actionType == SlotActionType.PICKUP && slot != null &&
				(slot.inventory == MainUtil.client.player.getInventory() || !creativeInv) &&
				(!(source instanceof InventoryScreen) || slot.id > 4)) {
			ItemStack cursor = source.getScreenHandler().getCursorStack();
			ItemStack item = slot.getStack();
			if (cursor == null || cursor.isEmpty() || item == null || item.isEmpty())
				return;
			if (cursor.getItem() == Items.ENCHANTED_BOOK || item.getItem() == Items.ENCHANTED_BOOK) {
				if (cursor.getItem() != Items.ENCHANTED_BOOK) { // Make sure the cursor is an enchanted book
					ItemStack temp = cursor;
					cursor = item;
					item = temp;
				}
				
				new Enchants(item).addEnchants(new Enchants(cursor).getEnchants());
				
				slotId = slot.id;
				
				if (creativeInv) {
					boolean armor = false;
					if (!MVMisc.isCreativeInventoryTabSelected())
						slotId -= 9;
					else if (slotId < 9)
						armor = true;
					
					if (armor)
						MainUtil.saveItem(EquipmentSlot.fromTypeIndex(EquipmentSlot.Type.ARMOR, 8 - slotId), item);
					else
						MainUtil.saveItemInvSlot(slotId, item);
					source.getScreenHandler().setCursorStack(ItemStack.EMPTY);
				} else {
					ItemReference.getContainerItem(slotId, source).saveItem(item);
					new ServerItemReference(-1, source).saveItem(ItemStack.EMPTY);
				}
				
				info.cancel();
			}
		}
	}
	
	public static void keyPressed(HandledScreen<?> source, int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> info) {
		boolean creativeInv = (source instanceof CreativeInventoryScreen);
		
		if (keyCode == GLFW.GLFW_KEY_SPACE) {
			Slot hoveredSlot = ((HandledScreenAccessor) source).getFocusedSlot();
			if (hoveredSlot != null &&
					((creativeInv && hoveredSlot.inventory == MainUtil.client.player.getInventory()) ||
							(!creativeInv && NBTEditorClient.SERVER_CONN.isScreenEditable())) &&
					(!(source instanceof InventoryScreen) || hoveredSlot.id > 4) &&
					(ConfigScreen.isAirEditable() || hoveredSlot.getStack() != null && !hoveredSlot.getStack().isEmpty())) {
				if (creativeInv) {
					int slot = hoveredSlot.getIndex();
					if (!MVMisc.isCreativeInventoryTabSelected())
						slot += 36;
					ClientHandledScreen.handleKeybind(hoveredSlot.getStack(), ItemReference.getInventoryOrArmorItem(slot, true));
				} else {
					ClientHandledScreen.handleKeybind(hoveredSlot.getStack(), ItemReference.getContainerItem(hoveredSlot.id, source));
				}
				info.setReturnValue(true);
			}
		}
	}
	
	
	public static final Map<Thread, PlayerEntity> SCREEN_HANDLER_OWNER = new HashMap<>();
	public static final WeakHashMap<Slot, PlayerEntity> SLOT_OWNER = new WeakHashMap<>();
	
	
	public static final List<ItemStack> ENCHANT_GLINT_FIX = new ArrayList<>();
	
	
	public static final Map<Thread, LecternBlockEntity> getLecternRequests = new HashMap<>();
	public static LecternBlockEntity getLectern(LecternScreenHandler handler, PlayerEntity player) {
		getLecternRequests.put(Thread.currentThread(), null);
		handler.canUse(player);
		return getLecternRequests.remove(Thread.currentThread());
	}
	
	
	public static final List<Thread> BLOCK_ENTITY_WRITE_NBT_WITHOUT_IDENTIFYING_DATA = new ArrayList<>();
	
}
