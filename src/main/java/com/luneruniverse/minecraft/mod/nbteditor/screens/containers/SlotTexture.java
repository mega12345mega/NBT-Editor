package com.luneruniverse.minecraft.mod.nbteditor.screens.containers;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.IdentifierInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;

import net.minecraft.util.Identifier;

public record SlotTexture(Identifier texture, int u, int v, int textureWidth, int textureHeight) {
	
	public static final boolean SIMPLIFIED = Version.<Boolean>newSwitch()
			.range("1.21.4", null, true)
			.range(null, "1.21.3", false)
			.get();
	
	public static final SlotTexture HELMET;
	public static final SlotTexture CHESTPLATE;
	public static final SlotTexture LEGGINGS;
	public static final SlotTexture BOOTS;
	
	public static final SlotTexture SADDLE;
	public static final SlotTexture HORSE_ARMOR;
	public static final SlotTexture LLAMA_ARMOR;
	
	public static final SlotTexture SWORD;
	public static final SlotTexture SHIELD;
	
	public static final SlotTexture BREWING_FUEL;
	public static final SlotTexture POTION;
	
	static {
		if (SIMPLIFIED) {
			HELMET = new SlotTexture(IdentifierInst.of("minecraft", "container/slot/helmet"));
			CHESTPLATE = new SlotTexture(IdentifierInst.of("minecraft", "container/slot/chestplate"));
			LEGGINGS = new SlotTexture(IdentifierInst.of("minecraft", "container/slot/leggings"));
			BOOTS = new SlotTexture(IdentifierInst.of("minecraft", "container/slot/boots"));
			
			SADDLE = new SlotTexture(IdentifierInst.of("minecraft", "container/slot/saddle"));
			HORSE_ARMOR = new SlotTexture(IdentifierInst.of("minecraft", "container/slot/horse_armor"));
			LLAMA_ARMOR = new SlotTexture(IdentifierInst.of("minecraft", "container/slot/llama_armor"));
			
			SWORD = new SlotTexture(IdentifierInst.of("minecraft", "container/slot/sword"));
			SHIELD = new SlotTexture(IdentifierInst.of("minecraft", "container/slot/shield"));
			
			BREWING_FUEL = new SlotTexture(IdentifierInst.of("minecraft", "container/slot/brewing_fuel"));
			POTION = new SlotTexture(IdentifierInst.of("minecraft", "container/slot/potion"));
		} else if (Version.<Boolean>newSwitch()
				.range("1.20.2", "1.21.3", true)
				.range(null, "1.20.1", false)
				.get()) {
			HELMET = new SlotTexture(IdentifierInst.of("minecraft", "textures/item/empty_armor_slot_helmet.png"));
			CHESTPLATE = new SlotTexture(IdentifierInst.of("minecraft", "textures/item/empty_armor_slot_chestplate.png"));
			LEGGINGS = new SlotTexture(IdentifierInst.of("minecraft", "textures/item/empty_armor_slot_leggings.png"));
			BOOTS = new SlotTexture(IdentifierInst.of("minecraft", "textures/item/empty_armor_slot_boots.png"));
			
			SADDLE = new SlotTexture(IdentifierInst.of("minecraft", "textures/gui/sprites/container/horse/saddle_slot.png"), 1, 1, 18, 18);
			HORSE_ARMOR = new SlotTexture(IdentifierInst.of("minecraft", "textures/gui/sprites/container/horse/armor_slot.png"), 1, 1, 18, 18);
			LLAMA_ARMOR = new SlotTexture(IdentifierInst.of("minecraft", "textures/gui/sprites/container/horse/llama_armor_slot.png"), 1, 1, 18, 18);
			
			SWORD = new SlotTexture(IdentifierInst.of("minecraft", "textures/item/empty_slot_sword.png"));
			SHIELD = new SlotTexture(IdentifierInst.of("minecraft", "textures/item/empty_armor_slot_shield.png"));
			
			BREWING_FUEL = new SlotTexture(IdentifierInst.of("minecraft", "textures/gui/container/brewing_stand.png"), 17, 17, 256, 256);
			POTION = new SlotTexture(IdentifierInst.of("minecraft", "textures/gui/container/brewing_stand.png"), 79, 58, 256, 256);
		} else {
			HELMET = new SlotTexture(IdentifierInst.of("minecraft", "textures/item/empty_armor_slot_helmet.png"));
			CHESTPLATE = new SlotTexture(IdentifierInst.of("minecraft", "textures/item/empty_armor_slot_chestplate.png"));
			LEGGINGS = new SlotTexture(IdentifierInst.of("minecraft", "textures/item/empty_armor_slot_leggings.png"));
			BOOTS = new SlotTexture(IdentifierInst.of("minecraft", "textures/item/empty_armor_slot_boots.png"));
			
			SADDLE = new SlotTexture(IdentifierInst.of("minecraft", "textures/gui/container/horse.png"), 19, 221, 256, 256);
			HORSE_ARMOR = new SlotTexture(IdentifierInst.of("minecraft", "textures/gui/container/horse.png"), 1, 221, 256, 256);
			LLAMA_ARMOR = new SlotTexture(IdentifierInst.of("minecraft", "textures/gui/container/horse.png"), 37, 221, 256, 256);
			
			SWORD = new SlotTexture(IdentifierInst.of(Version.<String>newSwitch()
					.range("1.19.4", null, "minecraft")
					.range(null, "1.19.3", "nbteditor")
					.get(), "textures/item/empty_slot_sword.png"));
			SHIELD = new SlotTexture(IdentifierInst.of("minecraft", "textures/item/empty_armor_slot_shield.png"));
			
			BREWING_FUEL = new SlotTexture(IdentifierInst.of("minecraft", "textures/gui/container/brewing_stand.png"), 17, 17, 256, 256);
			POTION = new SlotTexture(IdentifierInst.of("minecraft", "textures/gui/container/brewing_stand.png"), 79, 58, 256, 256);
		}
	}
	
	public SlotTexture {
		if (SIMPLIFIED) {
			if (u != 0 || v != 0)
				throw new IllegalArgumentException("u and v must be 0 in 1.21.4+");
			if (textureWidth != 16 || textureHeight != 16)
				throw new IllegalArgumentException("textureWidth and textureHeight must be 16 in 1.21.4+");
		}
	}
	public SlotTexture(Identifier texture) {
		this(texture, 0, 0, 16, 16);
	}
	
}
