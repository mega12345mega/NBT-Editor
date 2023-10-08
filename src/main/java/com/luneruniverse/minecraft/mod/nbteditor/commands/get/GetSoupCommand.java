package com.luneruniverse.minecraft.mod.nbteditor.commands.get;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.StatusEffectArgumentType;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.block.SuspiciousStewIngredient;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SuspiciousStewItem;

import java.util.List;

import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.argument;

public class GetSoupCommand extends ClientCommand {

    @Override
    public String getName() {
        return "soup";
    }

    @Override
    public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
        Command<FabricClientCommandSource> getSoup = context -> {
            int duration = getDefaultArg(context, "duration", 5, Integer.class);

            ItemStack item = new ItemStack(Items.SUSPICIOUS_STEW, 1);
            SuspiciousStewIngredient.StewEffect stewEffect = new SuspiciousStewIngredient.StewEffect(context.getArgument("effect", StatusEffect.class), duration * 20);
            SuspiciousStewItem.writeEffectsToStew(item, List.of(stewEffect));
            MainUtil.getWithMessage(item);
            return Command.SINGLE_SUCCESS;
        };

        builder.then(argument("effect", StatusEffectArgumentType.statusEffect()).then(argument("duration", IntegerArgumentType.integer(0)).executes(getSoup)).executes(getSoup));
    }

}
