package com.luneruniverse.minecraft.mod.nbteditor.screens;

import java.util.function.Consumer;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.screens.nbtmenugenerators.MenuGenerator;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.List2D;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.command.argument.NbtElementArgumentType;
import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

public class NBTValue extends List2D.List2DValue {
	
	private static final Identifier BACK = new Identifier("nbteditor", "textures/nbt/back.png");
	private static final Identifier BYTE = new Identifier("nbteditor", "textures/nbt/byte.png");
	private static final Identifier SHORT = new Identifier("nbteditor", "textures/nbt/short.png");
	private static final Identifier INT = new Identifier("nbteditor", "textures/nbt/int.png");
	private static final Identifier LONG = new Identifier("nbteditor", "textures/nbt/long.png");
	private static final Identifier FLOAT = new Identifier("nbteditor", "textures/nbt/float.png");
	private static final Identifier DOUBLE = new Identifier("nbteditor", "textures/nbt/double.png");
	private static final Identifier NUMBER = new Identifier("nbteditor", "textures/nbt/number.png");
	private static final Identifier STRING = new Identifier("nbteditor", "textures/nbt/string.png");
	private static final Identifier LIST = new Identifier("nbteditor", "textures/nbt/list.png");
	private static final Identifier BYTE_ARRAY = new Identifier("nbteditor", "textures/nbt/byte_array.png");
	private static final Identifier INT_ARRAY = new Identifier("nbteditor", "textures/nbt/int_array.png");
	private static final Identifier LONG_ARRAY = new Identifier("nbteditor", "textures/nbt/long_array.png");
	private static final Identifier COMPOUND = new Identifier("nbteditor", "textures/nbt/compound.png");
	
	
	
	
	private final NBTEditorScreen screen;
	private final String key;
	private NbtElement value;
	private AbstractNbtList<?> parentList;
	
	private boolean selected;
	private boolean unsafe;
	
	public NBTValue(NBTEditorScreen screen, String key, NbtElement value, AbstractNbtList<?> parentList) {
		this.screen = screen;
		this.key = key;
		this.value = value;
		this.parentList = parentList;
	}
	public NBTValue(NBTEditorScreen screen, String key, NbtElement value) {
		this(screen, key, value, null);
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		Identifier icon = null;
		if (key == null)
			icon = BACK;
		else if (value.getType() == NbtElement.BYTE_TYPE)
			icon = BYTE;
		else if (value.getType() == NbtElement.SHORT_TYPE)
			icon = SHORT;
		else if (value.getType() == NbtElement.INT_TYPE)
			icon = INT;
		else if (value.getType() == NbtElement.LONG_TYPE)
			icon = LONG;
		else if (value.getType() == NbtElement.FLOAT_TYPE)
			icon = FLOAT;
		else if (value.getType() == NbtElement.DOUBLE_TYPE)
			icon = DOUBLE;
		else if (value.getType() == NbtElement.NUMBER_TYPE)
			icon = NUMBER;
		else if (value.getType() == NbtElement.STRING_TYPE)
			icon = STRING;
		else if (value.getType() == NbtElement.LIST_TYPE)
			icon = LIST;
		else if (value.getType() == NbtElement.BYTE_ARRAY_TYPE)
			icon = BYTE_ARRAY;
		else if (value.getType() == NbtElement.INT_ARRAY_TYPE)
			icon = INT_ARRAY;
		else if (value.getType() == NbtElement.LONG_ARRAY_TYPE)
			icon = LONG_ARRAY;
		else if (value.getType() == NbtElement.COMPOUND_TYPE)
			icon = COMPOUND;
		if (icon != null)
			MultiVersionDrawableHelper.drawTexture(matrices, icon, 0, 0, 0, 0, 32, 32, 32, 32);
		
		int color = -1;
		if (unsafe && selected || parentList != null && parentList.getHeldType() != value.getType())
			color = 0xFFFFAA33;
		else if (selected)
			color = 0xFFDF4949;
		else if (isHovering(mouseX, mouseY))
			color = 0xFF257789;
		if (color != -1) {
			MultiVersionDrawableHelper.fill(matrices, -4, -4, 36, 0, color);
			MultiVersionDrawableHelper.fill(matrices, -4, -4, 0, 36, color);
			MultiVersionDrawableHelper.fill(matrices, -4, 32, 36, 36, color);
			MultiVersionDrawableHelper.fill(matrices, 32, -4, 36, 36, color);
		}
		
		if (key == null)
			return;
		
		matrices.push();
		matrices.scale((float) ConfigScreen.getKeyTextSize(), (float) ConfigScreen.getKeyTextSize(), 0);
		double scale = 1 / ConfigScreen.getKeyTextSize();
		MainUtil.drawWrappingString(matrices, textRenderer, key, (int) (16 * scale), (int) (24 * scale), (int) (32 * scale), -1, true, true);
		matrices.pop();
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (isHovering((int) mouseX, (int) mouseY)) {
			if (key == null) {
				screen.selectNbt(null, true);
				return true;
			}
			
			MenuGenerator menuGen = MenuGenerator.TYPES.get(value.getType());
			screen.selectNbt(this, selected && menuGen != null && !menuGen.hasEmptyKey(screen, value));
			selected = !selected;
			return selected;
		}
		
		selected = false;
		return false;
	}
	
	private boolean isHovering(int mouseX, int mouseY) {
		return isInsideList() && mouseX >= 0 && mouseY >= 0 && mouseX <= 32 && mouseY <= 32;
	}
	
	public void valueChanged(String str, Consumer<NbtElement> onChange) {
		try {
			value = NbtElementArgumentType.nbtElement().parse(new StringReader(str));
			onChange.accept(value);
		} catch (CommandSyntaxException e) {}
	}
	
	public String getKey() {
		return key;
	}
	public String getValueText() {
		return value.toString();
	}
	
	public void setUnsafe(boolean unsafe) {
		this.unsafe = unsafe;
	}
	/**
	 * 
	 * @return Returns if this value has been manually set as unsafe; doesn't take into account list types
	 */
	public boolean isUnsafe() {
		return unsafe;
	}
	
}
