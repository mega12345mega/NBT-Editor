package com.luneruniverse.minecraft.mod.nbteditor.commands.get;

import java.util.List;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommandGroup;

public class GetCommand extends ClientCommandGroup {
	
	public GetCommand() {
		super(List.of(new GetItemCommand(),
				new GetPotionCommand(),
				new GetSoupCommand(),
				new GetSkullCommand(),
				new GetHdbCommand(),
				new GetColorCodesCommand(),
				new GetLostItemCommand(),
				new GetHelpCommand(),
				new GetCreditsCommand()));
	}
	
	@Override
	public String getName() {
		return "get";
	}
	
	@Override
	public boolean allowShortcuts() {
		return true;
	}
	
}
