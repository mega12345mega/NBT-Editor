package com.luneruniverse.minecraft.mod.nbteditor.mixin.toggled;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.snbtformatters.SNBTFormatter;
import com.mojang.brigadier.StringReader;
import com.mojang.serialization.DynamicOps;

import net.minecraft.nbt.SnbtParsing;
import net.minecraft.util.packrat.ParsingRule;
import net.minecraft.util.packrat.ParsingRules;
import net.minecraft.util.packrat.ParsingState;
import net.minecraft.util.packrat.Symbol;
import net.minecraft.util.packrat.Term;

@Mixin(SnbtParsing.class)
public class SnbtParsingMixin {
	
	@WrapOperation(method = "createParser", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/packrat/Symbol;of(Ljava/lang/String;)Lnet/minecraft/util/packrat/Symbol;", ordinal = 0),
			slice = @Slice(from = @At(value = "CONSTANT", args = "stringValue=float_type_suffix")))
	private static Symbol<?> createParser$Symbol_of(String name, Operation<Symbol<?>> original, @Share("floatTypeSuffix") LocalRef<Symbol<?>> floatTypeSuffixRef) {
		if (!name.equals("float_type_suffix"))
			throw new AssertionError("nbteditor SnbtParsingMixin createParser$Symbol_of injected into the wrong place");
		
		Symbol<?> floatTypeSuffix = original.call(name);
		floatTypeSuffixRef.set(floatTypeSuffix);
		return floatTypeSuffix;
	}
	
	@ModifyArgs(method = "createParser", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/packrat/ParsingRules;set(Lnet/minecraft/util/packrat/Symbol;Lnet/minecraft/util/packrat/Term;Lnet/minecraft/util/packrat/ParsingRule$RuleAction;)Lnet/minecraft/util/packrat/ParsingRuleEntry;", ordinal = 0),
			slice = @Slice(from = @At(value = "CONSTANT", args = "stringValue=float_literal")))
	private static <T> void createParser$ParsingRules_set(Args args, @Local ParsingRules<StringReader> parsingRules, @Local DynamicOps<T> ops, @Share("floatTypeSuffix") LocalRef<Symbol<?>> floatTypeSuffixRef) {
		if (!args.<Symbol<?>>get(0).name().equals("float_literal"))
			throw new AssertionError("nbteditor SnbtParsingMixin createParser$ParsingRules_set injected into the wrong place");
		
		Term<StringReader> term = args.get(1);
		ParsingRule.RuleAction<StringReader, T> action = args.get(2);
		Symbol<?> floatTypeSuffix = floatTypeSuffixRef.get();
		
		Symbol<T> floatSpecial = Symbol.of("float_special");
		parsingRules.set(floatSpecial, new ParsingRule<StringReader, T>() {
			@Override
			public T parse(final ParsingState<StringReader> state) {
				if (!MixinLink.specialNumbers.contains(Thread.currentThread()))
					return null;
				
				state.getReader().skipWhitespace();
				
				for (Map.Entry<String, Number> specialNum : SNBTFormatter.SPECIAL_NUMS.entrySet()) {
					if (state.getReader().getString().startsWith(specialNum.getKey(), state.getCursor())) {
						state.setCursor(state.getCursor() + specialNum.getKey().length() - 1); // Don't consume suffix
						
						if (specialNum.getValue() instanceof Double d)
							return ops.createDouble(d);
						else if (specialNum.getValue() instanceof Float f)
							return ops.createFloat(f);
						else
							throw new IllegalStateException("Number of invalid type: " + specialNum.getValue().getClass().getName());
					}
				}
				
				return null;
			}
		});
		
		args.set(1, Term.anyOf(term, Term.sequence(parsingRules.term(floatSpecial), parsingRules.term(floatTypeSuffix))));
		
		args.set(2, (ParsingRule.RuleAction<StringReader, T>) state -> {
			T specialNumber = state.getResults().get(floatSpecial);
			if (specialNumber != null)
				return specialNumber;
			
			return action.run(state);
		});
	}
	
	@WrapOperation(method = "method_68722", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/DynamicOps;createString(Ljava/lang/String;)Ljava/lang/Object;", remap = false))
	private static Object createParser$method_68722$DynamicOps_createString(DynamicOps<?> ops, String str, Operation<Object> original) {
		if (MixinLink.specialNumbers.contains(Thread.currentThread())) {
			Number specialNum = SNBTFormatter.SPECIAL_NUMS.get(str);
			if (specialNum != null) {
				if (specialNum instanceof Double d)
					return ops.createDouble(d);
				else if (specialNum instanceof Float f)
					return ops.createFloat(f);
				else
					throw new IllegalStateException("Number of invalid type: " + specialNum.getClass().getName());
			}
		}
		return original.call(ops, str);
	}
	
	@WrapOperation(method = "parseFiniteFloat", at = @At(value = "INVOKE", target = "Ljava/lang/Float;isFinite(F)Z"))
	private static boolean parseFiniteFloat$Float_isFinite(float f, Operation<Boolean> original) {
		if (MixinLink.specialNumbers.contains(Thread.currentThread()))
			return true;
		return original.call(f);
	}
	
	@WrapOperation(method = "parseFiniteDouble", at = @At(value = "INVOKE", target = "Ljava/lang/Double;isFinite(D)Z"))
	private static boolean parseFiniteDouble$Double_isFinite(double d, Operation<Boolean> original) {
		if (MixinLink.specialNumbers.contains(Thread.currentThread()))
			return true;
		return original.call(d);
	}
	
}
