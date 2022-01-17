package com.luneruniverse.minecraft.mod.nbteditor.util;

import java.util.ArrayList;
import java.util.Collections;

import com.google.common.collect.Lists;
import com.luneruniverse.minecraft.mod.nbteditor.mixin.source.StringNbtWriterAccessor;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.visitor.StringNbtWriter;

public class StringNbtWriterQuoted extends StringNbtWriter {
	
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
		if (output.startsWith("\""))
			return output;
		return "\"" + output + "\"";
	}
	
}
