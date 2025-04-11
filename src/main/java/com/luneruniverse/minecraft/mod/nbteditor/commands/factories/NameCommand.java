package com.luneruniverse.minecraft.mod.nbteditor.commands.factories;

import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.argument;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.commands.arguments.FancyTextArgumentType;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalNBT;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandInternals;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.NBTReference;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.NBTReferenceFilter;
import com.luneruniverse.minecraft.mod.nbteditor.screens.factories.DisplayScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.luneruniverse.minecraft.mod.nbteditor.util.TextUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.text.Text;

public class NameCommand extends ClientCommand {
	
	@Override
	public String getName() {
		return "name";
	}
	
	@Override
	public String getExtremeAlias() {
		return "n";
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder, String path) {
		builder.then(argument("name", FancyTextArgumentType.fancyText()).executes(context -> {
			FancyTextArgumentType.UnparsedText unparsedName = context.getArgument("name", FancyTextArgumentType.UnparsedText.class);
			NBTReference.getReference(NBTReferenceFilter.ANY_NBT, false, ref -> setName(context.getSource(), ref, unparsedName));
			
			return Command.SINGLE_SUCCESS;
		})).executes(context -> {
			NBTReference.getReference(NBTReferenceFilter.ANY_NBT, false,
					ref -> MainUtil.client.setScreen(new DisplayScreen<>(ref)));
			return Command.SINGLE_SUCCESS;
		});
	}
	
	private static <T extends LocalNBT> void setName(FabricClientCommandSource source, NBTReference<T> ref, FancyTextArgumentType.UnparsedText unparsedName) {
		try {
			T localNBT = ref.getLocalNBT();
			Text name = unparsedName.parse(TextUtil.getBaseNameStyle(localNBT, false));
			localNBT.setName(name);
			ref.saveLocalNBT(localNBT, TextInst.translatable("nbteditor.named").append(name));
		} catch (CommandSyntaxException e) {
			source.sendError(ClientCommandInternals.getErrorMessage(e));
		}
	}
	
}
