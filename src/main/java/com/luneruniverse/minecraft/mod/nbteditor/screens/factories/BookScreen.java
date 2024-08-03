package com.luneruniverse.minecraft.mod.nbteditor.screens.factories;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalItem;
import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.EditableText;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVTooltip;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.LocalEditorScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigValueDropdown;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.AlertWidget;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.FormattedTextFieldWidget;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.GroupWidget;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.ImageToLoreWidget;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.NamedTextFieldWidget;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.TranslatedGroupWidget;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.WrittenBookTagReferences;

import net.minecraft.client.gui.screen.ingame.BookScreen.Contents;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class BookScreen extends LocalEditorScreen<LocalItem> {
	
	private enum Generation {
		ORIGINAL,
		COPY_OF_ORIGINAL,
		COPY_OF_COPY,
		TATTERED;
		
		@Override
		public String toString() {
			return TextInst.translatable("book.generation." + ordinal()).getString();
		}
	}
	
	private int page;
	private GroupWidget group;
	private NamedTextFieldWidget title;
	private NamedTextFieldWidget author;
	private GroupWidget gen;
	private FormattedTextFieldWidget contents;
	
	public BookScreen(ItemReference ref, int page) {
		super(TextInst.of("Book"), ref);
		this.page = page;
	}
	public BookScreen(ItemReference ref) {
		this(ref, 0);
	}
	
	private String getBookTitle() {
		return WrittenBookTagReferences.TITLE.get(localNBT.getEditableItem());
	}
	private void setBookTitle(String title) {
		WrittenBookTagReferences.TITLE.set(localNBT.getEditableItem(), title);
		checkSave();
	}
	
	private String getAuthor() {
		return WrittenBookTagReferences.AUTHOR.get(localNBT.getEditableItem());
	}
	private void setAuthor(String author) {
		WrittenBookTagReferences.AUTHOR.set(localNBT.getEditableItem(), author);
		checkSave();
	}
	
	private Generation getGeneration() {
		int gen = WrittenBookTagReferences.GENERATION.get(localNBT.getEditableItem());
		if (gen < 0 || gen >= 4)
			return Generation.TATTERED;
		return Generation.values()[gen];
	}
	private void setGeneration(Generation gen) {
		WrittenBookTagReferences.GENERATION.set(localNBT.getEditableItem(), gen.ordinal());
		checkSave();
	}
	
	private int getPageCount() {
		return WrittenBookTagReferences.PAGES.get(localNBT.getEditableItem()).size();
	}
	private Text getPage() {
		List<Text> pages = WrittenBookTagReferences.PAGES.get(localNBT.getEditableItem());
		return page < pages.size() ? pages.get(page) : TextInst.of("");
	}
	private void setPage(Text contents) {
		List<Text> pages = WrittenBookTagReferences.PAGES.get(localNBT.getEditableItem());
		if (page < pages.size())
			pages.set(page, contents);
		else {
			while (page > pages.size())
				pages.add(TextInst.of(""));
			pages.add(contents);
		}
		WrittenBookTagReferences.PAGES.set(localNBT.getEditableItem(), pages);
		
		checkSave();
	}
	
	private void addPage() {
		List<Text> pages = WrittenBookTagReferences.PAGES.get(localNBT.getEditableItem());
		if (page < pages.size()) {
			pages.add(page, TextInst.of(""));
			WrittenBookTagReferences.PAGES.set(localNBT.getEditableItem(), pages);
			checkSave();
			refresh();
		}
	}
	private void removePage() {
		List<Text> pages = WrittenBookTagReferences.PAGES.get(localNBT.getEditableItem());
		if (page < pages.size()) {
			pages.remove(page);
			WrittenBookTagReferences.PAGES.set(localNBT.getEditableItem(), pages);
			checkSave();
			refresh();
		}
	}
	
	private void back() {
		if (page > 0) {
			page--;
			refresh();
		}
	}
	private void forward() {
		page++;
		refresh();
	}
	
	private void refresh() {
		contents = null;
		clearChildren();
		init();
	}
	
	private Contents getPreviewItem() {
		List<Text> pages = WrittenBookTagReferences.PAGES.get(localNBT.getEditableItem());
		pages.replaceAll(this::makePreviewText);
		return MVMisc.getBookContents(pages);
	}
	private Text makePreviewText(Text text) {
		EditableText output = TextInst.copy(text);
		output.setStyle(makePreviewStyle(output.getStyle()));
		output.getSiblings().replaceAll(this::makePreviewText);
		return output;
	}
	private Style makePreviewStyle(Style style) {
		if (style.getClickEvent() == null)
			return style;
		return MixinLink.withRunClickEvent(style, () -> {
			net.minecraft.client.gui.screen.ingame.BookScreen preview = getOverlay();
			setOverlay(new AlertWidget(
					() -> setOverlayScreen(preview, 200),
					TextInst.translatable("nbteditor.book.preview.click.title"),
					TextInst.of(""),
					TextInst.translatable("nbteditor.book.preview.click.action",
							MVMisc.getClickEventActionName(style.getClickEvent().getAction())),
					TextInst.of(""),
					TextInst.translatable("nbteditor.book.preview.click.value", style.getClickEvent().getValue())), 200);
		});
	}
	
	@Override
	protected void initEditor() {
		MVMisc.setKeyboardRepeatEvents(true);
		
		group = new GroupWidget();
		addDrawableChild(group);
		
		title = group.addWidget(new NamedTextFieldWidget(16, 64 + 2, 100, 16)
				.name(TextInst.translatable("nbteditor.book.title")));
		title.setMaxLength(32);
		title.setText(getBookTitle());
		title.setChangedListener(this::setBookTitle);
		
		author = group.addWidget(new NamedTextFieldWidget(16 + 108, 64 + 2, 100, 16)
				.name(TextInst.translatable("nbteditor.book.author")));
		author.setMaxLength(Integer.MAX_VALUE);
		author.setText(getAuthor());
		author.setChangedListener(this::setAuthor);
		
		gen = group.addElement(TranslatedGroupWidget.forWidget(
				ConfigValueDropdown.forEnum(getGeneration(), Generation.ORIGINAL, Generation.class)
						.addValueListener(value -> setGeneration(value.getValidValue())), 16 + 108 * 2, 64, 0));
		
		group.addWidget(MVMisc.newButton(16 + 108 * 3 - 4, 64, 20, 20,
				TextInst.translatable("nbteditor.book.add"), btn -> addPage()));
		group.addWidget(MVMisc.newButton(16 + 108 * 3 + 20, 64, 20, 20,
				TextInst.translatable("nbteditor.book.remove"), btn -> removePage()));
		group.addWidget(MVMisc.newButton(16 + 108 * 3 + 44, 64, 20, 20,
				TextInst.translatable("nbteditor.book.preview.icon"),
				btn -> {
					net.minecraft.client.gui.screen.ingame.BookScreen preview =
							new net.minecraft.client.gui.screen.ingame.BookScreen(getPreviewItem()) {
						@Override
						public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
							if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
								setOverlay(null);
								return true;
							}
							return super.keyPressed(keyCode, scanCode, modifiers);
						}
					};
					setOverlayScreen(preview, 200);
					preview.setPage(page);
				},
				new MVTooltip("nbteditor.book.preview")));
		
		contents = group.addWidget(FormattedTextFieldWidget.create(contents, 16 + 24, 64 + 24, width - 32 - 24 * 2,
				height - 80 - 24, getPage(), true, Style.EMPTY.withColor(Formatting.BLACK), this::setPage));
		contents.setBackgroundColor(0xFFFDF8EB);
		contents.setCursorColor(0xFF000000);
		contents.setSelectionColor(0x55000000);
		contents.setShadow(false);
		
		group.addDrawable(gen);
		
		EditableText prevKeybind = TextInst.translatable("nbteditor.keybind.page.down");
		EditableText nextKeybind = TextInst.translatable("nbteditor.keybind.page.up");
		if (ConfigScreen.isInvertedPageKeybinds()) {
			EditableText temp = prevKeybind;
			prevKeybind = nextKeybind;
			nextKeybind = temp;
		}
		
		group.addWidget(MVMisc.newButton(16, 64 + 24, 20, height - 80 - 24,
				TextInst.translatable("nbteditor.book.back"), btn -> back(),
				ConfigScreen.isKeybindsHidden() ? null : new MVTooltip(TextInst.literal("")
						.append(prevKeybind).append(TextInst.translatable("nbteditor.keybind.page.prev")))))
				.active = (page > 0);
		group.addWidget(MVMisc.newButton(width - 16 - 20, 64 + 24, 20, height - 80 - 24,
				TextInst.translatable("nbteditor.book.forward"), btn -> forward(),
				ConfigScreen.isKeybindsHidden() ? null : new MVTooltip(TextInst.literal("")
						.append(nextKeybind).append(TextInst.translatable("nbteditor.keybind.page.next")))));
	}
	
	@Override
	protected void renderEditor(MatrixStack matrices, int fdf8eb, int mouseY, float delta) {
		MVDrawableHelper.drawTextWithShadow(matrices, textRenderer, TextInst.translatable("nbteditor.book.page", page + 1, getPageCount()),
				16 + 108 * 3 - 4 + 24 * 3, 64 + 10 - textRenderer.fontHeight / 2, -1);
		renderTip(matrices, "nbteditor.formatted_text.tip");
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (super.keyPressed(keyCode, scanCode, modifiers))
			return true;
		
		if (keyCode == GLFW.GLFW_KEY_PAGE_UP || keyCode == GLFW.GLFW_KEY_PAGE_DOWN) {
			boolean prev = (keyCode == GLFW.GLFW_KEY_PAGE_DOWN);
			if (ConfigScreen.isInvertedPageKeybinds())
				prev = !prev;
			if (prev)
				back();
			else
				forward();
			return true;
		}
		
		return false;
	}
	
	@Override
	public void filesDragged(List<Path> paths) {
		List<Text> lines = new ArrayList<>();
		lines.add(getPage());
		ImageToLoreWidget.openImportFiles(paths, (file, imgLines) -> lines.addAll(imgLines), () -> {
			if (lines.size() > 1)
				contents.setText(lines);
		});
	}
	
	@Override
	public void removed() {
		MVMisc.setKeyboardRepeatEvents(false);
	}
	
}
