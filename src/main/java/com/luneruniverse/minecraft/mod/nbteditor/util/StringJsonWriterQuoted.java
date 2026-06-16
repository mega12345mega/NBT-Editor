package com.luneruniverse.minecraft.mod.nbteditor.util;

import java.util.ArrayList;
import java.util.Collections;

import com.google.common.collect.Lists;
import com.luneruniverse.minecraft.mod.nbteditor.mixin.StringNbtWriterAccessor;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;

import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.visitor.StringNbtWriter;

public class StringJsonWriterQuoted extends StringNbtWriter {
	
	// From StringNbtWriter.apply in <= 1.21.4
	public String apply(NbtElement element) {
		element.accept(this);
		return ((StringNbtWriterAccessor) this).getResult().toString();
	}
	
	@Override
	public void visitByte(NbtByte element) {
		if (element.nbte$byteValue() == 0)
			((StringNbtWriterAccessor) this).getResult().append(false);
		else if (element.nbte$byteValue() == 1)
			((StringNbtWriterAccessor) this).getResult().append(true);
		else
			super.visitByte(element);
	}
	
	@Override
	public void visitString(NbtString element) {
		((StringNbtWriterAccessor) this).getResult().append(escape(MVMisc.value(element)));
	}
	
	@Override
	public void visitList(NbtList element) {
		StringBuilder result = ((StringNbtWriterAccessor) this).getResult();
		
		result.append('[');
		for (int i = 0; i < element.nbte$size(); ++i) {
			if (i != 0) {
				result.append(',');
			}
			result.append(new StringJsonWriterQuoted().apply(element.get(i)));
		}
		result.append(']');
	}
	
	@Override
	public void visitCompound(NbtCompound compound) {
		StringBuilder result = ((StringNbtWriterAccessor) this).getResult();
		
		result.append('{');
		ArrayList<String> list = Lists.newArrayList(compound.getKeys());
		Collections.sort(list);
		for (String string : list) {
			if (result.length() != 1) {
				result.append(',');
			}
			result.append(escape(string)).append(':').append(new StringJsonWriterQuoted().apply(compound.get(string)));
		}
		result.append('}');
	}
	
	// From NbtString.escape
	// Edited to optionally force double quotes
	private static String escape(String value) {
		StringBuilder builder = new StringBuilder(" ");
		char quote = (ConfigScreen.isSingleQuotesAllowed() ? '\0' : '"');
		
		for (char c : value.toCharArray()) {
			if (c == '\\') {
				builder.append('\\');
			} else if (c == '"' || c == '\'') {
				if (quote == '\0')
					quote = (c == '"' ? '\'' : '"');
				if (quote == c)
					builder.append('\\');
			}
			builder.append(c);
		}
		
		if (quote == '\0')
			quote = '"';
		builder.setCharAt(0, quote);
		builder.append(quote);
		return builder.toString();
	}
	
}
