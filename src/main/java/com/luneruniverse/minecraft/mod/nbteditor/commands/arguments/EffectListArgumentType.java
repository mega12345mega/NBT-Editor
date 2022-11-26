package com.luneruniverse.minecraft.mod.nbteditor.commands.arguments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class EffectListArgumentType implements ArgumentType<Collection<StatusEffectInstance>> {
	
	public enum Arg {
		DURATION("-duration", (effect, str) -> new StatusEffectInstance(effect.getEffectType(), Integer.parseInt(str) * 20, effect.getAmplifier(), effect.isAmbient(), effect.shouldShowParticles(), effect.shouldShowIcon()), false),
		AMPLIFIER("-amplifier", (effect, str) -> new StatusEffectInstance(effect.getEffectType(), effect.getDuration(), Integer.parseInt(str), effect.isAmbient(), effect.shouldShowParticles(), effect.shouldShowIcon()), false),
		AMBIENT("-ambient", (effect, str) -> new StatusEffectInstance(effect.getEffectType(), effect.getDuration(), effect.getAmplifier(), parseBoolean(str), effect.shouldShowParticles(), effect.shouldShowIcon()), true),
		PERMANENT("-permanent", (effect, str) -> { effect.setPermanent(parseBoolean(str)); return effect; }, true),
		SHOW_PARTICLES("-showparticles", (effect, str) -> new StatusEffectInstance(effect.getEffectType(), effect.getDuration(), effect.getAmplifier(), effect.isAmbient(), parseBoolean(str), effect.shouldShowIcon()), true),
		SHOW_ICON("-showicon", (effect, str) -> new StatusEffectInstance(effect.getEffectType(), effect.getDuration(), effect.getAmplifier(), effect.isAmbient(), effect.shouldShowParticles(), parseBoolean(str)), true);
		
		private static boolean parseBoolean(String str) {
			if (str.equalsIgnoreCase("true"))
				return true;
			if (str.equalsIgnoreCase("false"))
				return false;
			
			throw new IllegalArgumentException("Expected true or false");
		}
		
		private final String name;
		private final BiFunction<StatusEffectInstance, String, StatusEffectInstance> apply;
		private final boolean isBoolean;
		
		private Arg(String name, BiFunction<StatusEffectInstance, String, StatusEffectInstance> apply, boolean isBoolean) {
			this.name = name;
			this.apply = apply;
			this.isBoolean = isBoolean;
		}
	}
	
	private static final Collection<String> EXAMPLES = Arrays.asList("minecraft:blindness duration:1 showparticles:false", "minecraft:jump_boost");
	public static final DynamicCommandExceptionType INVALID_EFFECT_EXCEPTION = new DynamicCommandExceptionType((id) -> {
		return TextInst.translatable("effect.effectNotFound", new Object[]{id});
	});

	public static EffectListArgumentType effectList() {
		return new EffectListArgumentType();
	}

	@SuppressWarnings("unchecked")
	public static Collection<StatusEffectInstance> getStatusEffectInstance(CommandContext<ServerCommandSource> context, String name) {
		return context.getArgument(name, Collection.class);
	}

	public Collection<StatusEffectInstance> parse(StringReader stringReader) throws CommandSyntaxException {
		List<StatusEffectInstance> effects = new ArrayList<>();
		while (stringReader.canRead()) {
			Identifier identifier = Identifier.fromCommandInput(stringReader);
			StatusEffect type = Registry.STATUS_EFFECT.getOrEmpty(identifier).orElseThrow(() -> {
				return INVALID_EFFECT_EXCEPTION.create(identifier);
			});
			if (!stringReader.canRead()) {
				effects.add(new StatusEffectInstance(type, 5 * 20));
				break;
			}
			if (stringReader.read() != ' ')
				throw new SimpleCommandExceptionType(TextInst.translatable("nbteditor.effect_list_arg_type.expected.space")).createWithContext(stringReader);
			
			StatusEffectInstance effect = new StatusEffectInstance(type, 5 * 20);
			
			while (stringReader.canRead() && stringReader.peek() == '-') {
				StringBuilder arg = new StringBuilder();
				while (stringReader.canRead() && stringReader.peek() != ':')
					arg.append(stringReader.read());
				Arg key = Arrays.stream(Arg.values()).filter(test -> test.name.equalsIgnoreCase(arg.toString())).findFirst().orElse(null);
				if (key == null)
					throw new SimpleCommandExceptionType(TextInst.translatable("nbteditor.effect_list_arg_type.invalid.arg")).createWithContext(stringReader);
				if (!stringReader.canRead())
					throw new SimpleCommandExceptionType(TextInst.translatable("nbteditor.effect_list_arg_type.expected.colon")).createWithContext(stringReader);
				
				stringReader.read(); // Colon
				
				StringBuilder value = new StringBuilder();
				while (stringReader.canRead() && stringReader.peek() != ' ')
					value.append(stringReader.read());
				
				try {
					effect = key.apply.apply(effect, value.toString());
				} catch (Exception e) {
					throw new SimpleCommandExceptionType(TextInst.translatable("nbteditor.effect_list_arg_type.invalid.value")).createWithContext(stringReader);
				}
				
				if (stringReader.canRead())
					stringReader.read();
			}
			
			effects.add(effect);
		}
		return effects;
	}

	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		boolean afterFirstEffect = builder.getRemaining().contains(" ");
		builder = builder.createOffset(builder.getInput().lastIndexOf(" ") + 1);
		if (afterFirstEffect) {
			for (Arg arg : Arg.values()) {
				if ((arg.name + ":").startsWith(builder.getRemainingLowerCase()))
					builder.suggest(arg.name + ":");
			}
			
			if (builder.getRemaining().startsWith("-")) {
				int colonPos = builder.getRemaining().indexOf(":");
				if (colonPos != -1) {
					String argStr = builder.getRemaining().substring(0, colonPos);
					for (Arg arg : Arg.values()) {
						if (arg.name.equals(argStr)) {
							if (arg.isBoolean) {
								String value = builder.getRemaining().substring(colonPos + 1);
								if ("true".startsWith(value.toLowerCase()))
									builder.suggest(argStr + ":true");
								if ("false".startsWith(value.toLowerCase()))
									builder.suggest(argStr + ":false");
							}
							break;
						}
					}
				}
			}
		}
		return CommandSource.suggestIdentifiers(Registry.STATUS_EFFECT.getIds(), builder);
	}

	public Collection<String> getExamples() {
		return EXAMPLES;
	}
}
