package tsp.headdb.ported.inventory;

import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public class InventoryClickEvent {
	private final Slot slot;
	private final int slotId;
	private final int button;
	private final SlotActionType actionType;
	private final ClickTypeMod clickType;
	
	public InventoryClickEvent(Slot slot, int slotId, int button, SlotActionType actionType, ClickTypeMod clickType) {
		this.slot = slot;
		this.slotId = slotId;
		this.button = button;
		this.actionType = actionType;
		this.clickType = clickType;
	}
	
	public Slot getSlot() {
		return slot;
	}
	public int getSlotId() {
		return slotId;
	}
	public int getButton() {
		return button;
	}
	public SlotActionType getActionType() {
		return actionType;
	}
	public ClickTypeMod getClickType() {
		return clickType;
	}
}
