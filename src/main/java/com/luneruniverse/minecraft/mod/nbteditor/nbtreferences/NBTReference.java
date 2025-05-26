package com.luneruniverse.minecraft.mod.nbteditor.nbtreferences;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalNBT;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.HandItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public interface NBTReference<T extends LocalNBT> {
	public static CompletableFuture<? extends Optional<? extends NBTReference<?>>> getReference(NBTReferenceFilter filter, boolean airable) {
		HitResult target = MainUtil.client.crosshairTarget;
		if (target instanceof EntityHitResult entity && filter.isEntityAllowed()) {
			return EntityReference.getEntity(entity.getEntity().getEntityWorld().getRegistryKey(), entity.getEntity().getUuid())
					.thenApply(ref -> ref.<NBTReference<?>>map(UnaryOperator.identity())
							.filter(filter).or(() -> getClientReference(target, filter, airable)));
		}
		if (target instanceof BlockHitResult block && filter.isBlockAllowed()) {
			return BlockReference.getBlock(MainUtil.client.world.getRegistryKey(), block.getBlockPos())
					.thenApply(ref -> ref.<NBTReference<?>>map(UnaryOperator.identity())
							.filter(filter).or(() -> getClientReference(target, filter, airable)));
		}
		return CompletableFuture.completedFuture(getClientReference(target, filter, airable));
	}
	private static Optional<? extends NBTReference<?>> getClientReference(HitResult target, NBTReferenceFilter filter, boolean airable) {
		boolean heldItemDisallowed = false;
		if (filter.isItemAllowed()) {
			try {
				ItemReference ref = ItemReference.getHeldItem();
				if (filter.test(ref))
					return Optional.of(ref);
				heldItemDisallowed = true;
			} catch (CommandSyntaxException e) {}
		}
		
		if (filter.isBlockAllowed() && NBTEditorClient.SERVER_CONN.isEditingExpanded()) {
			if (target instanceof BlockHitResult block && block.getType() != HitResult.Type.MISS) {
				BlockReference ref = BlockReference.getBlockWithoutNBT(block.getBlockPos());
				if (filter.test(ref))
					return Optional.of(ref);
			}
		}
		
		if (airable && !heldItemDisallowed && filter.isItemAllowed())
			return Optional.of(new HandItemReference(Hand.MAIN_HAND));
		
		return Optional.empty();
	}
	
	public static void getReference(NBTReferenceFilter filter, boolean airable, Consumer<NBTReference<?>> consumer) {
		NBTReference.getReference(filter, airable).thenAccept(ref -> MainUtil.client.execute(() -> {
			ref.ifPresentOrElse(consumer, () -> {
				if (MainUtil.client.player != null)
					MainUtil.client.player.sendMessage(filter.getFailMessage(), false);
			});
		}));
	}
	
	T getLocalNBT();
	public default void saveLocalNBT(T nbt, Runnable onFinished) {
		saveNBT(nbt.getId(), nbt.getNBT(), onFinished);
	}
	public default void saveLocalNBT(T nbt, Text msg) {
		saveLocalNBT(nbt, () -> MainUtil.client.player.sendMessage(msg, false));
	}
	public default void saveLocalNBT(T nbt) {
		saveLocalNBT(nbt, () -> {});
	}
	public default void modifyLocalNBT(Consumer<T> nbtConsumer, Runnable onFinished) {
		T nbt = getLocalNBT();
		nbtConsumer.accept(nbt);
		saveLocalNBT(nbt, onFinished);
	}
	public default void modifyLocalNBT(Consumer<T> nbtConsumer, Text msg) {
		modifyLocalNBT(nbtConsumer, () -> MainUtil.client.player.sendMessage(msg, false));
	}
	public default void modifyLocalNBT(Consumer<T> nbtConsumer) {
		modifyLocalNBT(nbtConsumer, () -> {});
	}
	
	public Identifier getId();
	public NbtCompound getNBT();
	public void saveNBT(Identifier id, NbtCompound toSave, Runnable onFinished);
	public default void saveNBT(Identifier id, NbtCompound toSave, Text msg) {
		saveNBT(id, toSave, () -> MainUtil.client.player.sendMessage(msg, false));
	}
	public default void saveNBT(Identifier id, NbtCompound toSave) {
		saveNBT(id, toSave, () -> {});
	}
	
	public default void showParent(Optional<ItemStack> cursor) {
		escapeParent(cursor);
	}
	public default void escapeParent(Optional<ItemStack> cursor) {
		cursor.ifPresent(MainUtil::setInventoryCursorStack);
		MainUtil.client.player.currentScreenHandler = MainUtil.client.player.playerScreenHandler;
		MainUtil.client.setScreen(null);
	}
	public default void clearParentCursor() {}
}
