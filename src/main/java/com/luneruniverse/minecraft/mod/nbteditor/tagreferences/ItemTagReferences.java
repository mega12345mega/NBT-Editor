package com.luneruniverse.minecraft.mod.nbteditor.tagreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general.ComponentTagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general.NBTTagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general.TagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.AttributesNBTTagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.CustomDataNBTTagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.CustomPotionContentsNBTTagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.GameProfileNBTTagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.GameProfileNameNBTTagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.HideFlagsNBTTagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.AttributeData;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.AttributeData.AttributeModifierData.Operation;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.AttributeData.AttributeModifierData.Slot;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.CustomPotionContents;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.HideFlag;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.component.type.UnbreakableComponent;
import net.minecraft.component.type.WritableBookContentComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.text.RawFilteredPair;

public class ItemTagReferences {
	
	public static final TagReference<Boolean, ItemStack> HIDE_ADDITIONAL_TOOLTIP = Version.<TagReference<Boolean, ItemStack>>newSwitch()
			.range("1.20.5", null, () -> ComponentTagReference.forExistance(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP))
			.range(null, "1.20.4", () -> new HideFlagsNBTTagReference(HideFlag.MISC))
			.get();
	
	public static final TagReference<CustomPotionContents, ItemStack> CUSTOM_POTION_CONTENTS = Version.<TagReference<CustomPotionContents, ItemStack>>newSwitch()
			.range("1.20.5", null, () -> new ComponentTagReference<>(DataComponentTypes.POTION_CONTENTS,
					() -> new PotionContentsComponent(Optional.empty(), Optional.empty(), List.of()),
					contents -> new CustomPotionContents(contents.customColor(), contents.customEffects()),
					contents -> new PotionContentsComponent(Optional.empty(), contents.color(), contents.effects())))
			.range(null, "1.20.4", () -> new CustomPotionContentsNBTTagReference())
			.get();
	
	public static final TagReference<Optional<String>, ItemStack> PROFILE_NAME = Version.<TagReference<Optional<String>, ItemStack>>newSwitch()
			.range("1.20.5", null, () -> new ComponentTagReference<>(DataComponentTypes.PROFILE,
					null,
					component -> component == null ? Optional.empty() : component.name(),
					name -> new ProfileComponent(name, Optional.empty(), new PropertyMap())))
			.range(null, "1.20.4", () -> TagReference.forItems(Optional::empty, new GameProfileNameNBTTagReference()))
			.get();
	public static final TagReference<Optional<GameProfile>, ItemStack> PROFILE = Version.<TagReference<Optional<GameProfile>, ItemStack>>newSwitch()
			.range("1.20.5", null, () -> new ComponentTagReference<>(DataComponentTypes.PROFILE,
					null,
					profile -> Optional.ofNullable(profile).map(ProfileComponent::gameProfile),
					profile -> profile.map(ProfileComponent::new).orElse(null)))
			.range(null, "1.20.4", () -> TagReference.forItems(Optional::empty, new GameProfileNBTTagReference()))
			.get();
	
	public static final TagReference<List<AttributeData>, ItemStack> ATTRIBUTES = Version.<TagReference<List<AttributeData>, ItemStack>>newSwitch()
			.range("1.20.5", null, () -> new ComponentTagReference<>(DataComponentTypes.ATTRIBUTE_MODIFIERS,
					() -> new AttributeModifiersComponent(List.of(), true),
					component -> component.modifiers().stream().map(data -> new AttributeData(
							data.attribute().value(), data.modifier().value(), Operation.fromMinecraft(data.modifier().operation()),
							Slot.fromMinecraft(data.slot()), data.modifier().uuid())).collect(Collectors.toList()),
					(component, list) -> new AttributeModifiersComponent(list.stream().map(
							data -> new AttributeModifiersComponent.Entry(Registries.ATTRIBUTE.getEntry(data.attribute()),
									new EntityAttributeModifier(data.modifierData().get().uuid(),
											Registries.ATTRIBUTE.getId(data.attribute()).toString(), data.value(),
											data.modifierData().get().operation().toMinecraft()),
									data.modifierData().get().slot().toMinecraft())).toList(),
							component == null ? true : component.showInTooltip())))
			.range(null, "1.20.4", () -> new AttributesNBTTagReference())
			.get();
	
	public static final TagReference<List<String>, ItemStack> WRITABLE_BOOK_PAGES = Version.<TagReference<List<String>, ItemStack>>newSwitch()
			.range("1.20.5", null, () -> new ComponentTagReference<>(DataComponentTypes.WRITABLE_BOOK_CONTENT,
					() -> new WritableBookContentComponent(List.of()),
					content -> content.pages().stream().map(RawFilteredPair::raw).collect(Collectors.toList()),
					pages -> new WritableBookContentComponent(pages.stream().map(RawFilteredPair::of).toList())))
			.range(null, "1.20.4", () -> TagReference.forItems(ArrayList::new, TagReference.forLists(String.class, new NBTTagReference<>(String[].class, "pages"))))
			.get();
	
	public static final TagReference<Boolean, ItemStack> UNBREAKABLE = Version.<TagReference<Boolean, ItemStack>>newSwitch()
			.range("1.20.5", null, () -> ComponentTagReference.forExistance(DataComponentTypes.UNBREAKABLE, () -> new UnbreakableComponent(true)))
			.range(null, "1.20.4", () -> TagReference.forItems(() -> false, new NBTTagReference<>(Boolean.class, "Unbreakable")))
			.get();
	
	public static final TagReference<NbtCompound, ItemStack> CUSTOM_DATA = Version.<TagReference<NbtCompound, ItemStack>>newSwitch()
			.range("1.20.5", null, () -> new ComponentTagReference<NbtCompound, NbtComponent>(DataComponentTypes.CUSTOM_DATA,
					null,
					component -> component == null ? new NbtCompound() : component.copyNbt(),
					NbtComponent::of))
			.range(null, "1.20.4", () -> new CustomDataNBTTagReference())
			.get();
	
}
