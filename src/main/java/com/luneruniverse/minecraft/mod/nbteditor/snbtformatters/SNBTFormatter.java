package com.luneruniverse.minecraft.mod.nbteditor.snbtformatters;

import java.util.Map;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;

import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public abstract class SNBTFormatter {
	
	public static record Result(Text snbt, NbtElement nbt) {
		public static Result ofSuccess(Text snbt, NbtElement nbt) {
			return new Result(snbt, nbt);
		}
		public static Result ofFailure(String snbt) {
			return new Result(TextInst.literal(snbt).formatted(Formatting.RED), null);
		}
		
		public boolean success() {
			return nbt != null;
		}
	}
	
	public static SNBTFormatter FORMATTER = Version.<SNBTFormatter>newSwitch()
			.range("1.21.5", null, NormalSNBTFormatter2::new)
			.range(null, "1.21.4", NormalSNBTFormatter1::new)
			.get();
	
	public static final Formatting NUMBER_COLOR = Formatting.GOLD;
	public static final Formatting TYPE_SUFFIX_COLOR = Formatting.RED;
	public static final Formatting STRING_COLOR = Formatting.GREEN;
	public static final Formatting OPERATOR_COLOR = Formatting.LIGHT_PURPLE;
	public static final Formatting NAME_COLOR = Formatting.AQUA;
	
	public static final Map<String, Number> SPECIAL_NUMS = Map.of(
			"NaNd", Double.NaN,
			"Infinityd", Double.POSITIVE_INFINITY,
			"-Infinityd", Double.NEGATIVE_INFINITY,
			"NaNf", Float.NaN,
			"Infinityf", Float.POSITIVE_INFINITY,
			"-Infinityf", Float.NEGATIVE_INFINITY);
	
	public abstract Result format(String snbt, boolean allowSpecialNumbers);
	
}
