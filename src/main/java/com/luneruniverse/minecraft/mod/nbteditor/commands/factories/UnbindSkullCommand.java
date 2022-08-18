package com.luneruniverse.minecraft.mod.nbteditor.commands.factories;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import com.luneruniverse.minecraft.mod.nbteditor.commands.SubCommand;
import com.luneruniverse.minecraft.mod.nbteditor.util.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;

public class UnbindSkullCommand implements SubCommand {
	
	@Override
	public LiteralArgumentBuilder<FabricClientCommandSource> register(LiteralArgumentBuilder<FabricClientCommandSource> parent, CommandRegistryAccess cmdReg) {
		return parent.then(literal("unbindskull").executes(context -> {
			ItemReference heldItem = MainUtil.getHeldItem(item -> item.getItem() == Items.PLAYER_HEAD, Text.translatable("nbteditor.noskull"));
			Hand hand = heldItem.getHand();
			ItemStack item = heldItem.getItem();
			
			NbtCompound nbt = item.getOrCreateNbt();
			if (nbt.contains("SkullOwner", NbtType.COMPOUND)) {
				NbtCompound owner = nbt.getCompound("SkullOwner");
				owner.putIntArray("Id", new int[] {0, 0, 0, 0});
				String name = owner.getString("Name");
				owner.putString("Name", "Unbound Player");
				if (!item.hasCustomName())
					item.setCustomName(Text.literal(name).formatted(Formatting.YELLOW));
			} else
				throw new SimpleCommandExceptionType(Text.translatable("nbteditor.skull_not_loaded")).create();
			
			MainUtil.saveItem(hand, item);
			context.getSource().sendFeedback(Text.translatable("nbteditor.skull_edited"));
			return Command.SINGLE_SUCCESS;
		}));
	}
	
}
