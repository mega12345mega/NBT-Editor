package com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific;

import java.util.ArrayList;
import java.util.stream.Collectors;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDataComponentType;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistry;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general.ComponentTagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general.NBTTagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general.TagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.Enchants;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class EnchantsTagReference implements TagReference<Enchants, ItemStack> {
	
	private static TagReference<Enchants, ItemStack> getEnchantsTagRef(String tag, MVDataComponentType<ItemEnchantmentsComponent> component) {
		return Version.<TagReference<Enchants, ItemStack>>newSwitch()
				.range("1.20.5", null, () -> new ComponentTagReference<>(component,
						null,
						componentValue -> componentValue == null ? new Enchants() : new Enchants(componentValue.getEnchantmentsMap().stream()
								.map(entry -> new Enchants.EnchantWithLevel(entry.getKey().value(), entry.getIntValue())).collect(Collectors.toList())),
								(componentValue, enchants) -> new ItemEnchantmentsComponent(new Object2IntOpenHashMap<>(
										enchants.getEnchants().stream().collect(Collectors.toMap(
												enchant -> Registries.ENCHANTMENT.getEntry(enchant.enchant()),
												enchant -> Math.min(255, enchant.level()),
												Math::max))),
										componentValue.showInTooltip)))
				.range(null, "1.20.4", () -> TagReference.mapValue(Enchants::new, Enchants::getEnchants,
						TagReference.forItems(ArrayList::new, TagReference.forLists(element -> {
							if (!(element instanceof NbtCompound compound))
								return null;
							if (!compound.contains("id", NbtElement.STRING_TYPE))
								return null;
							Enchantment enchant = MVRegistry.ENCHANTMENT.get(new Identifier(compound.getString("id")));
							if (enchant == null)
								return null;
							int level = compound.getShort("lvl");
							if (level < 1)
								return null;
							return new Enchants.EnchantWithLevel(enchant, level);
						}, enchant -> {
							NbtCompound output = new NbtCompound();
							output.putString("id", MVRegistry.ENCHANTMENT.getId(enchant.enchant()).toString());
							output.putShort("lvl", (short) enchant.level());
							return output;
						}, new NBTTagReference<>(NbtList.class, tag)))))
				.get();
	}
	
	private static final TagReference<Enchants, ItemStack> ENCHANTMENTS = getEnchantsTagRef("Enchantments", MVDataComponentType.ENCHANTMENTS);
	private static final TagReference<Enchants, ItemStack> STORED_ENCHANTMENTS = getEnchantsTagRef("StoredEnchantments", MVDataComponentType.STORED_ENCHANTMENTS);
	
	public EnchantsTagReference() {
		
	}
	
	@Override
	public Enchants get(ItemStack object) {
		if (object.isOf(Items.ENCHANTED_BOOK))
			return STORED_ENCHANTMENTS.get(object);
		return ENCHANTMENTS.get(object);
	}
	
	@Override
	public void set(ItemStack object, Enchants value) {
		if (object.isOf(Items.ENCHANTED_BOOK))
			STORED_ENCHANTMENTS.set(object, value);
		else
			ENCHANTMENTS.set(object, value);
	}
	
}
