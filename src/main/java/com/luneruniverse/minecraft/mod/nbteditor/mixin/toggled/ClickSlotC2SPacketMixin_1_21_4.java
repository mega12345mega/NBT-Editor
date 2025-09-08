package com.luneruniverse.minecraft.mod.nbteditor.mixin.toggled;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.packets.ClickSlotC2SPacketParent;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;

import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;

@Mixin(ClickSlotC2SPacket.class)
public class ClickSlotC2SPacketMixin_1_21_4 implements ClickSlotC2SPacketParent {
	private static final int NO_SLOT_RESTRICTIONS_FLAG = 0b01000000;
	
	@Shadow(remap = false)
	@SuppressWarnings("target")
	private int field_12817; // button
	
	@ModifyVariable(method = "<init>(IIIILnet/minecraft/class_1713;Lnet/minecraft/class_1799;Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;)V", at = @At("HEAD"), ordinal = 3, remap = false)
	@Group(name = "<init>", min = 1)
	@SuppressWarnings("target")
	private static int init_new(int button) {
		if (ConfigScreen.isNoSlotRestrictions() && NBTEditorClient.SERVER_CONN.isEditingExpanded())
			return button | NO_SLOT_RESTRICTIONS_FLAG;
		return button;
	}
	@ModifyVariable(method = "<init>(IIILnet/minecraft/class_1713;Lnet/minecraft/class_1799;Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;)V", at = @At("HEAD"), ordinal = 2, remap = false)
	@Group(name = "<init>", min = 1)
	@SuppressWarnings("target")
	private static int init_old(int button) {
		if (Version.<Boolean>newSwitch()
				.range("1.17.1", null, true)
				.range(null, "1.17", false)
				.get()) {
			// https://github.com/SpongePowered/Mixin/issues/677
			return button;
		}
		if (ConfigScreen.isNoSlotRestrictions() && NBTEditorClient.SERVER_CONN.isEditingExpanded())
			return button | NO_SLOT_RESTRICTIONS_FLAG;
		return button;
	}
	
	@Inject(method = "method_12193()I", at = @At("RETURN"), cancellable = true, remap = false)
	@SuppressWarnings("target")
	private void getButton(CallbackInfoReturnable<Integer> info) {
		info.setReturnValue(info.getReturnValue() & ~NO_SLOT_RESTRICTIONS_FLAG);
	}
	@Override
	public boolean isNoSlotRestrictions() {
		return (field_12817 & NO_SLOT_RESTRICTIONS_FLAG) != 0;
	}
}
