package com.luneruniverse.minecraft.mod.nbteditor.screens;

import java.util.List;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

public class CursorHistoryScreen extends ClientContainerScreen {
	
	private ItemStack[] prevInv;
	
	public CursorHistoryScreen(GenericContainerScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
		this.dropCursorOnClose = false;
	}
	private CursorHistoryScreen build(List<ItemStack> items) {
		for (int i = 0; i < this.handler.getInventory().size(); i++) {
			if (i == items.size())
				break;
			this.handler.getSlot(i).setStack(items.get(i).copy());
		}
		
		return this;
	}
	public static void show(List<ItemStack> items) {
		PlayerInventory inv = MainUtil.client.player.getInventory();
		MainUtil.client.setScreen(new CursorHistoryScreen(new CursorHistoryHandler(0, inv), inv, TextInst.translatable("nbteditor.container.title")
				.append(TextInst.translatable("nbteditor.get.lost_item.history"))).build(items));
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		super.render(matrices, mouseX, mouseY, delta);
		MainUtil.renderLogo(matrices);
	}
	
	@Override
	protected void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType) {
		prevInv = new ItemStack[this.handler.getInventory().size()];
		for (int i = 0; i < prevInv.length; i++)
			prevInv[i] = this.handler.getInventory().getStack(i).copy();
		
		super.onMouseClick(slot, slotId, button, actionType);
	}
	
	@Override
	public boolean lockSlots() {
		return true;
	}
	@Override
	public ItemStack[] getPrevInventory() {
		return prevInv;
	}
	
	@Override
	public boolean shouldPause() {
		return true;
	}
	
}
