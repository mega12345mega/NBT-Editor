package com.luneruniverse.minecraft.mod.nbteditor.screens.factories;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.lwjgl.glfw.GLFW;

import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalItem;
import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.EditableText;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVTooltip;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.screens.LocalEditorScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigValueDropdownEnum;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.AlertWidget;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.FormattedTextFieldWidget;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.GroupWidget;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.ImageToLoreWidget;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.NamedTextFieldWidget;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.TranslatedGroupWidget;
import com.luneruniverse.minecraft.mod.nbteditor.util.Lore.LoreConsumer;
import com.luneruniverse.minecraft.mod.nbteditor.util.TextUtil;

import net.minecraft.client.gui.screen.ingame.BookScreen.Contents;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
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
		return localNBT.getOrCreateNBT().getString("title");
	}
	private void setBookTitle(String title) {
		localNBT.getOrCreateNBT().putString("title", title);
		localNBT.getNBT().remove("filtered_title");
		checkSave();
	}
	
	private String getAuthor() {
		return localNBT.getOrCreateNBT().getString("author");
	}
	private void setAuthor(String author) {
		localNBT.getOrCreateNBT().putString("author", author);
		checkSave();
	}
	
	private Generation getGeneration() {
		int gen = localNBT.getOrCreateNBT().getInt("generation");
		if (gen < 0 || gen >= 4)
			return Generation.TATTERED;
		return Generation.values()[gen];
	}
	private void setGeneration(Generation gen) {
		localNBT.getOrCreateNBT().putInt("generation", gen.ordinal());
		checkSave();
	}
	
	private int getPageCount() {
		return localNBT.getOrCreateNBT().getList("pages", NbtElement.STRING_TYPE).size();
	}
	private Text getPage() {
		return MVMisc.getActualPage(localNBT.getItem(), page);
	}
	private void setPage(Text contents) {
		NbtList pages = localNBT.getOrCreateNBT().getList("pages", NbtElement.STRING_TYPE);
		NbtString nbtContents = NbtString.of(TextInst.toJsonString(contents));
		if (page < pages.size())
			pages.set(page, nbtContents);
		else {
			while (page > pages.size())
				pages.add(NbtString.of("{\"text\":\"\"}"));
			pages.add(nbtContents);
		}
		localNBT.getNBT().put("pages", pages);
		
		if (localNBT.getNBT().contains("filtered_pages", NbtElement.COMPOUND_TYPE))
			localNBT.getNBT().getCompound("filtered_pages").remove(page + "");
		
		checkSave();
	}
	
	private void addPage() {
		NbtList pages = localNBT.getOrCreateNBT().getList("pages", NbtElement.STRING_TYPE);
		if (page < pages.size()) {
			pages.add(page, NbtString.of("{\"text\":\"\"}"));
			checkSave();
			refresh();
		}
	}
	private void removePage() {
		NbtList pages = localNBT.getOrCreateNBT().getList("pages", NbtElement.STRING_TYPE);
		if (page < pages.size()) {
			pages.remove(page);
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
		ItemStack output = localNBT.getItem().copy();
		if (!output.hasNbt())
			return net.minecraft.client.gui.screen.ingame.BookScreen.EMPTY_PROVIDER;
		NbtList pages = output.getNbt().getList("pages", NbtElement.STRING_TYPE);
		List<Text> previewPages = new ArrayList<>();
		for (int i = 0; i < pages.size(); i++)
			previewPages.add(makePreviewText(TextUtil.fromJsonSafely(pages.getString(i))));
		
		return MVMisc.getBookContents(previewPages);
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
				new ConfigValueDropdownEnum<>(getGeneration(), Generation.ORIGINAL, Generation.class)
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
		
		group.addWidget(MVMisc.newButton(16, 64 + 24, 20, height - 80 - 24,
				TextInst.translatable("nbteditor.book.back"), btn -> back())).active = (page > 0);
		group.addWidget(MVMisc.newButton(width - 16 - 20, 64 + 24, 20, height - 80 - 24,
				TextInst.translatable("nbteditor.book.forward"), btn -> forward()));
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
		
		return switch (keyCode) {
			case GLFW.GLFW_KEY_PAGE_UP -> {
				back();
				yield true;
			}
			case GLFW.GLFW_KEY_PAGE_DOWN -> {
				forward();
				yield true;
			}
			default -> false;
		};
	}
	
	@Override
	public void filesDragged(List<Path> paths) {
		AtomicReference<Text> newPage = new AtomicReference<>();
		LoreConsumer loreConsumer = LoreConsumer.createAppendPage(getPage(), newPage::setPlain);
		ImageToLoreWidget.openImportFiles(paths, file -> loreConsumer, () -> contents.setText(newPage.getPlain()));
	}
	
	@Override
	public void removed() {
		MVMisc.setKeyboardRepeatEvents(false);
	}
	
}
