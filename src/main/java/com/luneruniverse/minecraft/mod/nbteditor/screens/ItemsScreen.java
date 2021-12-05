package com.luneruniverse.minecraft.mod.nbteditor.screens;

import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

public class ItemsScreen extends ClientContainerScreen {
	
	private ItemStack item;
	private Hand hand;
	private int hotbarSlot;
	private int slot;
	
	public ItemsScreen(GenericContainerScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
	}
	public ItemsScreen build(ItemStack item, Hand hand) {
		this.item = item;
		this.hand = hand;
		this.hotbarSlot = hand == Hand.MAIN_HAND ? MinecraftClient.getInstance().player.getInventory().selectedSlot : -100;
		this.slot = hand == Hand.MAIN_HAND ? this.hotbarSlot + 36 + 27 - 9 : -100;
		
		if (item.hasNbt()) {
			NbtCompound nbt = item.getNbt();
			if (nbt.contains("BlockEntityTag", NbtType.COMPOUND)) {
				NbtCompound blockEntityTag = nbt.getCompound("BlockEntityTag");
				if (blockEntityTag.contains("Items", NbtType.LIST)) {
					NbtList items = blockEntityTag.getList("Items", NbtType.COMPOUND);
					if (!items.isEmpty()) {
						for (NbtElement containedItemElement : items) {
							NbtCompound containedItem = (NbtCompound) containedItemElement;
							try {
								this.handler.getSlot(containedItem.getByte("Slot")).setStack(ItemStack.fromNbt(containedItem));
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
		
		return this;
	}
	
	@Override
	protected void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType) {
		if (slot != null && slot.id == this.slot)
			return;
		if (actionType == SlotActionType.SWAP && button == this.hotbarSlot)
			return;
		
		super.onMouseClick(slot, slotId, button, actionType);
		
		NbtCompound nbt = item.getOrCreateNbt();
		if (!nbt.contains("BlockEntityTag", NbtType.COMPOUND))
			nbt.put("BlockEntityTag", new NbtCompound());
		NbtCompound blockEntityTag = nbt.getCompound("BlockEntityTag");
		NbtList items = blockEntityTag.getList("Items", NbtType.COMPOUND);
		blockEntityTag.put("Items", items);
		
		items.clear();
		for (int i = 0; i < this.handler.getInventory().size(); i++) {
			ItemStack item = this.handler.getInventory().getStack(i);
			if (item == null || item.isEmpty())
				continue;
			NbtCompound itemNbt = new NbtCompound();
			itemNbt.putByte("Slot", (byte) i);
			item.writeNbt(itemNbt);
			items.add(itemNbt);
		}
		
		MainUtil.saveItem(hand, item);
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		super.render(matrices, mouseX, mouseY, delta);
		MainUtil.renderLogo(matrices);
	}
	
}
