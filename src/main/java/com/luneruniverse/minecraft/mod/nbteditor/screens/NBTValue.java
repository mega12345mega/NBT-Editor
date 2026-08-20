package com.luneruniverse.minecraft.mod.nbteditor.screens;

import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalItem;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalNBT;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.IdentifierInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVTooltip;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.MVNbtCompoundParent;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.manager.NBTManagers;
import com.luneruniverse.minecraft.mod.nbteditor.screens.nbtfolder.NBTFolder;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.List2D;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.luneruniverse.minecraft.mod.nbteditor.util.StringJsonWriterQuoted;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

public class NBTValue extends List2D.List2DValue {
	
	private static final Identifier BACK = IdentifierInst.of("nbteditor", "textures/nbt/back.png");
	private static final Identifier BYTE = IdentifierInst.of("nbteditor", "textures/nbt/byte.png");
	private static final Identifier SHORT = IdentifierInst.of("nbteditor", "textures/nbt/short.png");
	private static final Identifier INT = IdentifierInst.of("nbteditor", "textures/nbt/int.png");
	private static final Identifier LONG = IdentifierInst.of("nbteditor", "textures/nbt/long.png");
	private static final Identifier FLOAT = IdentifierInst.of("nbteditor", "textures/nbt/float.png");
	private static final Identifier DOUBLE = IdentifierInst.of("nbteditor", "textures/nbt/double.png");
	private static final Identifier NUMBER = IdentifierInst.of("nbteditor", "textures/nbt/number.png");
	private static final Identifier STRING = IdentifierInst.of("nbteditor", "textures/nbt/string.png");
	private static final Identifier LIST = IdentifierInst.of("nbteditor", "textures/nbt/list.png");
	private static final Identifier BYTE_ARRAY = IdentifierInst.of("nbteditor", "textures/nbt/byte_array.png");
	private static final Identifier INT_ARRAY = IdentifierInst.of("nbteditor", "textures/nbt/int_array.png");
	private static final Identifier LONG_ARRAY = IdentifierInst.of("nbteditor", "textures/nbt/long_array.png");
	private static final Identifier COMPOUND = IdentifierInst.of("nbteditor", "textures/nbt/compound.png");
	
	private final NBTEditorScreen<?> screen;
	private final String key;
	private NbtElement value;
	private AbstractNbtList parentList;
	
	private boolean selected;
	private boolean unsafe;
	private boolean invalidComponent;
	
	public NBTValue(NBTEditorScreen<?> screen, String key, NbtElement value, AbstractNbtList parentList) {
		this.screen = screen;
		this.key = key;
		this.value = value;
		this.parentList = parentList;
	}
	public NBTValue(NBTEditorScreen<?> screen, String key, NbtElement value) {
		this(screen, key, value, null);
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		Identifier icon;
		if (key == null) {
			icon = BACK;
		} else {
			icon = switch (value.getType()) {
				case NbtElement.BYTE_TYPE -> BYTE;
				case NbtElement.SHORT_TYPE -> SHORT;
				case NbtElement.INT_TYPE -> INT;
				case NbtElement.LONG_TYPE -> LONG;
				case NbtElement.FLOAT_TYPE -> FLOAT;
				case NbtElement.DOUBLE_TYPE -> DOUBLE;
				case MVNbtCompoundParent.NUMBER_TYPE -> NUMBER;
				case NbtElement.STRING_TYPE -> STRING;
				case NbtElement.LIST_TYPE -> LIST;
				case NbtElement.BYTE_ARRAY_TYPE -> BYTE_ARRAY;
				case NbtElement.INT_ARRAY_TYPE -> INT_ARRAY;
				case NbtElement.LONG_ARRAY_TYPE -> LONG_ARRAY;
				case NbtElement.COMPOUND_TYPE -> COMPOUND;
				default -> null;
			};
		}
		if (icon != null)
			MVDrawableHelper.drawTexture(matrices, icon, 0, 0, 0, 0, 32, 32, 32, 32);
		
		int color = -1;
		String tooltip = null;
		if (unsafe && selected || parentList != null &&
				!MVNbtCompoundParent.NBT_CODE_REFACTORED && parentList.nbte$getHeldType().get() != value.getType()) {
			color = 0xFFFFAA33;
			tooltip = "nbteditor.nbt.marker.unsafe";
		} else if (invalidComponent) {
			color = 0xFF550000;
			tooltip = "nbteditor.nbt.marker.invalid_component";
		} else if (selected)
			color = 0xFFDF4949;
		else if (isHovering(mouseX, mouseY))
			color = 0xFF257789;
		if (color != -1) {
			MVDrawableHelper.fill(matrices, -4, -4, 36, 0, color);
			MVDrawableHelper.fill(matrices, -4, -4, 0, 36, color);
			MVDrawableHelper.fill(matrices, -4, 32, 36, 36, color);
			MVDrawableHelper.fill(matrices, 32, -4, 36, 36, color);
		}
		if (tooltip != null && isHovering(mouseX, mouseY))
			new MVTooltip(tooltip).render(matrices, mouseX, mouseY);
		
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
			
			NBTFolder<?> folder = NBTFolder.get(value);
			screen.selectNbt(this, selected && folder != null && !folder.hasEmptyKey());
			selected = !selected;
			return selected;
		}
		
		selected = false;
		return false;
	}
	
	private boolean isHovering(int mouseX, int mouseY) {
		return isInsideList() && mouseX >= 0 && mouseY >= 0 && mouseX <= 32 && mouseY <= 32;
	}
	
	public String getKey() {
		return key;
	}
	
	public void setValue(NbtElement value) {
		this.value = value;
	}
	public NbtElement getValue() {
		return value;
	}
	public String getValueText(boolean json) {
		return json ? new StringJsonWriterQuoted().apply(value) : value.toString();
	}
	
	public void setUnsafe(boolean unsafe) {
		this.unsafe = unsafe;
	}
	/**
	 * @return Returns if this value has been manually set as unsafe; doesn't take into account list types
	 */
	public boolean isUnsafe() {
		return unsafe;
	}
	
	public void updateInvalidComponent(LocalNBT localNBT, String component) {
		if (!NBTManagers.COMPONENTS_EXIST)
			return;
		if (localNBT instanceof LocalItem localItem) {
			NbtCompound nbtOutput = localItem.getReadableItem().nbte$getNbt();
			if (component == null)
				component = this.key;
			this.invalidComponent = (nbtOutput == null || !nbtOutput.contains(MainUtil.addNamespace(component)));
		}
	}
	public boolean isInvalidComponent() {
		return invalidComponent;
	}
	
}
