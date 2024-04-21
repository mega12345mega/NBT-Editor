package com.luneruniverse.minecraft.mod.nbteditor.screens.factories;

import java.nio.file.Path;
import java.util.List;

import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalItem;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.screens.LocalEditorScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.FormattedTextFieldWidget;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.ImageToLoreWidget;
import com.luneruniverse.minecraft.mod.nbteditor.util.Lore;
import com.luneruniverse.minecraft.mod.nbteditor.util.Lore.LoreConsumer;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;

public class DisplayScreen extends LocalEditorScreen<LocalItem, ItemReference> {
	
	private FormattedTextFieldWidget name;
	private FormattedTextFieldWidget lore;
	
	public DisplayScreen(ItemReference ref) {
		super(TextInst.of("Display"), ref);
	}
	
	@Override
	protected void initEditor() {
		MVMisc.setKeyboardRepeatEvents(true);
		
		name = FormattedTextFieldWidget.create(name, 16, 64, width - 32, 24 + textRenderer.fontHeight * 3, localNBT.getItem().getName(),
				false, Style.EMPTY.withFormatting(Formatting.ITALIC, localNBT.getItem().getRarity().formatting), text -> {
			localNBT.getItem().setCustomName(text);
			checkSave();
		}).setOverscroll(false);
		int loreY = 64 + 24 + textRenderer.fontHeight * 3 + 4;
		lore = FormattedTextFieldWidget.create(lore, 16, loreY, width - 32, height - 16 - 20 - 4 - loreY,
				new Lore(localNBT.getItem()).getLore(), Style.EMPTY.withFormatting(Formatting.ITALIC, Formatting.DARK_PURPLE), lines -> {
			if (lines.size() == 1 && lines.get(0).getString().isEmpty())
				new Lore(localNBT.getItem()).clearLore();
			else
				new Lore(localNBT.getItem()).setAllLines(lines);
			checkSave();
		});
		addSelectableChild(name);
		addSelectableChild(lore);
		addDrawableChild(MVMisc.newButton(16, height - 16 - 20, 100, 20, TextInst.translatable("nbteditor.hide_flags"),
				btn -> closeSafely(() -> client.setScreen(new HideFlagsScreen(ref)))));
		addDrawable(lore);
	}
	
	@Override
	protected void renderEditor(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		matrices.push();
		matrices.translate(0.0, 0.0, 1.0);
		name.render(matrices, mouseX, mouseY, delta);
		matrices.pop();
		
		renderTip(matrices, "nbteditor.formatted_text.tip");
	}
	
	@Override
	public void filesDragged(List<Path> paths) {
		LoreConsumer loreConsumer = LoreConsumer.createAppend(localNBT.getItem());
		ImageToLoreWidget.openImportFiles(paths, file -> loreConsumer, () -> lore.setText(new Lore(localNBT.getItem()).getLore()));
	}
	
	@Override
	public void removed() {
		MVMisc.setKeyboardRepeatEvents(false);
	}
	
}
