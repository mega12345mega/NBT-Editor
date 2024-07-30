package com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import net.minecraft.enchantment.Enchantment;

public class Enchants {
	
	public static record EnchantWithLevel(Enchantment enchant, int level) {}
	
	private final List<EnchantWithLevel> enchants;
	
	public Enchants(List<EnchantWithLevel> enchants) {
		this.enchants = enchants;
	}
	public Enchants() {
		this.enchants = new ArrayList<>();
	}
	
	public int size() {
		return enchants.size();
	}
	public boolean isEmpty() {
		return size() == 0;
	}
	
	public List<EnchantWithLevel> getEnchants() {
		return enchants;
	}
	public int getLevel(Enchantment enchant) {
		return enchants.stream().filter(test -> test.enchant() == enchant)
				.mapToInt(EnchantWithLevel::level).max().orElse(0);
	}
	
	public void addEnchant(Enchantment enchant, int level) {
		enchants.add(new EnchantWithLevel(enchant, level));
	}
	public void addEnchant(EnchantWithLevel enchant) {
		enchants.add(enchant);
	}
	public void addEnchants(Collection<EnchantWithLevel> enchants) {
		this.enchants.addAll(enchants);
	}
	
	public boolean removeEnchant(Enchantment enchant) {
		return enchants.removeIf(enchantWithLevel -> enchantWithLevel.enchant() == enchant);
	}
	public boolean removeEnchants(Collection<Enchantment> enchants) {
		boolean output = false;
		for (Enchantment enchant : enchants)
			output |= removeEnchant(enchant);
		return output;
	}
	
	public boolean removeDuplicates() {
		Map<Enchantment, Integer> enchants = new LinkedHashMap<>();
		for (EnchantWithLevel enchant : this.enchants)
			enchants.put(enchant.enchant(), enchant.level());
		if (this.enchants.size() == enchants.size())
			return false;
		this.enchants.clear();
		enchants.forEach(this::addEnchant);
		return true;
	}
	
	public boolean setEnchant(Enchantment enchant, int level, boolean onlyUpgrade) {
		if (onlyUpgrade && level <= getLevel(enchant)) {
			return false;
		}
		
		boolean found = false;
		for (ListIterator<EnchantWithLevel> iter = enchants.listIterator(); iter.hasNext();) {
			EnchantWithLevel enchantWithLevel = iter.next();
			if (enchantWithLevel.enchant() != enchant)
				continue;
			if (found)
				iter.remove();
			else {
				iter.set(new EnchantWithLevel(enchant, level));
				found = true;
			}
		}
		if (!found)
			addEnchant(enchant, level);
		return true;
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
		this.enchants.addAll(enchants);
	}
	public void clearEnchants() {
		enchants.clear();
	}
	
}
