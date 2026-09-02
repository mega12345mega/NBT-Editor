package com.luneruniverse.minecraft.mod.nbteditor.multiversion.textevents.hover;

import java.util.Optional;
import java.util.function.Function;

import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;

public class NewMVHoverAction<E extends HoverEvent, V> implements MVHoverAction<E, V> {
	
	public static final NewMVHoverAction<HoverEvent.ShowText, Text> SHOW_TEXT = new NewMVHoverAction<>("show_text", HoverEvent.ShowText::value, HoverEvent.ShowText::new);
	public static final NewMVHoverAction<HoverEvent.ShowItem, ItemStack> SHOW_ITEM = new NewMVHoverAction<>("show_item", HoverEvent.ShowItem::item, HoverEvent.ShowItem::new);
	public static final NewMVHoverAction<HoverEvent.ShowEntity, HoverEvent.EntityContent> SHOW_ENTITY = new NewMVHoverAction<>("show_entity", HoverEvent.ShowEntity::entity, HoverEvent.ShowEntity::new);
	
	private final String name;
	private final Function<E, V> getter;
	private final Function<V, E> constructor;
	
	public NewMVHoverAction(String name, Function<E, V> getter, Function<V, E> constructor) {
		this.name = name;
		this.getter = getter;
		this.constructor = constructor;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public Optional<V> parseValue(String valueStr) {
		return newEventWithParse(valueStr).map(this::getValue);
	}
	
	@Override
	public V getValue(E event) {
		return getter.apply(event);
	}
	@Override
	public String getStringifiedValue(E event) {
		NbtCompound nbt = (NbtCompound) MVMisc.result(HoverEvent.CODEC.encodeStart(MVMisc.registryNbtOps(), event)).orElseThrow();
		if (this == SHOW_TEXT)
			return nbt.get("value").toString();
		nbt.remove("action");
		return nbt.toString();
	}
	
	@Override
	public E newEvent(V value) {
		return constructor.apply(value);
	}
	@SuppressWarnings("unchecked")
	@Override
	public Optional<E> newEventWithParse(String valueStr) {
		NbtElement valueNbt;
		try {
			valueNbt = MixinLink.parseSnbt(valueStr, ConfigScreen.isSpecialNumbers());
		} catch (CommandSyntaxException e) {
			return Optional.empty();
		}
		
		NbtCompound nbt = new NbtCompound();
		nbt.putString("action", name);
		if (this == SHOW_TEXT)
			nbt.put("value", valueNbt);
		else if (valueNbt instanceof NbtCompound valueNbtCompound && !valueNbtCompound.contains("action"))
			nbt.copyFrom(valueNbtCompound);
		else
			return Optional.empty();
		
		return MVMisc.result(HoverEvent.CODEC.parse(MVMisc.registryNbtOps(), nbt)).map(event -> (E) event);
	}
	
}
