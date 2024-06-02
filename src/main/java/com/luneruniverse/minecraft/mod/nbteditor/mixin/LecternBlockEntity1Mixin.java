package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LecternBlockEntity;

@Mixin(targets = "net.minecraft.block.entity.LecternBlockEntity$1")
public class LecternBlockEntity1Mixin {
	@ModifyArg(method = "canPlayerUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Inventory;canPlayerUse(Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/entity/player/PlayerEntity;)Z"))
	private BlockEntity canPlayerUse(BlockEntity entity) {
		if (MixinLink.getLecternRequests.containsKey(Thread.currentThread()))
			MixinLink.getLecternRequests.put(Thread.currentThread(), (LecternBlockEntity) entity);
		return entity;
	}
}
