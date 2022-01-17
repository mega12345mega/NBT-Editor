package com.luneruniverse.minecraft.mod.nbteditor.commands;

import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.literal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.luneruniverse.minecraft.mod.nbteditor.InternalItems;
import com.luneruniverse.minecraft.mod.nbteditor.commands.arguments.EffectListArgumentType;
import com.luneruniverse.minecraft.mod.nbteditor.commands.arguments.EnumArgumentType;
import com.luneruniverse.minecraft.mod.nbteditor.util.ItemChest;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.command.argument.StatusEffectArgumentType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SuspiciousStewItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.registry.Registry;
import tsp.headdb.ported.Category;
import tsp.headdb.ported.Head;
import tsp.headdb.ported.HeadAPI;
import tsp.headdb.ported.inventory.InventoryUtils;

public class GetCommand implements RegisterableCommand {
	
	public static void get(ItemStack item, boolean dropIfNoSpace) {
		MinecraftClient client = MinecraftClient.getInstance();
		PlayerInventory inv = client.player.getInventory();
		item = item.copy();
		
		int slot = inv.getOccupiedSlotWithRoomForStack(item);
		if (slot == -1)
			slot = inv.getEmptySlot();
		if (slot == -1) {
			if (dropIfNoSpace) {
				if (item.getCount() > item.getMaxCount())
					item.setCount(item.getMaxCount());
				client.interactionManager.dropCreativeStack(item);
			}
		} else {
			item.setCount(item.getCount() + inv.getStack(slot).getCount());
			int overflow = 0;
			if (item.getCount() > item.getMaxCount()) {
				overflow = item.getCount() - item.getMaxCount();
				item.setCount(item.getMaxCount());
			}
			MainUtil.saveItem(slot, item);
			if (overflow != 0) {
				item.setCount(overflow);
				get(item, false);
			}
		}
	}
	@SuppressWarnings("resource")
	public static void getWithMessage(ItemStack item) {
		get(item, true);
		MinecraftClient.getInstance().player.sendMessage(new TranslatableText("nbteditor.get.item").append(item.toHoverableText()), false);
	}
	
	
	public enum PotionType {
		NORMAL(Items.POTION),
		SPLASH(Items.SPLASH_POTION),
		LINGERING(Items.LINGERING_POTION);
		
		private final Item item;
		
		private PotionType(Item item) {
			this.item = item;
		}
	}
	public enum HelpType {
		NBTEDITOR("nbteditor.help.nbteditor"),
		CLIENTCHEST("nbteditor.help.clientchest");
		
		private final String msgKey;
		
		private HelpType(String msgKey) {
			this.msgKey = msgKey;
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public LiteralCommandNode<FabricClientCommandSource> register(boolean dedicated) {
		Command<FabricClientCommandSource> getItem = context -> {
			int count = 1;
			try {
				count = context.getArgument("count", Integer.class);
			} catch (IllegalArgumentException e) {}
			
			ItemStack item = context.getArgument("item", ItemStackArgument.class).createStack(count, false);
			
			getWithMessage(item);
			return Command.SINGLE_SUCCESS;
		};
		
		Command<FabricClientCommandSource> getSoup = context -> {
			int duration = 5;
			try {
				duration = context.getArgument("duration", Integer.class);
			} catch (IllegalArgumentException e) {}
			
			ItemStack item = new ItemStack(Items.SUSPICIOUS_STEW, 1);
			SuspiciousStewItem.addEffectToStew(item, context.getArgument("effect", StatusEffect.class), duration * 20);
			getWithMessage(item);
			return Command.SINGLE_SUCCESS;
		};
		
		return ClientCommandManager.DISPATCHER.register(literal("get")
				.then(literal("item").then(argument("item", ItemStackArgumentType.itemStack()).then(argument("count", IntegerArgumentType.integer(1)).executes(getItem)).executes(getItem)))
				.then(literal("potion").then(argument("type", EnumArgumentType.options(PotionType.class)).then(argument("effects", EffectListArgumentType.effectList()).executes(context -> {
					ItemStack item = new ItemStack(context.getArgument("type", PotionType.class).item, 1);
					List<StatusEffectInstance> effects = new ArrayList<>(context.getArgument("effects", Collection.class));
					if (!effects.isEmpty()) {
						StatusEffectInstance effect = effects.get(0);
						List<Potion> potions = new ArrayList<>();
						Registry.POTION.forEach(potions::add);
						Potion potion = potions.stream().filter(testPotion -> !testPotion.getEffects().isEmpty() && testPotion.getEffects().get(0).getEffectType() == effect.getEffectType()).findFirst().orElse(null);
						if (potion != null) {
							int color = potion.getEffects().get(0).getEffectType().getColor();
							NbtCompound nbt = item.getOrCreateNbt();
							nbt.putInt("CustomPotionColor", color);
						}
					}
					PotionUtil.setCustomPotionEffects(item, effects);
					getWithMessage(item);
					return Command.SINGLE_SUCCESS;
				}))))
				.then(literal("soup").then(argument("effect", StatusEffectArgumentType.statusEffect()).then(argument("duration", IntegerArgumentType.integer(0)).executes(getSoup)).executes(getSoup)))
				.then(literal("skull").then(argument("player", StringArgumentType.word()).executes(context -> {
					ItemStack item = new ItemStack(Items.PLAYER_HEAD, 1);
					NbtCompound nbt = item.getOrCreateNbt();
					nbt.putString("SkullOwner", context.getArgument("player", String.class));
					getWithMessage(item);
					return Command.SINGLE_SUCCESS;
				})))
				.then(literal("hdb")
						.then(literal("search").then(argument("query", StringArgumentType.greedyString()).executes(context -> {
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
										context.getSource().sendFeedback(Text.of(Formatting.RED + "Head not found!"));
									else
										InventoryUtils.purchaseHead(head, context.getArgument("amount", Integer.class), "", "");
									return Command.SINGLE_SUCCESS;
								}))
						.executes(context -> {
							Head head = HeadAPI.getHeadByID(context.getArgument("id", Integer.class));
							if (head == null)
								context.getSource().sendFeedback(Text.of(Formatting.RED + "Head not found!"));
							else
								InventoryUtils.purchaseHead(head, 1, "", "");
							return Command.SINGLE_SUCCESS;
						})))
						.then(literal("all").then(argument("category", EnumArgumentType.options(Category.class)).executes(context -> {
							Category category = context.getArgument("category", Category.class);
							ItemStack shulker = ShulkerBoxBlock.getItemStack(MainUtil.getDyeColor(category.getColor()));
							shulker.setCustomName(Text.of(Formatting.RESET.toString() + category.getColor() + Formatting.BOLD + category.getName().toUpperCase()));
							shulker.getOrCreateNbt().putByte("HideFlags", (byte) 32);
							ItemChest.writeDatabase(shulker, HeadAPI.getHeads(category), Head::getItemStack);
							getWithMessage(shulker);
							return Command.SINGLE_SUCCESS;
						})).then(literal("search").then(argument("query", StringArgumentType.greedyString()).executes(context -> {
							String query = context.getArgument("query", String.class);
							ItemStack shulker = new ItemStack(Items.BROWN_SHULKER_BOX);
							shulker.setCustomName(Text.of(Formatting.RESET.toString() + Formatting.GOLD + Formatting.BOLD + "Search: " + query));
							shulker.getOrCreateNbt().putByte("HideFlags", (byte) 32);
							ItemChest.writeDatabase(shulker, HeadAPI.getHeadsByName(query), Head::getItemStack);
							getWithMessage(shulker);
							return Command.SINGLE_SUCCESS;
						}))))
						.then(literal("update").executes(context -> {
							context.getSource().sendFeedback(Text.of(Formatting.GOLD + "Updating database ..."));
							new Thread(() -> {
								HeadAPI.updateDatabase();
								context.getSource().sendFeedback(Text.of(Formatting.GREEN + "Database updated!"));
							}, "Manual Head Refresh").start();
							return Command.SINGLE_SUCCESS;
						}))
				.executes(context -> {
					if (HeadAPI.checkUpdated())
						HeadAPI.openDatabase();
					return Command.SINGLE_SUCCESS;
				}))
				.then(literal("colorcodes").executes(context -> {
					getWithMessage(InternalItems.COLOR_CODES.copy());
					return Command.SINGLE_SUCCESS;
				}))
				.then(literal("help").then(argument("feature", EnumArgumentType.options(HelpType.class)).executes(context -> {
					context.getSource().sendFeedback(MainUtil.getLongTranslatableText(context.getArgument("feature", HelpType.class).msgKey));
					return Command.SINGLE_SUCCESS;
				})).executes(context -> {
					context.getSource().sendFeedback(MainUtil.getLongTranslatableText("nbteditor.help"));
					return Command.SINGLE_SUCCESS;
				}))
				.then(literal("credits").executes(context -> {
					TranslatableText credits = new TranslatableText("nbteditor.credits_1");
					for (int i = 2; i <= 6; i++) {
						final int finalI = i;
						credits.append("\n").append(new TranslatableText("nbteditor.credits_" + i).styled(style -> {
							if (finalI == 5)
								return style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/free-headdb-head-menu.84967/"));
							return style;
						}));
					}
					context.getSource().sendFeedback(credits);
					return Command.SINGLE_SUCCESS;
				}))
			);
	}
	
	@Override
	public List<String> getAliases() {
		return Arrays.asList();
	}
	
	@Override
	public EnvType getSide() {
		return EnvType.CLIENT;
	}
	
}
