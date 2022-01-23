package com.luneruniverse.minecraft.mod.nbteditor.screens;

import java.io.IOException;

import org.lwjgl.glfw.GLFW;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.util.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.luneruniverse.minecraft.mod.nbteditor.util.SaveQueue;

import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class ClientChestScreen extends ClientContainerScreen {
	
	public static int PAGE = 0;
	public static int prevPageJumpTarget;
	public static int nextPageJumpTarget;
	
	public static void show() {
		if (MainUtil.client.currentScreen instanceof ClientChestScreen) {
			ClientChestScreen screen = (ClientChestScreen) MainUtil.client.currentScreen;
			((ClientChestHandler) screen.handler).fillPage();
			screen.updatePageNavigation();
		} else {
			ClientPlayerEntity player = MainUtil.client.player;
			ClientChestHandler handler = new ClientChestHandler(0, player.getInventory());
			MainUtil.client.setScreen(new ClientChestScreen(handler, player.getInventory(), new TranslatableText("nbteditor.clientchest")));
		}
	}
	
	
	
	private final SaveQueue saveQueue = new SaveQueue("Client Chest", () -> {
		int page = PAGE; // Thread safe
		
		ItemStack[] items = new ItemStack[54];
		for (int i = 0; i < this.handler.getInventory().size(); i++)
			items[i] = this.handler.getInventory().getStack(i).copy();
		try {
			NBTEditorClient.setClientChestPage(page, items);
		} catch (IOException e) {
			e.printStackTrace();
			this.client.player.sendMessage(new TranslatableText("nbteditor.storage_save_error"), false);
		}
	});
	private boolean saved;
	private Text unsavedTitle;
	
	private boolean navigationClicked;
	private ButtonWidget prevPage;
	private TextFieldWidget pageField;
	private ButtonWidget nextPage;
	private ButtonWidget prevPageJump;
	private ButtonWidget nextPageJump;
	
	public ClientChestScreen(GenericContainerScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
		this.dropCursorOnClose = false;
		this.saved = true;
		this.unsavedTitle = title.copy().append("*");
	}
	
	@Override
	protected void init() {
		this.clearChildren();
		super.init();
		x += 87 / 2;
		
		this.addDrawableChild(new CreativeTab(this, new ItemStack(Items.BRICKS).setCustomName(new TranslatableText("itemGroup.nbteditor.creative")), () -> client.setScreen(new CreativeInventoryScreen(client.player))));
		
		this.addDrawableChild(prevPage = new ButtonWidget(this.x - 87, this.y, 20, 20, Text.of("<"), btn -> {
			navigationClicked = true;
			PAGE--;
			pageField.setText((PAGE + 1) + "");
			show();
		}));
		
		pageField = new TextFieldWidget(textRenderer, this.x - 63, this.y + 2, 35, 16, Text.of("")) {
			@Override
			public boolean mouseClicked(double mouseX, double mouseY, int button) {
				boolean output = super.mouseClicked(mouseX, mouseY, button);
				if (output)
					navigationClicked = true;
				return output;
			}
		};
		pageField.setMaxLength(1000);
		pageField.setText((PAGE + 1) + "");
		pageField.setChangedListener(str -> {
			if (str.isEmpty() || str.equals("0"))
				return;
			
			PAGE = Integer.parseInt(str) - 1;
			show();
		});
		pageField.setTextPredicate((str) -> {
			if (str.isEmpty() || str.equals("0"))
				return true;
			
			try {
				int num = Integer.parseInt(str);
				return num > 0 && num <= 100;
			} catch (NumberFormatException e) {
				return false;
			}
		});
		this.addSelectableChild(pageField);
		
		this.addDrawableChild(nextPage = new ButtonWidget(this.x - 24, this.y, 20, 20, Text.of(">"), btn -> {
			navigationClicked = true;
			PAGE++;
			pageField.setText((PAGE + 1) + "");
			show();
		}));
		
		this.addDrawableChild(prevPageJump = new ButtonWidget(this.x - 87, this.y + 24, 39, 20, Text.of("<<"), btn -> {
			navigationClicked = true;
			PAGE = prevPageJumpTarget;
			pageField.setText((PAGE + 1) + "");
			show();
		}));
		
		this.addDrawableChild(nextPageJump = new ButtonWidget(this.x - 43, this.y + 24, 39, 20, Text.of(">>"), btn -> {
			navigationClicked = true;
			PAGE = nextPageJumpTarget;
			pageField.setText((PAGE + 1) + "");
			show();
		}));
		
		this.addDrawableChild(new ButtonWidget(this.x - 87, this.y + 48, 83, 20, Text.of("Reload"), btn -> {
			navigationClicked = true;
			try {
				NBTEditorClient.loadClientChestPage(PAGE);
				show();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}));
		
		this.addDrawableChild(new ButtonWidget(this.x - 87, this.y + 72, 83, 20, Text.of("Clear Page"), btn -> {
			navigationClicked = true;
			client.setScreen(new ConfirmScreen(value -> {
				if (value) {
					this.handler.getInventory().clear();
					save();
				}
				
				client.setScreen(ClientChestScreen.this);
			}, new TranslatableText("nbteditor.clearpage.title"), new TranslatableText("nbteditor.clearpage.message"),
					new TranslatableText("nbteditor.clearpage.yes"), new TranslatableText("nbteditor.clearpage.no")));
		}));
		
		
		updatePageNavigation();
	}
	public void updatePageNavigation() {
		int[] jumps = NBTEditorClient.getClientChestJumpPages(PAGE);
		prevPageJumpTarget = jumps[0] == -1 ? (PAGE == 0 ? -1 : 0) : jumps[0];
		nextPageJumpTarget = jumps[1] == -1 ? (PAGE == 99 ? -1 : 99) : jumps[1];
		
		prevPage.active = PAGE != 0;
		nextPage.active = PAGE != 99;
		prevPageJump.active = prevPageJumpTarget != -1;
		nextPageJump.active = nextPageJumpTarget != -1;
	}
	
	@Override
	protected void handledScreenTick() {
		pageField.tick();
	}
	
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ESCAPE)
			onClose();
		
		if (keyCode == GLFW.GLFW_KEY_SPACE) {
			Slot hoveredSlot = this.focusedSlot;
			if (hoveredSlot != null && hoveredSlot.getStack() != null && !hoveredSlot.getStack().isEmpty()) {
				int slot = hoveredSlot.getIndex();
				ItemReference ref = hoveredSlot.inventory == client.player.getInventory() ? new ItemReference(slot >= 36 ? slot - 36 : slot) : new ItemReference(PAGE, hoveredSlot.getIndex());
				if (hasControlDown()) {
					if (ItemsScreen.isContainer(hoveredSlot.getStack()))
						ItemsScreen.show(ref);
				} else
					client.setScreen(new NBTEditorScreen(ref));
				return true;
			}
		}
		
		return !this.pageField.keyPressed(keyCode, scanCode, modifiers) && !this.pageField.isActive()
				? super.keyPressed(keyCode, scanCode, modifiers) : true;
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		navigationClicked = false;
		this.client.keyboard.setRepeatEvents(this.pageField.mouseClicked(mouseX, mouseY, button));
		super.mouseClicked(mouseX, mouseY, button);
		return true;
	}
	
	@Override
	protected void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType) {
		if (navigationClicked)
			return;
		
		super.onMouseClick(slot, slotId, button, actionType);
		
		ItemStack[] page = NBTEditorClient.getClientChestPage(PAGE);
		for (int i = 0; i < this.handler.getInventory().size(); i++) {
			if (!ItemStack.areEqual(page[i] == null ? ItemStack.EMPTY : page[i], this.handler.getInventory().getStack(i))) {
				save();
				break;
			}
		}
	}
	
	private void save() {
		saved = false;
		saveQueue.save(() -> {
			saved = true;
		});
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		super.render(matrices, mouseX, mouseY, delta);
		pageField.render(matrices, mouseX, mouseY, delta);
		MainUtil.renderLogo(matrices);
	}
	
	@Override
	protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
		this.textRenderer.draw(matrices, saved ? this.title : this.unsavedTitle, (float)this.titleX, (float)this.titleY, 4210752);
		this.textRenderer.draw(matrices, this.playerInventoryTitle, (float)this.playerInventoryTitleX, (float)this.playerInventoryTitleY, 4210752);
	}
	
	@Override
	public void removed() {
		this.client.keyboard.setRepeatEvents(false);
	}
	
}
