package com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data;

import java.util.List;
import java.util.Optional;

import net.minecraft.entity.effect.StatusEffectInstance;

public record CustomPotionContents(Optional<Integer> color, List<StatusEffectInstance> effects) {
	
}
