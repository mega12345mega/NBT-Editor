package com.luneruniverse.minecraft.mod.nbteditor.nbtreferences;

import java.util.function.Predicate;
import java.util.function.Supplier;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ItemReference;

import net.minecraft.text.Text;

public interface NBTReferenceFilter extends Predicate<NBTReference<?>> {
	public static final NBTReferenceFilter ANY = create(ref -> true, ref -> true, ref -> true,
			TextInst.translatable("nbteditor.no_ref.to_edit"), TextInst.translatable("nbteditor.no_hand.no_item.to_edit"));
	
	/**
	 * Pass in <code>null</code> to the filters to always reject that type<br>
	 * Avoid passing in <code>() -> false</code> as this is inefficient for blocks and entities
	 */
	public static NBTReferenceFilter create(
			Predicate<ItemReference> itemFilter,
			Predicate<BlockReference> blockFilter,
			Predicate<EntityReference> entityFilter,
			Supplier<Text> failMsg) {
		return new NBTReferenceFilter() {
			@Override
			public boolean test(NBTReference<?> ref) {
				if (ref instanceof ItemReference item)
					return itemFilter != null && itemFilter.test(item);
				if (ref instanceof BlockReference block)
					return blockFilter != null && blockFilter.test(block);
				if (ref instanceof EntityReference entity)
					return entityFilter != null && entityFilter.test(entity);
				return false;
			}
			@Override
			public Text getFailMessage() {
				return failMsg.get();
			}
			@Override
			public boolean isItemAllowed() {
				return itemFilter != null;
			}
			@Override
			public boolean isBlockAllowed() {
				return blockFilter != null;
			}
			@Override
			public boolean isEntityAllowed() {
				return entityFilter != null;
			}
		};
	}
	/**
	 * Pass in <code>null</code> to the filters to always reject that type<br>
	 * Avoid passing in <code>() -> false</code> as this is inefficient for blocks and entities
	 */
	public static NBTReferenceFilter create(
			Predicate<ItemReference> itemFilter,
			Predicate<BlockReference> blockFilter,
			Predicate<EntityReference> entityFilter,
			Text expandedFailMsg,
			Text nonExpandedFailMsg) {
		return create(itemFilter, blockFilter, entityFilter,
				() -> NBTEditorClient.SERVER_CONN.isEditingExpanded() ? expandedFailMsg : nonExpandedFailMsg);
	}
	
	public Text getFailMessage();
	
	// Use to avoid requesting block or entity data when it will always be rejected
	public default boolean isItemAllowed() {
		return true;
	}
	public default boolean isBlockAllowed() {
		return true;
	}
	public default boolean isEntityAllowed() {
		return true;
	}
}
