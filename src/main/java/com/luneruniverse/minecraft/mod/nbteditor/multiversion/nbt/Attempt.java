package com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt;

import java.util.Optional;

public record Attempt<T>(Optional<T> value, String error) {
	
	public Attempt {
		if (value.isEmpty() && error == null)
			throw new IllegalArgumentException("Missing either a value or an error!");
	}
	public Attempt(T value, String error) {
		this(Optional.of(value), error);
	}
	public Attempt(T value) {
		this(value, null);
	}
	
	public T getSuccessOrThrow() {
		if (error != null)
			throw new IllegalStateException(error);
		return value.get();
	}
	public T getAttemptOrThrow() {
		return value.orElseThrow(() -> new IllegalStateException(error));
	}
	public boolean isSuccessful() {
		return error == null;
	}
	
}
