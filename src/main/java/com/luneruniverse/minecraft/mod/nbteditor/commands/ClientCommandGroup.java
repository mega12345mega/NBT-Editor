package com.luneruniverse.minecraft.mod.nbteditor.commands;

import java.util.List;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

public abstract class ClientCommandGroup extends ClientCommand {
	
	private final List<ClientCommand> children;
	
	public ClientCommandGroup(List<ClientCommand> children) {
		this.children = children;
	}
	
	public final List<ClientCommand> getChildren() {
		return children;
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
		for (ClientCommand child : children)
			child.registerAll(builder::then);
	}
	
	@Override
	public ClientCommand getShortcut(List<String> path, int index) {
		ClientCommand output = super.getShortcut(path, index);
		if (output != null || !allowShortcuts())
			return output;
		
		if (path.size() > index) {
			String next = path.get(index);
			return children.stream().filter(cmd -> cmd.getName().equals(next) || (cmd.getAliases() != null && cmd.getAliases().contains(next)))
					.findFirst().map(cmd -> cmd.getShortcut(path, index + 1)).orElse(null);
		}
		return null;
	}
	
	public boolean allowShortcuts() {
		return false;
	}
	
}
