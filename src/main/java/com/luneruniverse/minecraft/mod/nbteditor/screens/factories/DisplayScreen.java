package com.luneruniverse.minecraft.mod.nbteditor.screens.factories;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalBlock;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalEntity;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalItem;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalNBT;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.NBTReference;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.screens.LocalEditorScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.FormattedTextFieldWidget;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.ImageToLoreWidget;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.EntityTagReferences;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.ItemTagReferences;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class DisplayScreen<L extends LocalNBT> extends LocalEditorScreen<L> {
	
	private FormattedTextFieldWidget name;
	private FormattedTextFieldWidget lore;
	
	public DisplayScreen(NBTReference<L> ref) {
		super(TextInst.of("Display"), ref);
	}
	
	@Override
	protected void initEditor() {
		MVMisc.setKeyboardRepeatEvents(true);
		
		Style baseNameStyle = Style.EMPTY;
		if (localNBT instanceof LocalItem item)
			baseNameStyle = baseNameStyle.withFormatting(Formatting.ITALIC, item.getItem().getRarity().formatting);
		else if (localNBT instanceof LocalBlock)
			;
		else if (localNBT instanceof LocalEntity)
			baseNameStyle = baseNameStyle.withFormatting(Formatting.WHITE);
		else
			throw new IllegalStateException("DisplayScreen doesn't support " + localNBT.getClass().getName());
		
		name = FormattedTextFieldWidget.create(name, 16, 64, width - 32, 24 + textRenderer.fontHeight * 3,
				localNBT.getName(), false, baseNameStyle, text -> {
			localNBT.setName(text);
			checkSave();
		}).setOverscroll(false).setShadow(localNBT instanceof LocalItem);
		
		int nextY = 64 + 24 + textRenderer.fontHeight * 3 + 4;
		
		if (localNBT instanceof LocalItem item) {
			lore = FormattedTextFieldWidget.create(lore, 16, nextY, width - 32, height - 16 - 20 - 4 - nextY,
					ItemTagReferences.LORE.get(item.getItem()), Style.EMPTY.withFormatting(Formatting.ITALIC, Formatting.DARK_PURPLE), lines -> {
				if (lines.size() == 1 && lines.get(0).getString().isEmpty())
					ItemTagReferences.LORE.set(item.getItem(), new ArrayList<>());
				else
					ItemTagReferences.LORE.set(item.getItem(), lines);
				checkSave();
			});
			addSelectableChild(name);
			addSelectableChild(lore);
			addDrawableChild(MVMisc.newButton(16, height - 16 - 20, 100, 20, TextInst.translatable("nbteditor.hide_flags"),
					btn -> closeSafely(() -> client.setScreen(new HideFlagsScreen((ItemReference) ref)))));
			addDrawable(lore);
		} else
			addSelectableChild(name);
		
		if (localNBT instanceof LocalEntity entity) {
			addDrawableChild(MVMisc.newButton(16, nextY, 150, 20,
					TextInst.translatable("nbteditor.display.custom_name_visible." +
							(EntityTagReferences.CUSTOM_NAME_VISIBLE.get(entity) ? "enabled" : "disabled")), btn -> {
				boolean customNameVisible = !EntityTagReferences.CUSTOM_NAME_VISIBLE.get(entity);
				EntityTagReferences.CUSTOM_NAME_VISIBLE.set(entity, customNameVisible);
				btn.setMessage(TextInst.translatable("nbteditor.display.custom_name_visible." + (customNameVisible ? "enabled" : "disabled")));
				checkSave();
			}));
		}
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
		if (!(localNBT instanceof LocalItem item))
			return;
		List<Text> lines = new ArrayList<>();
		lines.add(lore.getText());
		ImageToLoreWidget.openImportFiles(paths, (file, imgLines) -> lines.addAll(imgLines), () -> {
			if (lines.size() > 1)
				lore.setText(lines);
		});
	}
	
	@Override
	public void removed() {
		MVMisc.setKeyboardRepeatEvents(false);
	}
	
}
