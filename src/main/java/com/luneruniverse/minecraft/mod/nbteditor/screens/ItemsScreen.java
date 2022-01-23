package com.luneruniverse.minecraft.mod.nbteditor.screens;

import org.lwjgl.glfw.GLFW;

import com.luneruniverse.minecraft.mod.nbteditor.util.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.BarrelBlock;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;

public class ItemsScreen extends ClientContainerScreen {
	
	public static boolean isContainer(ItemStack item) {
		return item.getItem() instanceof BlockItem && (
				((BlockItem) item.getItem()).getBlock() instanceof ShulkerBoxBlock ||
				((BlockItem) item.getItem()).getBlock() instanceof ChestBlock ||
				((BlockItem) item.getItem()).getBlock() instanceof BarrelBlock
			);
	}
	
	
	
	private boolean saved;
	private final Text unsavedTitle;
	
	private ItemReference ref;
	private ItemStack item;
	private int slot;
	
	public ItemsScreen(GenericContainerScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
		
		this.saved = true;
		this.unsavedTitle = title.shallowCopy().append("*");
	}
	private ItemsScreen build(ItemReference ref) {
		this.ref = ref;
		this.item = ref.getItem().copy();
		this.slot = ref.getHand() == Hand.MAIN_HAND ? ref.getHotbarSlot() + 36 + 27 - 9 : (ref.getHand() == Hand.OFF_HAND ? 40 : ref.getInvSlot() + 27);
		
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
	public static void show(ItemReference ref) {
		PlayerInventory inv = MainUtil.client.player.getInventory();
		MainUtil.client.setScreen(new ItemsScreen(new ItemsHandler(0, inv), inv, new TranslatableText("nbteditor.items")
				.append(ref.getItem().getName())).build(ref));
	}
	
	@Override
	protected void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType) {
		if (slot != null && slot.id == this.slot)
			return;
		if (actionType == SlotActionType.SWAP && button == ref.getHotbarSlot())
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
		
		saved = false;
		ref.saveItem(item, () -> {
			saved = true;
		});
	}
	
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (ref.keyPressed(keyCode, scanCode, modifiers))
			return true;
		
		if (keyCode == GLFW.GLFW_KEY_SPACE) {
			Slot hoveredSlot = this.focusedSlot;
			if (hoveredSlot != null && hoveredSlot.getStack() != null && !hoveredSlot.getStack().isEmpty()) {
				int slot = hoveredSlot.getIndex();
				ItemReference ref = hoveredSlot.inventory == client.player.getInventory() ? new ItemReference(slot >= 36 ? slot - 36 : slot) : new ItemReference(this.ref, hoveredSlot.getIndex());
				if (hasControlDown()) {
					if (ItemsScreen.isContainer(hoveredSlot.getStack()))
						ItemsScreen.show(ref);
				} else
					client.setScreen(new NBTEditorScreen(ref));
				return true;
			}
		}
		
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		super.render(matrices, mouseX, mouseY, delta);
		MainUtil.renderLogo(matrices);
	}
	
	@Override
	protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
		this.textRenderer.draw(matrices, saved ? this.title : this.unsavedTitle, (float)this.titleX, (float)this.titleY, 4210752);
		this.textRenderer.draw(matrices, this.playerInventoryTitle, (float)this.playerInventoryTitleX, (float)this.playerInventoryTitleY, 4210752);
	}
	
	public ItemReference getReference() {
		return ref;
	}
	
}
