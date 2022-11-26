package com.luneruniverse.minecraft.mod.nbteditor.util;

import java.util.ArrayList;
import java.util.Collections;

import com.google.common.collect.Lists;
import com.luneruniverse.minecraft.mod.nbteditor.mixin.source.StringNbtWriterAccessor;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.visitor.StringNbtWriter;

public class StringNbtWriterQuoted extends StringNbtWriter {
	
	@Override
	public void visitString(NbtString element) {
		((StringNbtWriterAccessor) this).getResult().append(ConfigScreen.isSingleQuotesAllowed() ? NbtString.escape(element.asString()) : escape(element.asString()));
	}
	
    @Override
    public void visitList(NbtList element) {
		StringBuilder result = ((StringNbtWriterAccessor) this).getResult();
		
        result.append('[');
        for (int i = 0; i < element.size(); ++i) {
            if (i != 0) {
                result.append(',');
            }
            result.append(new StringNbtWriterQuoted().apply(element.get(i)));
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
            result.append(escapeNameWithQuotes(string)).append(':').append(new StringNbtWriterQuoted().apply(compound.get(string)));
        }
        result.append('}');
	}
	
	private String escapeNameWithQuotes(String str) {
		String output = escapeName(str);
		if (output.startsWith("\"") || output.startsWith("\'"))
			return output;
		return "\"" + output + "\"";
	}
	
	protected static String escapeName(String str) {
		String superEsc = StringNbtWriter.escapeName(str);
		
		if (ConfigScreen.isSingleQuotesAllowed() || superEsc.equals(str))
			return superEsc;
		else
			return escape(str);
	}
	
	// From NbtString.escape
	// Edited to always use double quotes
	private static String escape(String value) {
		StringBuilder stringBuilder = new StringBuilder(" ");
		int c = 34; // Force using double quotes
		for (int i = 0; i < value.length(); ++i) {
			int d = value.charAt(i);
			if (d == 92) {
				stringBuilder.append('\\');
			} else if (d == 34 || d == 39) {
				if (c == 0) {
					c = d == 34 ? 39 : 34;
				}
				if (c == d) {
					stringBuilder.append('\\');
				}
			}
			stringBuilder.append((char) d);
		}
		if (c == 0) {
			c = 34;
		}
		stringBuilder.setCharAt(0, (char) c);
		stringBuilder.append((char) c);
		return stringBuilder.toString();
	}
	
}
