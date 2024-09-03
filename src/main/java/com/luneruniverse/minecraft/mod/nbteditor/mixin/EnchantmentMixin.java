package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;

@Mixin(Enchantment.class)
public class EnchantmentMixin {
	@Inject(method = "getName(Lnet/minecraft/registry/entry/RegistryEntry;I)Lnet/minecraft/text/Text;", at = @At("HEAD"), cancellable = true)
	@Group(name = "getName", min = 1)
	private static void getName(RegistryEntry<Enchantment> enchantment, int level, CallbackInfoReturnable<Text> info) {
		info.setReturnValue(ConfigScreen.getEnchantNameWithMax(enchantment.value(), level));
	}
	@Inject(method = "method_8179(I)Lnet/minecraft/class_2561;", at = @At("HEAD"), cancellable = true)
	@Group(name = "getName", min = 1)
	@SuppressWarnings("target")
	private void getName_old(int level, CallbackInfoReturnable<Text> info) {
		info.setReturnValue(ConfigScreen.getEnchantNameWithMax((Enchantment) (Object) this, level));
	}
}
