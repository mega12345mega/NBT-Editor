package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.luneruniverse.minecraft.mod.nbteditor.misc.ParallelResourceReload;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.DynamicRegistryManagerHolder;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;

import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.resource.ResourceReload;

@Mixin(SplashOverlay.class)
public class SplashOverlayMixin {
	@ModifyVariable(method = "<init>", at = @At("HEAD"), ordinal = 0)
	private static ResourceReload init_monitor(ResourceReload monitor) {
		return Version.<ResourceReload>newSwitch()
				.range("1.20.5", null, () -> new ParallelResourceReload(monitor, DynamicRegistryManagerHolder.loadDefaultManager()))
				.range(null, "1.20.4", monitor)
				.get();
	}
}
