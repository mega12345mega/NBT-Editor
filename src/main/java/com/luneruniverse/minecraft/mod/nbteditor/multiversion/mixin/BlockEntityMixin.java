package com.luneruniverse.minecraft.mod.nbteditor.multiversion.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.server.ServerMixinLink;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;

@Mixin(BlockEntity.class)
public class BlockEntityMixin {
	@Inject(method = "method_11007(Lnet/minecraft/class_2487;)Lnet/minecraft/class_2487;", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
	@SuppressWarnings("target")
	private void writeNbt(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> info) {
		if (ServerMixinLink.BLOCK_ENTITY_WRITE_NBT_WITHOUT_IDENTIFYING_DATA.remove(Thread.currentThread()))
			info.setReturnValue(nbt);
	}
}
