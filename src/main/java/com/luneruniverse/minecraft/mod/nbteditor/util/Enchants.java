package com.luneruniverse.minecraft.mod.nbteditor.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistry;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;

public class Enchants {
	
	public static record EnchantWithLevel(Enchantment enchant, int level) {}
	
	private static String getEnchantKey(ItemStack item) {
		return item.isOf(Items.ENCHANTED_BOOK) ? "StoredEnchantments" : "Enchantments";
	}
	
	private final ItemStack item;
	private final NbtList enchants;
	
	public Enchants(ItemStack item) {
		this.item = item;
		this.enchants = new NbtList();
		if (item.hasNbt())
			enchants.addAll(item.getNbt().getList(getEnchantKey(item), NbtElement.COMPOUND_TYPE));
	}
	
	private void save() {
		if (enchants.isEmpty())
			item.getOrCreateNbt().remove(getEnchantKey(item));
		else
			item.getOrCreateNbt().put(getEnchantKey(item), enchants.copy());
	}
	
	public int size() {
		return enchants.size();
	}
	public boolean isEmpty() {
		return size() == 0;
	}
	
	public List<EnchantWithLevel> getEnchants() {
		return enchants.stream()
				.filter(nbt -> nbt instanceof NbtCompound)
				.map(nbt -> (NbtCompound) nbt)
				.map(nbt -> new EnchantWithLevel(MVRegistry.ENCHANTMENT.getOrEmpty(
						Identifier.tryParse(nbt.getString("id"))).orElse(null), nbt.getInt("lvl")))
				.filter(enchant -> enchant.enchant() != null)
				.collect(Collectors.toList());
	}
	public int getLevel(Enchantment enchant) {
		return getEnchants().stream().filter(test -> test.enchant() == enchant)
				.map(EnchantWithLevel::level).reduce(0, (a, b) -> a + b);
	}
	
	public void addEnchant(Enchantment enchant, int level) {
		NbtCompound nbt = new NbtCompound();
		nbt.putString("id", MVRegistry.ENCHANTMENT.getId(enchant).toString());
		nbt.putShort("lvl", (short) level);
		enchants.add(nbt);
		save();
	}
	public void addEnchant(EnchantWithLevel enchant) {
		addEnchant(enchant.enchant(), enchant.level());
	}
	public void addEnchants(Collection<EnchantWithLevel> enchants) {
		for (EnchantWithLevel enchant : enchants)
			addEnchant(enchant);
	}
	
	public boolean removeEnchant(Enchantment enchant) {
		String key = MVRegistry.ENCHANTMENT.getId(enchant).toString();
		boolean output = enchants.removeIf(test -> !(test instanceof NbtCompound nbt) || nbt.getString("id").equals(key));
		save();
		return output;
	}
	public boolean removeEnchants(Collection<Enchantment> enchants) {
		boolean output = false;
		for (Enchantment enchant : enchants)
			output |= removeEnchant(enchant);
		return output;
	}
	
	public boolean removeDuplicates() {
		Map<Enchantment, Integer> foundEnchants = new HashMap<>();
		boolean anyRemoved = false;
		for (Iterator<NbtElement> i = enchants.iterator(); i.hasNext();) {
			if (i.next() instanceof NbtCompound nbt) {
				Optional<Enchantment> enchant = MVRegistry.ENCHANTMENT.getOrEmpty(Identifier.tryParse(nbt.getString("id")));
				if (enchant.isPresent()) {
					int level = nbt.getInt("lvl");
					Integer maxLevel = foundEnchants.get(enchant.get());
					foundEnchants.put(enchant.get(), Math.max(level, maxLevel == null ? 0 : maxLevel));
					if (maxLevel != null) {
						i.remove();
						anyRemoved = true;
					}
				}
			}
		}
		if (anyRemoved) {
			for (NbtElement element : enchants) {
				if (element instanceof NbtCompound nbt) {
					Optional<Enchantment> enchant = MVRegistry.ENCHANTMENT.getOrEmpty(Identifier.tryParse(nbt.getString("id")));
					if (enchant.isPresent())
						nbt.putShort("lvl", (short) (int) foundEnchants.get(enchant.get()));
				}
			}
		}
		save();
		return anyRemoved;
	}
	
	public void setEnchant(Enchantment enchant, int level, boolean onlyUpgrade) {
		String key = MVRegistry.ENCHANTMENT.getId(enchant).toString();
		boolean anySet = false;
		boolean anyFound = false;
		for (NbtElement element : enchants) {
			if (element instanceof NbtCompound nbt && nbt.getString("id").equals(key)) {
				if (!onlyUpgrade || nbt.getInt("lvl") < level) {
					nbt.putShort("lvl", (short) level);
					anySet = true;
				}
				anyFound = true;
			}
		}
		if (anySet)
			save();
		else if (!anyFound)
			addEnchant(enchant, level);
	}
	public void setEnchant(EnchantWithLevel enchant, boolean onlyUpgrade) {
		setEnchant(enchant.enchant(), enchant.level(), onlyUpgrade);
	}
	public void setEnchants(Collection<EnchantWithLevel> enchants, boolean onlyUpgrade) {
		for (EnchantWithLevel enchant : enchants)
			setEnchant(enchant, onlyUpgrade);
	}
	
	public void replaceEnchants(Collection<EnchantWithLevel> enchants) {
		this.enchants.clear();
		if (enchants.isEmpty())
			save();
		else
			addEnchants(enchants);
	}
	public void clearEnchants() {
		enchants.clear();
		save();
	}
	
}
