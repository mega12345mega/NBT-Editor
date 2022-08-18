package com.luneruniverse.minecraft.mod.nbteditor.screens;

import org.lwjgl.glfw.GLFW;

import com.luneruniverse.minecraft.mod.nbteditor.util.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public abstract class ItemEditorScreen extends Screen {
	
	protected final ItemReference ref;
	protected ItemStack item;
	protected ItemStack savedItem;
	private boolean saved;
	
	protected NamedTextFieldWidget name;
	private ButtonWidget saveBtn;
	
	protected ItemEditorScreen(Text title, ItemReference ref) {
		super(title);
		this.ref = ref;
		this.savedItem = ref.getItem().copy();
		this.item = savedItem.copy();
		this.saved = true;
	}
	
	protected boolean isNameEditable() {
		return false;
	}
	
	
	@Override
	protected final void init() {
		name = new NamedTextFieldWidget(textRenderer, 16 + (32 + 8) * 2, 16 + 8, 100, 16, Text.of("")) {
			@Override
			public boolean mouseClicked(double mouseX, double mouseY, int button) {
				if (isNameEditable())
					return super.mouseClicked(mouseX, mouseY, button);
				else
					return false;
			}
		}.name(Text.translatable("nbteditor.name"));
		name.setMaxLength(Integer.MAX_VALUE);
		name.setText(MainUtil.getItemNameSafely(item).getString());
		name.setEditable(isNameEditable());
		addDrawableChild(name);
		
		saveBtn = addDrawableChild(new ButtonWidget(16 + (32 + 8) * 2 + 100 + 8, 16 + 6, 100, 20, Text.translatable("nbteditor.save"), btn -> {
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
	
	public void setSaved(boolean saved) {
		this.saved = saved;
		if (saveBtn != null)
			saveBtn.active = !saved;
	}
	public boolean isSaved() {
		return saved;
	}
	protected void save() {
		savedItem = item.copy();
		saveBtn.setMessage(Text.translatable("nbteditor.saving"));
		setSaved(true);
		ref.saveItem(savedItem, () -> {
			saveBtn.setMessage(Text.translatable("nbteditor.save"));
		});
	}
	protected void checkSave() {
		item.getOrCreateNbt(); // Make sure both items have NBT defined, so no NBT and empty NBT comes out equal
		savedItem.getOrCreateNbt();
		boolean saved = ItemStack.areEqual(item, savedItem);
		if (saved != this.saved)
			setSaved(saved);
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
			}, Text.translatable("nbteditor.notsaved.title"), Text.translatable("nbteditor.notsaved.message"),
					Text.translatable("nbteditor.notsaved.yes"), Text.translatable("nbteditor.notsaved.no")));
		}
	}
	
}
