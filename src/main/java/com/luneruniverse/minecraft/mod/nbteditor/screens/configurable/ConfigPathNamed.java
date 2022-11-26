package com.luneruniverse.minecraft.mod.nbteditor.screens.configurable;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionMisc;

import net.minecraft.text.Text;

public interface ConfigPathNamed extends ConfigPath {
	public Text getName();
	public void setNamePrefix(Text prefix);
	public Text getNamePrefix();
	public default Text getFullName() {
		Text name = getName();
		Text prefix = getNamePrefix();
		if (name == null)
			return prefix == null ? null : MultiVersionMisc.copyText(prefix);
		if (prefix == null)
			return MultiVersionMisc.copyText(name);
		return MultiVersionMisc.copyText(prefix).append(MultiVersionMisc.copyText(name));
	}
}
