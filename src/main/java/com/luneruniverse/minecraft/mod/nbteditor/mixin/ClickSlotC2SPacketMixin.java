package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.packets.ClickSlotC2SPacketParent;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;

import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;

@Mixin(ClickSlotC2SPacket.class)
public class ClickSlotC2SPacketMixin implements ClickSlotC2SPacketParent {
	private static final int NO_ARMOR_RESTRICTION_FLAG = 0b01000000;
	
	@Shadow
	private int button;
	
	@ModifyVariable(method = "<init>(IIIILnet/minecraft/screen/slot/SlotActionType;Lnet/minecraft/item/ItemStack;Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;)V", at = @At("HEAD"), ordinal = 3)
	private static int init(int button) {
		if (ConfigScreen.isNoArmorRestriction())
			return button | NO_ARMOR_RESTRICTION_FLAG;
		return button;
	}
	@Inject(method = "getButton", at = @At("RETURN"), cancellable = true)
	private void getButton(CallbackInfoReturnable<Integer> info) {
		info.setReturnValue(info.getReturnValue() & ~NO_ARMOR_RESTRICTION_FLAG);
	}
	@Override
	public boolean isNoArmorRestriction() {
		return (button & NO_ARMOR_RESTRICTION_FLAG) != 0;
	}
}
