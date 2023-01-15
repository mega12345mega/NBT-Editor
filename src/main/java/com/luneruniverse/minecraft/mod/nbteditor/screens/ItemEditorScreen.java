package com.luneruniverse.minecraft.mod.nbteditor.screens;

import org.lwjgl.glfw.GLFW;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionScreen;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.util.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public abstract class ItemEditorScreen extends MultiVersionScreen {
	
	protected final ItemReference ref;
	protected ItemStack item;
	protected ItemStack savedItem;
	private boolean saved;
	
	protected NamedTextFieldWidget name;
	private ButtonWidget saveBtn;
	
	protected ItemEditorScreen(Text title, ItemReference ref) {
		super(title);
		this.ref = ref;
		this.savedItem = MainUtil.copyAirable(ref.getItem());
		this.item = MainUtil.copyAirable(savedItem);
		this.saved = true;
	}
	
	protected boolean isNameEditable() {
		return false;
	}
	
	
	@Override
	protected final void init() {
		name = new NamedTextFieldWidget(textRenderer, 16 + (32 + 8) * 2, 16 + 8, 100, 16, TextInst.of("")) {
			@Override
			public boolean mouseClicked(double mouseX, double mouseY, int button) {
				if (isNameEditable())
					return super.mouseClicked(mouseX, mouseY, button);
				else
					return false;
			}
		}.name(TextInst.translatable("nbteditor.editor.name"));
		name.setMaxLength(Integer.MAX_VALUE);
		name.setText(MainUtil.getItemNameSafely(item).getString());
		name.setEditable(isNameEditable());
		addDrawableChild(name);
		
		saveBtn = addDrawableChild(MultiVersionMisc.newButton(16 + (32 + 8) * 2 + 100 + 8, 16 + 6, 100, 20, TextInst.translatable("nbteditor.editor.save"), btn -> {
			save();
		}));
		saveBtn.active = !saved;
		
		initEditor();
	}
	protected void initEditor() {}
	
	@Override
	public final void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		super.renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
		renderEditor(matrices, mouseX, mouseY, delta);
		MainUtil.renderLogo(matrices);
		MainUtil.renderItem(item);
	}
	protected void renderEditor(MatrixStack matrices, int mouseX, int mouseY, float delta) {}
	
	@Override
	public void tick() {
		name.tick();
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			close();
			return true;
		}
		
		if ((modifiers & GLFW.GLFW_MOD_CONTROL) != 0 && keyCode == GLFW.GLFW_KEY_S) {
			save();
			return true;
		}
		
		return name.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
	}
	
	protected void setSaved(boolean saved) {
		this.saved = saved;
		if (saveBtn != null)
			saveBtn.active = !saved;
	}
	public boolean isSaved() {
		return saved;
	}
	protected void save() {
		savedItem = item.copy();
		saveBtn.setMessage(TextInst.translatable("nbteditor.editor.saving"));
		setSaved(true);
		ref.saveItem(savedItem, () -> {
			saveBtn.setMessage(TextInst.translatable("nbteditor.editor.save"));
		});
	}
	protected void checkSave() {
		item.getOrCreateNbt(); // Make sure both items have NBT defined, so no NBT and empty NBT comes out equal
		savedItem.getOrCreateNbt();
		setSaved(ItemStack.areEqual(item, savedItem));
	}
	
	@Override
	public void close() {
		if (saved)
			ref.showParent();
		else {
			client.setScreen(new FancyConfirmScreen(value -> {
				if (value)
					save();
				
				ref.showParent();
			}, TextInst.translatable("nbteditor.editor.unsaved.title"), TextInst.translatable("nbteditor.editor.unsaved.desc"),
					TextInst.translatable("nbteditor.editor.unsaved.yes"), TextInst.translatable("nbteditor.editor.unsaved.no")));
		}
	}
	
}
