package com.luneruniverse.minecraft.mod.nbteditor.commands;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;

public class UnbindSkullCommand implements ClientCommand {
	
	@Override
	public LiteralCommandNode<? extends CommandSource> register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess cmdReg) {
		return dispatcher.register(literal("unbindskull").executes(context -> {
			ClientPlayerEntity player = context.getSource().getPlayer();
			Map.Entry<Hand, ItemStack> heldItem = MainUtil.getHeldItem(player, item -> item.getItem() == Items.PLAYER_HEAD, Text.translatable("nbteditor.noskull"));
			Hand hand = heldItem.getKey();
			ItemStack item = heldItem.getValue();
			
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
	
	@Override
	public List<String> getAliases() {
		return Arrays.asList();
	}
	
}
