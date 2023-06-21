package com.luneruniverse.minecraft.mod.nbteditor.commands.get;

import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.argument;
import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.literal;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.commands.arguments.EnumArgumentType;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.util.ItemChest;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Formatting;
import tsp.headdb.ported.Category;
import tsp.headdb.ported.Head;
import tsp.headdb.ported.HeadAPI;
import tsp.headdb.ported.inventory.InventoryUtils;

public class GetHdbCommand extends ClientCommand {
	
	@Override
	public String getName() {
		return "hdb";
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
		builder.then(literal("search").then(argument("query", StringArgumentType.greedyString()).executes(context -> {
					HeadAPI.openSearchDatabase(context.getArgument("query", String.class));
					return Command.SINGLE_SUCCESS;
				})))
				.then(literal("tagsearch").then(argument("query", StringArgumentType.greedyString()).executes(context -> {
					HeadAPI.openTagSearchDatabase(context.getArgument("query", String.class));
					return Command.SINGLE_SUCCESS;
				})))
				.then(literal("id").then(argument("id", IntegerArgumentType.integer(1))
						.then(argument("amount", IntegerArgumentType.integer(1)).executes(context -> {
							Head head = HeadAPI.getHeadByID(context.getArgument("id", Integer.class));
							if (head == null)
								context.getSource().sendFeedback(TextInst.translatable("nbteditor.hdb.head_not_found"));
							else
								InventoryUtils.purchaseHead(head, context.getArgument("amount", Integer.class), "", "");
							return Command.SINGLE_SUCCESS;
						}))
					.executes(context -> {
						Head head = HeadAPI.getHeadByID(context.getArgument("id", Integer.class));
						if (head == null)
							context.getSource().sendFeedback(TextInst.translatable("nbteditor.hdb.head_not_found"));
						else
							InventoryUtils.purchaseHead(head, 1, "", "");
						return Command.SINGLE_SUCCESS;
					})))
				.then(literal("all").then(argument("category", EnumArgumentType.options(Category.class)).executes(context -> {
					Category category = context.getArgument("category", Category.class);
					ItemStack shulker = ShulkerBoxBlock.getItemStack(MainUtil.getDyeColor(category.getColor()));
					shulker.setCustomName(TextInst.of(Formatting.RESET.toString() + category.getColor() + Formatting.BOLD + category.getTranslatedName().toUpperCase()));
					shulker.getOrCreateNbt().putByte("HideFlags", (byte) 32);
					ItemChest.writeDatabase(shulker, HeadAPI.getHeads(category), Head::getItemStack);
					MainUtil.getWithMessage(shulker);
					return Command.SINGLE_SUCCESS;
				})).then(literal("search").then(argument("query", StringArgumentType.greedyString()).executes(context -> {
					String query = context.getArgument("query", String.class);
					ItemStack shulker = new ItemStack(Items.BROWN_SHULKER_BOX);
					shulker.setCustomName(TextInst.of(Formatting.RESET.toString() + Formatting.GOLD + Formatting.BOLD + TextInst.translatable("nbteditor.hdb.search").getString() + ": " + query));
					shulker.getOrCreateNbt().putByte("HideFlags", (byte) 32);
					ItemChest.writeDatabase(shulker, HeadAPI.getHeadsByName(query), Head::getItemStack);
					MainUtil.getWithMessage(shulker);
					return Command.SINGLE_SUCCESS;
				}))))
				.then(literal("update").executes(context -> {
					context.getSource().sendFeedback(TextInst.translatable("nbteditor.hdb.updating_database"));
					Thread thread = new Thread(() -> {
						HeadAPI.updateDatabase();
						context.getSource().sendFeedback(TextInst.translatable("nbteditor.hdb.updated_database"));
					}, "NBTEditor/Async/HeadRefresh/Manual");
					thread.setDaemon(true);
					thread.start();
					return Command.SINGLE_SUCCESS;
				}))
				.executes(context -> {
					if (HeadAPI.checkUpdated())
						HeadAPI.openDatabase();
					return Command.SINGLE_SUCCESS;
				});
	}
	
}
