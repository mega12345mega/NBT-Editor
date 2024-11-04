package com.luneruniverse.minecraft.mod.nbteditor.commands.get;

import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.argument;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalBlock;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.util.BlockStateProperties;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public class GetBlockCommand extends ClientCommand {
	
	@Override
	public String getName() {
		return "block";
	}
	
	@Override
	public String getExtremeAlias() {
		return "b";
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder, String path) {
		Command<FabricClientCommandSource> getBlock = context -> {
			PosArgument posArg = getDefaultArg(context, "pos", null, PosArgument.class);
			BlockPos pos = (posArg == null ? null : posArg.toAbsoluteBlockPos(context.getSource().getPlayer().getCommandSource()));
			if (pos != null && !MainUtil.client.world.isInBuildLimit(pos))
				throw BlockPosArgumentType.OUT_OF_WORLD_EXCEPTION.create();
			BlockStateArgument blockArg = context.getArgument("block", BlockStateArgument.class);
			NbtCompound nbt = blockArg.data;
			if (nbt == null)
				nbt = new NbtCompound();
			LocalBlock block = new LocalBlock(blockArg.getBlockState().getBlock(), new BlockStateProperties(blockArg.getBlockState()), nbt);
			
			if (pos == null) {
				block.toItem().ifPresentOrElse(MainUtil::getWithMessage,
						() -> MainUtil.client.player.sendMessage(TextInst.translatable("nbteditor.nbt.export.item.error"), false));
			} else if (NBTEditorClient.SERVER_CONN.isEditingExpanded())
				block.place(pos);
			else
				MainUtil.client.player.sendMessage(TextInst.translatable("nbteditor.requires_server"), false);
			
			return Command.SINGLE_SUCCESS;
		};
		
		builder.then(argument("block", MVMisc.getBlockStateArg()).executes(getBlock))
				.then(argument("pos", BlockPosArgumentType.blockPos()).then(argument("block", MVMisc.getBlockStateArg()).executes(getBlock)));
	}
	
}
