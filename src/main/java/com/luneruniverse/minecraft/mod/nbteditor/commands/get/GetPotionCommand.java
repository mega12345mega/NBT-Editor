package com.luneruniverse.minecraft.mod.nbteditor.commands.get;

import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.argument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.commands.arguments.EffectListArgumentType;
import com.luneruniverse.minecraft.mod.nbteditor.commands.arguments.EnumArgumentType;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionRegistry;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;

public class GetPotionCommand extends ClientCommand {
	
	public enum PotionType {
		NORMAL(Items.POTION),
		SPLASH(Items.SPLASH_POTION),
		LINGERING(Items.LINGERING_POTION);
		
		private final Item item;
		
		private PotionType(Item item) {
			this.item = item;
		}
	}
	
	@Override
	public String getName() {
		return "potion";
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
		builder.then(argument("type", EnumArgumentType.options(PotionType.class)).then(argument("effects", EffectListArgumentType.effectList()).executes(context -> {
			ItemStack item = new ItemStack(context.getArgument("type", PotionType.class).item, 1);
			List<StatusEffectInstance> effects = new ArrayList<>(context.getArgument("effects", Collection.class));
			if (!effects.isEmpty()) {
				StatusEffectInstance effect = effects.get(0);
				List<Potion> potions = new ArrayList<>();
				MultiVersionRegistry.POTION.forEach(potions::add);
				Potion potion = potions.stream().filter(testPotion -> !testPotion.getEffects().isEmpty() && testPotion.getEffects().get(0).getEffectType() == effect.getEffectType()).findFirst().orElse(null);
				if (potion != null) {
					int color = potion.getEffects().get(0).getEffectType().getColor();
					NbtCompound nbt = item.getOrCreateNbt();
					nbt.putInt("CustomPotionColor", color);
				}
			}
			PotionUtil.setCustomPotionEffects(item, effects);
			MainUtil.getWithMessage(item);
			return Command.SINGLE_SUCCESS;
		})));
	}
	
}
