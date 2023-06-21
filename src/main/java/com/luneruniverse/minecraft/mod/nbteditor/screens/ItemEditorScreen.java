package com.luneruniverse.minecraft.mod.nbteditor.screens;

import java.util.function.Function;

import org.lwjgl.glfw.GLFW;

import com.luneruniverse.minecraft.mod.nbteditor.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionTooltip;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.screens.factories.ItemFactoryScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.util.FancyConfirmScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.NamedTextFieldWidget;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public abstract class ItemEditorScreen extends OverlaySupportingScreen {
	
	protected static record FactoryLink(String langName, Function<ItemReference, Screen> factory) {
		public FactoryLink(String langName, Function<ItemReference, Screen> factory) {
			this.langName = langName;
			this.factory = factory;
		}
	}
	
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
	
	protected boolean isSaveRequried() {
		return true;
	}
	
	protected FactoryLink getFactoryLink() {
		return new FactoryLink("nbteditor.item_factory", ItemFactoryScreen::new);
	}
	
	
	@Override
	protected final void init() {
		super.init();
		
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
		
		if (isSaveRequried()) {
			saveBtn = addDrawableChild(MultiVersionMisc.newButton(16 + (32 + 8) * 2 + 100 + 8, 16 + 6, 100, 20, TextInst.translatable("nbteditor.editor.save"), btn -> {
				save();
			}));
			saveBtn.active = !saved;
		}
		
		FactoryLink link = getFactoryLink();
		if (link != null) {
			addDrawableChild(MultiVersionMisc.newTexturedButton(width - 36, 22, 20, 20, 20,
					ItemFactoryScreen.FACTORY_ICON,
					btn -> closeSafely(() -> client.setScreen(link.factory().apply(ref))),
					new MultiVersionTooltip(link.langName())));
		}
		
		initEditor();
	}
	protected void initEditor() {}
	
	@Override
	public final void renderMain(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		super.renderBackground(matrices);
		preRenderEditor(matrices, mouseX, mouseY, delta);
		super.renderMain(matrices, mouseX, mouseY, delta);
		renderEditor(matrices, mouseX, mouseY, delta);
		MainUtil.renderLogo(matrices);
		renderItemPreview(matrices);
	}
	private void renderItemPreview(MatrixStack matrices) {
		int x = 16 + 32 + 8;
		int y = 16;
		int scaleX = 2;
		int scaleY = 2;
		
		x /= scaleX;
		y /= scaleY;
		
		boolean oldMatrix = switch (Version.get()) {
			case v1_19_4 -> false;
			case v1_19_3, v1_19, v1_18_v1_17 -> true;
		};
		if (oldMatrix)
			matrices = RenderSystem.getModelViewStack();
		
		matrices.push();
		matrices.translate(0.0D, 0.0D, 32.0D);
		matrices.scale(scaleX, scaleY, 1);
		if (oldMatrix)
			RenderSystem.applyModelViewMatrix();
		
		MultiVersionMisc.renderItem(matrices, 200.0F, true, item, x, y);
		
		matrices.pop();
		if (oldMatrix)
			RenderSystem.applyModelViewMatrix();
	}
	protected void preRenderEditor(MatrixStack matrices, int mouseX, int mouseY, float delta) {}
	protected void renderEditor(MatrixStack matrices, int mouseX, int mouseY, float delta) {}
	
	protected void renderTip(MatrixStack matrices, String langHint) {
		if (!ConfigScreen.isKeybindsHidden()) {
			int x = 16 + (32 + 8) * 2 + (100 + 8) * 2;
			MainUtil.drawWrappingString(matrices, textRenderer, TextInst.translatable(langHint).getString(),
					16 + (32 + 8) * 2 + (100 + 8) * 2, 16 + 6 + 10, width - x - 8 - 20 - 8, -1, false, true);
		}
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (super.keyPressed(keyCode, scanCode, modifiers))
			return true;
		
		if (hasControlDown() && !hasShiftDown() && !hasAltDown() && keyCode == GLFW.GLFW_KEY_S) {
			save();
			return true;
		}
		
		return name.keyPressed(keyCode, scanCode, modifiers);
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
		closeSafely(() -> ref.showParent());
	}
	
	public void closeSafely(Runnable onClose) {
		if (saved)
			onClose.run();
		else {
			client.setScreen(new FancyConfirmScreen(value -> {
				if (value)
					save();
				
				onClose.run();
			}, TextInst.translatable("nbteditor.editor.unsaved.title"), TextInst.translatable("nbteditor.editor.unsaved.desc"),
					TextInst.translatable("nbteditor.editor.unsaved.yes"), TextInst.translatable("nbteditor.editor.unsaved.no")));
		}
	}
	
}
