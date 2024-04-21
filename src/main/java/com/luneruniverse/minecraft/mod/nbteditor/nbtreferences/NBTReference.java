package com.luneruniverse.minecraft.mod.nbteditor.nbtreferences;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalNBT;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public interface NBTReference<T extends LocalNBT> {
	public static CompletableFuture<? extends Optional<? extends NBTReference<?>>> getAnyReference(boolean airable) {
		HitResult target = MainUtil.client.crosshairTarget;
		if (target instanceof EntityHitResult entity) {
			return EntityReference.getEntity(entity.getEntity().getWorld().getRegistryKey(), entity.getEntity().getUuid())
					.thenApply(ref -> ref.isPresent() ? ref : getClientReference(airable));
		}
		if (target instanceof BlockHitResult block) {
			return BlockReference.getBlock(MainUtil.client.world.getRegistryKey(), block.getBlockPos())
					.thenApply(ref -> ref.isPresent() ? ref : getClientReference(airable));
		}
		return CompletableFuture.completedFuture(getClientReference(airable));
	}
	private static Optional<? extends NBTReference<?>> getClientReference(boolean airable) {
		try {
			return Optional.of(airable ? ItemReference.getHeldItemAirable() : ItemReference.getHeldItem());
		} catch (CommandSyntaxException e) {}
		
		if (NBTEditorClient.SERVER_CONN.isEditingExpanded()) {
			HitResult target = MainUtil.client.crosshairTarget;
			if (target instanceof BlockHitResult block && (block.getType() != HitResult.Type.MISS || airable))
				return Optional.of(BlockReference.getBlockWithoutNBT(block.getBlockPos()));
		}
		
		return Optional.empty();
	}
	
	public static void getAnyReference(boolean airable, Consumer<NBTReference<?>> consumer) {
		NBTReference.getAnyReference(airable).thenAccept(ref -> ref.ifPresentOrElse(consumer, () -> {
			if (MainUtil.client.player != null) {
				MainUtil.client.player.sendMessage(TextInst.translatable(NBTEditorClient.SERVER_CONN.isEditingExpanded() ?
						"nbteditor.no_ref.to_edit" : "nbteditor.no_hand.no_item.to_edit"), false);
			}
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
	
	public NbtCompound getNBT();
	public void saveNBT(Identifier id, NbtCompound toSave, Runnable onFinished);
	public default void saveNBT(Identifier id, NbtCompound toSave, Text msg) {
		saveNBT(id, toSave, () -> MainUtil.client.player.sendMessage(msg, false));
	}
	public default void saveNBT(Identifier id, NbtCompound toSave) {
		saveNBT(id, toSave, () -> {});
	}
	
	public void showParent();
	public default void escapeParent() {
		MainUtil.client.setScreen(null);
	}
	public default boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ESCAPE)
			escapeParent();
		else if (MainUtil.client.options.inventoryKey.matchesKey(keyCode, scanCode))
			showParent();
		else
			return false;
		return true;
	}
}
