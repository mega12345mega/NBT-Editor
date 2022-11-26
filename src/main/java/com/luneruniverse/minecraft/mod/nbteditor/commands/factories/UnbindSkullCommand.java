package com.luneruniverse.minecraft.mod.nbteditor.commands.factories;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.util.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;

public class UnbindSkullCommand extends ClientCommand {
	
	@Override
	public String getName() {
		return "unbindskull";
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
		builder.executes(context -> {
			ItemReference heldItem = MainUtil.getHeldItem(item -> item.getItem() == Items.PLAYER_HEAD, TextInst.translatable("nbteditor.no_hand.no_item.skull"));
			Hand hand = heldItem.getHand();
			ItemStack item = heldItem.getItem();
			
			NbtCompound nbt = item.getOrCreateNbt();
			if (nbt.contains("SkullOwner", NbtType.COMPOUND)) {
				NbtCompound owner = nbt.getCompound("SkullOwner");
				owner.putIntArray("Id", new int[] {0, 0, 0, 0});
				String name = owner.getString("Name");
				owner.putString("Name", "Unbound Player");
				if (!item.hasCustomName())
					item.setCustomName(TextInst.literal(name).formatted(Formatting.YELLOW));
			} else
				throw new SimpleCommandExceptionType(TextInst.translatable("nbteditor.unbind_skull.no_textures")).create();
			
			MainUtil.saveItem(hand, item);
			context.getSource().sendFeedback(TextInst.translatable("nbteditor.unbind_skull.unbound"));
			return Command.SINGLE_SUCCESS;
		});
	}
	
}
