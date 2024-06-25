package com.luneruniverse.minecraft.mod.nbteditor.commands.get;

import java.util.ArrayList;
import java.util.List;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommandGroup;

public class GetCommand extends ClientCommandGroup {
	
	public static final GetCommand INSTANCE = new GetCommand();
	
	private GetCommand() {
		super(new ArrayList<>(List.of(
				new GetItemCommand(),
				new GetBlockCommand(),
				new GetEntityCommand(),
				new GetPotionCommand(),
				new GetSoupCommand(),
				new GetSkullCommand(),
				new GetHdbCommand(),
				new GetPresetCommand(),
				new GetLostItemCommand(),
				new GetHelpCommand(),
				new GetCreditsCommand())));
	}
	
	@Override
	public String getName() {
		return "get";
	}
	
	@Override
	public String getExtremeAlias() {
		return "g";
	}
	
	@Override
	public boolean allowShortcuts() {
		return true;
	}
	
}
