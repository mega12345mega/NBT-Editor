package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.lang.invoke.MethodType;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import com.mojang.serialization.DataResult;

public record Attempt<T>(Optional<T> value, String error) {
	
	private static final Supplier<Reflection.MethodInvoker> DataResult_resultOrPartial =
			Reflection.getOptionalMethod(DataResult.class, "resultOrPartial", MethodType.methodType(Optional.class));
	private static final Supplier<Reflection.MethodInvoker> DataResult_error =
			Reflection.getOptionalMethod(DataResult.class, "error", MethodType.methodType(Optional.class));
	public static <T> Attempt<T> ofResult(DataResult<T> result) {
		Optional<T> value = Version.<Optional<T>>newSwitch()
				.range("1.20.5", null, () -> result.resultOrPartial())
				.range(null, "1.20.4", () -> DataResult_resultOrPartial.get().invoke(result))
				.get();
		
		String error = Version.<Optional<DataResult.Error<T>>>newSwitch()
				.range("1.20.5", null, () -> result.error())
				.range(null, "1.20.4", () -> DataResult_error.get().invoke(result))
				.get()
				.map(DataResult.Error::message).orElse(null);
		
		return new Attempt<>(value, error);
	}
	
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
	
	public <E extends Throwable> T getSuccessOrThrow(Function<String, E> e) throws E {
		if (error != null)
			throw e.apply(error);
		return value.get();
	}
	public T getSuccessOrThrow() throws IllegalStateException {
		return getSuccessOrThrow(IllegalStateException::new);
	}
	
	public <E extends Throwable> T getAttemptOrThrow(Function<String, E> e) throws E {
		return value.orElseThrow(() -> e.apply(error));
	}
	public T getAttemptOrThrow() throws IllegalStateException {
		return getAttemptOrThrow(IllegalStateException::new);
	}
	
	public boolean isSuccessful() {
		return error == null;
	}
	
}
