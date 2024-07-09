package com.luneruniverse.minecraft.mod.nbteditor.multiversion.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin {
	// <= 1.17.1: patches item getting thrown & deleted when creative inventory is closed
	@Inject(method = "onClosed", at = @At("HEAD"), cancellable = true)
	private void close(PlayerEntity player, CallbackInfo info) {
		Version.newSwitch()
				.range("1.18.0", null, () -> {})
				.range(null, "1.17.1", () -> {
					if (!(player instanceof ServerPlayerEntity))
						info.cancel();
				})
				.run();
	}
}
