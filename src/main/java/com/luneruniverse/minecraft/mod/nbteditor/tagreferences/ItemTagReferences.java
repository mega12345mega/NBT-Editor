package com.luneruniverse.minecraft.mod.nbteditor.tagreferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVComponentType;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general.ComponentTagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general.NBTTagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general.TagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.AttributesNBTTagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.CustomDataNBTTagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.CustomPotionContentsNBTTagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.EnchantsTagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.GameProfileNBTTagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.GameProfileNameNBTTagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.AttributeData;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.CustomPotionContents;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.Enchants;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;

import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.BlockStateComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.component.type.UnbreakableComponent;
import net.minecraft.component.type.WritableBookContentComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.Text;

public class ItemTagReferences {
	
	private static TagReference<NbtCompound, ItemStack> getComponentTagRefOfNBT(MVComponentType<NbtComponent> component) {
		return new ComponentTagReference<>(component,
				null,
				componentValue -> componentValue == null ? new NbtCompound() : componentValue.copyNbt(),
				NbtComponent::of);
	}
	
	public static final TagReference<CustomPotionContents, ItemStack> CUSTOM_POTION_CONTENTS = Version.<TagReference<CustomPotionContents, ItemStack>>newSwitch()
			.range("1.20.5", null, () -> new ComponentTagReference<>(MVComponentType.POTION_CONTENTS,
					() -> MVMisc.newPotionContentsComponent(Optional.empty(), Optional.empty(), List.of()),
					contents -> new CustomPotionContents(contents.customColor(), contents.customEffects()),
					contents -> MVMisc.newPotionContentsComponent(Optional.empty(), contents.color(), contents.effects())))
			.range(null, "1.20.4", () -> new CustomPotionContentsNBTTagReference())
			.get();
	
	public static final TagReference<Optional<String>, ItemStack> PROFILE_NAME = Version.<TagReference<Optional<String>, ItemStack>>newSwitch()
			.range("1.20.5", null, () -> new ComponentTagReference<>(MVComponentType.PROFILE,
					null,
					component -> component == null ? Optional.empty() : component.name(),
					name -> new ProfileComponent(name, Optional.empty(), new PropertyMap())))
			.range(null, "1.20.4", () -> TagReference.forItems(Optional::empty, new GameProfileNameNBTTagReference()))
			.get();
	public static final TagReference<Optional<GameProfile>, ItemStack> PROFILE = Version.<TagReference<Optional<GameProfile>, ItemStack>>newSwitch()
			.range("1.20.5", null, () -> new ComponentTagReference<>(MVComponentType.PROFILE,
					null,
					profile -> Optional.ofNullable(profile).map(ProfileComponent::gameProfile),
					profile -> profile.map(ProfileComponent::new).orElse(null)))
			.range(null, "1.20.4", () -> TagReference.forItems(Optional::empty, new GameProfileNBTTagReference()))
			.get();
	
	public static final TagReference<List<AttributeData>, ItemStack> ATTRIBUTES = Version.<TagReference<List<AttributeData>, ItemStack>>newSwitch()
			.range("1.20.5", null, () -> new ComponentTagReference<>(MVComponentType.ATTRIBUTE_MODIFIERS,
					() -> new AttributeModifiersComponent(List.of(), true),
					component -> component.modifiers().stream().map(AttributeData::fromComponentEntry).collect(Collectors.toList()),
					(component, list) -> new AttributeModifiersComponent(
							list.stream().map(AttributeData::toComponentEntry).toList(),
							component == null ? true : component.showInTooltip())))
			.range(null, "1.20.4", () -> TagReference.forItems(ArrayList::new, new AttributesNBTTagReference(AttributesNBTTagReference.NBTLayout.ITEM_OLD)))
			.get();
	
	public static final TagReference<List<String>, ItemStack> WRITABLE_BOOK_PAGES = Version.<TagReference<List<String>, ItemStack>>newSwitch()
			.range("1.20.5", null, () -> new ComponentTagReference<>(MVComponentType.WRITABLE_BOOK_CONTENT,
					() -> new WritableBookContentComponent(List.of()),
					content -> content.pages().stream().map(RawFilteredPair::raw).collect(Collectors.toList()),
					pages -> new WritableBookContentComponent(pages.stream().map(RawFilteredPair::of).toList())))
			.range(null, "1.20.4", () -> TagReference.forItems(ArrayList::new, TagReference.forLists(String.class, new NBTTagReference<>(String[].class, "pages"))))
			.get();
	
	public static final TagReference<Boolean, ItemStack> UNBREAKABLE = Version.<TagReference<Boolean, ItemStack>>newSwitch()
			.range("1.20.5", null, () -> ComponentTagReference.forExistance(MVComponentType.UNBREAKABLE, () -> new UnbreakableComponent(true)))
			.range(null, "1.20.4", () -> TagReference.forItems(() -> false, new NBTTagReference<>(Boolean.class, "Unbreakable")))
			.get();
	
	public static final TagReference<NbtCompound, ItemStack> CUSTOM_DATA = Version.<TagReference<NbtCompound, ItemStack>>newSwitch()
			.range("1.20.5", null, () -> getComponentTagRefOfNBT(MVComponentType.CUSTOM_DATA))
			.range(null, "1.20.4", () -> new CustomDataNBTTagReference())
			.get();
	
	public static final TagReference<Map<String, String>, ItemStack> BLOCK_STATE = Version.<TagReference<Map<String, String>, ItemStack>>newSwitch()
			.range("1.20.5", null, () -> new ComponentTagReference<>(MVComponentType.BLOCK_STATE,
					null,
					component -> component == null ? new HashMap<>() : new HashMap<>(component.properties()),
					BlockStateComponent::new))
			.range(null, "1.20.4", () -> TagReference.forItems(HashMap::new, TagReference.forMaps(
					element -> element instanceof NbtString str ? str.asString() : null,
					NbtString::of,
					new NBTTagReference<>(NbtCompound.class, "BlockStateTag"))))
			.get();
	
	public static final TagReference<NbtCompound, ItemStack> BLOCK_ENTITY_DATA = Version.<TagReference<NbtCompound, ItemStack>>newSwitch()
			.range("1.20.5", null, () -> getComponentTagRefOfNBT(MVComponentType.BLOCK_ENTITY_DATA))
			.range(null, "1.20.4", () -> TagReference.forItems(NbtCompound::new, new NBTTagReference<>(NbtCompound.class, "BlockEntityTag")))
			.get();
	
	public static final TagReference<NbtCompound, ItemStack> ENTITY_DATA = Version.<TagReference<NbtCompound, ItemStack>>newSwitch()
			.range("1.20.5", null, () -> getComponentTagRefOfNBT(MVComponentType.ENTITY_DATA))
			.range(null, "1.20.4", () -> TagReference.forItems(NbtCompound::new, new NBTTagReference<>(NbtCompound.class, "EntityTag")))
			.get();
	
	public static final TagReference<Enchants, ItemStack> ENCHANTMENTS = new EnchantsTagReference();
	
	public static final TagReference<List<Text>, ItemStack> LORE = Version.<TagReference<List<Text>, ItemStack>>newSwitch()
			.range("1.20.5", null, () -> new ComponentTagReference<>(MVComponentType.LORE,
					() -> LoreComponent.DEFAULT,
					component -> new ArrayList<>(component.lines()),
					lore -> new LoreComponent(lore.stream().limit(256).toList())))
			.range(null, "1.20.4", () -> TagReference.forItems(ArrayList::new, TagReference.forLists(Text.class, new NBTTagReference<>(Text[].class, "display/Lore"))))
			.get();
	
}
