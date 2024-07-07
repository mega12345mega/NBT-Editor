package com.luneruniverse.minecraft.mod.nbteditor.commands.get;

import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.argument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.commands.arguments.EffectListArgumentType;
import com.luneruniverse.minecraft.mod.nbteditor.commands.arguments.EnumArgumentType;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistry;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.ItemTagReferences;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.CustomPotionContents;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;

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
	
	@Override
	public String getExtremeAlias() {
		return "p";
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder, String path) {
		builder.then(argument("type", EnumArgumentType.options(PotionType.class)).then(argument("effects", EffectListArgumentType.effectList()).executes(context -> {
			ItemStack item = new ItemStack(context.getArgument("type", PotionType.class).item, 1);
			List<StatusEffectInstance> effects = new ArrayList<>(context.getArgument("effects", Collection.class));
			Optional<Integer> color = Optional.empty();
			if (!effects.isEmpty()) {
				StatusEffectInstance effect = effects.get(0);
				Potion potion = MVRegistry.POTION.getEntrySet().stream().map(Map.Entry::getValue)
						.filter(testPotion -> !testPotion.getEffects().isEmpty() &&
								testPotion.getEffects().get(0).getEffectType() == effect.getEffectType()).findFirst().orElse(null);
				if (potion != null)
					color = Optional.of(MVMisc.getEffectType(potion.getEffects().get(0)).getColor());
			}
			ItemTagReferences.CUSTOM_POTION_CONTENTS.set(item, new CustomPotionContents(color, effects));
			MainUtil.getWithMessage(item);
			return Command.SINGLE_SUCCESS;
		})));
	}
	
}
