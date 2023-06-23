package com.luneruniverse.minecraft.mod.nbteditor.screens.configurable;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;

import net.minecraft.text.Text;

public interface ConfigPathNamed extends ConfigPath {
	public Text getName();
	public void setNamePrefix(Text prefix);
	public Text getNamePrefix();
	public default Text getFullName() {
		Text name = getName();
		Text prefix = getNamePrefix();
		if (name == null)
			return prefix == null ? null : prefix.copy();
		if (prefix == null)
			return name.copy();
		return TextInst.copy(prefix).append(TextInst.copy(name));
	}
}
