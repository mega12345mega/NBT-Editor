package com.luneruniverse.minecraft.mod.nbteditor.mixin.toggled;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.luneruniverse.minecraft.mod.nbteditor.server.ServerMixinLink;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

@Mixin(WorldChunk.class)
public class WorldChunkMixin {
	
	@WrapWithCondition(method = "method_12010(Lnet/minecraft/class_2338;Lnet/minecraft/class_2680;Z)Lnet/minecraft/class_2680;", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_2680;method_26197(Lnet/minecraft/class_1937;Lnet/minecraft/class_2338;Lnet/minecraft/class_2680;Z)V"), remap = false)
	@SuppressWarnings("target")
	private boolean setBlockState$BlockState_onStateReplaced(BlockState obj, World world, BlockPos pos, BlockState state, boolean moved) {
		boolean skip = ServerMixinLink.doesSetBlockStateHaveFlag(Block.SKIP_BLOCK_ENTITY_REPLACED_CALLBACK);
		if (skip) {
			// In 1.21.4-, onStateReplaced handled removing block entities
			if (obj.hasBlockEntity() && !obj.isOf(state.getBlock()))
				world.removeBlockEntity(pos);
		}
		return !skip;
	}
	
	@WrapWithCondition(method = "method_12010(Lnet/minecraft/class_2338;Lnet/minecraft/class_2680;Z)Lnet/minecraft/class_2680;", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_2680;method_26182(Lnet/minecraft/class_1937;Lnet/minecraft/class_2338;Lnet/minecraft/class_2680;Z)V"), remap = false)
	@SuppressWarnings("target")
	private boolean setBlockState$BlockState_onBlockAdded(BlockState obj, World world, BlockPos pos, BlockState state, boolean notify) {
		return !ServerMixinLink.doesSetBlockStateHaveFlag(Block.SKIP_BLOCK_ADDED_CALLBACK);
	}
	
}
