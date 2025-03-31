package com.luneruniverse.minecraft.mod.nbteditor.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.luneruniverse.minecraft.mod.nbteditor.server.ServerMVMisc;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.state.property.Property;

public class BlockStateProperties {
	
	private class BlockStateProperty {
		private String value;
		private final List<String> options;
		public BlockStateProperty(String value, List<String> options) {
			if (!options.contains(value))
				throw new IllegalArgumentException("The current value must be an option!");
			this.value = value;
			this.options = options;
		}
		public <T extends Comparable<T>> BlockStateProperty(Property<T> property, BlockState state) {
			this(property.name(state.get(property)), ServerMVMisc.getValues(property).stream().map(option -> property.name(option)).toList());
		}
		private void setValue(String value) {
			if (!options.contains(value))
				throw new IllegalArgumentException("The new value must be an option!");
			this.value = value;
		}
		public BlockStateProperty copy() {
			return new BlockStateProperty(value, new ArrayList<>(options));
		}
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof BlockStateProperty property)
				return this.value.equals(property.value) && this.options.equals(property.options);
			return false;
		}
	}
	
	private final LinkedHashMap<String, BlockStateProperty> properties;
	
	public BlockStateProperties(BlockState state) {
		properties = new LinkedHashMap<>();
		for (Property<?> property : state.getProperties())
			properties.put(property.getName(), new BlockStateProperty(property, state));
	}
	public BlockStateProperties(PacketByteBuf payload) {
		properties = new LinkedHashMap<>();
		for (int i = 0, numProperties = payload.readVarInt(); i < numProperties; i++) {
			String name = payload.readString();
			int valueIndex = payload.readVarInt();
			List<String> options = new ArrayList<>();
			for (int optionI = 0, numOptions = payload.readVarInt(); optionI < numOptions; optionI++)
				options.add(payload.readString());
			properties.put(name, new BlockStateProperty(options.get(valueIndex), options));
		}
	}
	private BlockStateProperties(BlockStateProperties toCopy) {
		properties = toCopy.properties.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().copy(),
						(a, b) -> { throw new RuntimeException("Impossible merge conflict!"); }, LinkedHashMap::new));
	}
	
	public Set<String> getProperties() {
		return properties.keySet();
	}
	public String getValue(String property) {
		return properties.get(property).value;
	}
	public List<String> getOptions(String property) {
		return properties.get(property).options;
	}
	
	public void setValue(String property, String value) {
		properties.get(property).setValue(value);
	}
	
	public BlockState applyTo(BlockState state) {
		Map<String, Property<?>> properties = state.getProperties().stream()
				.collect(Collectors.toMap(Property::getName, Function.identity()));
		if (!this.properties.keySet().equals(properties.keySet()))
			throw new IllegalArgumentException("The provided BlockState doesn't have the same properties!");
		for (Map.Entry<String, BlockStateProperty> property : this.properties.entrySet())
			state = applyPropertyTo(state, properties.get(property.getKey()), property.getValue().value);
		return state;
	}
	private <T extends Comparable<T>> BlockState applyPropertyTo(BlockState state, Property<T> property, String value) {
		T valueObj = ServerMVMisc.getValues(property).stream().filter(option -> property.name(option).equals(value)).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("The property value doesn't exist!"));
		return state.with(property, valueObj);
	}
	
	public BlockStateProperties mapTo(BlockState state) {
		BlockStateProperties properties = new BlockStateProperties(state);
		properties.properties.forEach((name, property) -> {
			BlockStateProperty prevProperty = this.properties.get(name);
			if (prevProperty != null && property.options.contains(prevProperty.value))
				property.value = prevProperty.value;
		});
		return properties;
	}
	public BlockState applyToSafely(BlockState state) {
		return mapTo(state).applyTo(state);
	}
	
	public NbtCompound getValues() {
		NbtCompound output = new NbtCompound();
		for (Map.Entry<String, BlockStateProperty> property : properties.entrySet())
			output.putString(property.getKey(), property.getValue().value);
		return output;
	}
	public Set<String> setValues(NbtCompound blockStateTag) {
		Set<String> unset = new HashSet<>(properties.keySet());
		for (String tag : blockStateTag.getKeys()) {
			if (!blockStateTag.contains(tag, NbtElement.STRING_TYPE))
				continue;
			BlockStateProperty property = properties.get(tag);
			if (property == null)
				continue;
			String value = blockStateTag.getString(tag);
			if (property.options.contains(value)) {
				property.value = value;
				unset.remove(tag);
			}
		}
		return unset;
	}
	
	public Map<String, String> getValuesMap() {
		Map<String, String> output = new HashMap<>();
		for (Map.Entry<String, BlockStateProperty> property : properties.entrySet())
			output.put(property.getKey(), property.getValue().value);
		return output;
	}
	public Set<String> setValuesMap(Map<String, String> blockStateTag) {
		Set<String> unset = new HashSet<>(properties.keySet());
		for (String tag : blockStateTag.keySet()) {
			BlockStateProperty property = properties.get(tag);
			if (property == null)
				continue;
			String value = blockStateTag.get(tag);
			if (property.options.contains(value)) {
				property.value = value;
				unset.remove(tag);
			}
		}
		return unset;
	}
	
	public void writeToPayload(PacketByteBuf payload) {
		payload.writeVarInt(properties.size());
		properties.forEach((name, property) -> {
			payload.writeString(name);
			payload.writeVarInt(property.options.indexOf(property.value));
			payload.writeVarInt(property.options.size());
			property.options.forEach(payload::writeString);
		});
	}
	
	public BlockStateProperties copy() {
		return new BlockStateProperties(this);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BlockStateProperties state)
			return this.properties.equals(state.properties);
		return false;
	}
	
	@Override
	public String toString() {
		return "[" + properties.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue().value).reduce((a, b) -> a + "," + b).orElse("") + "]";
	}
	
}
