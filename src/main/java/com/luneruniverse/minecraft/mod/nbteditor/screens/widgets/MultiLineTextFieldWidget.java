package com.luneruniverse.minecraft.mod.nbteditor.screens.widgets;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.lwjgl.glfw.GLFW;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionDrawable;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionElement;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionTooltip;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.OverlaySupportingScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.Tickable;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

public class MultiLineTextFieldWidget implements MultiVersionDrawable, MultiVersionElement, Tickable, Selectable {
	
	private class FindAndReplaceWidget extends TranslatedGroupWidget {
		private static String findValue = "";
		private static String replaceValue = "";
		private static boolean regex = false;
		
		private final NamedTextFieldWidget find;
		private final NamedTextFieldWidget replace;
		private final ButtonWidget regexBtn;
		private boolean dragging;
		private Matcher lastRegexMatch;
		
		public FindAndReplaceWidget() {
			super(MainUtil.client.getWindow().getScaledWidth() / 2 - 100,
					MainUtil.client.getWindow().getScaledHeight() / 2 - 30, 200);
			find = addWidget(new NamedTextFieldWidget(textRenderer, 0, 0, 176, 16, TextInst.of(""))
					.name(TextInst.translatable("nbteditor.multi_line_text.find")));
			replace = addWidget(new NamedTextFieldWidget(textRenderer, 0, 20, 200, 16, TextInst.of(""))
					.name(TextInst.translatable("nbteditor.multi_line_text.replace")));
			regexBtn = addWidget(MultiVersionMisc.newButton(180, -2, 20, 20,
					TextInst.translatable("nbteditor.multi_line_text.regex." + (regex ? "on" : "off")), btn -> {
				regex = !regex;
				btn.setMessage(TextInst.translatable("nbteditor.multi_line_text.regex." + (regex ? "on" : "off")));
			}, new MultiVersionTooltip("nbteditor.multi_line_text.regex")));
			addWidget(MultiVersionMisc.newButton(0, 40, 40, 20, TextInst.translatable("nbteditor.multi_line_text.find"), btn -> {
				goToNext(Screen.hasShiftDown(), true);
			}));
			addWidget(MultiVersionMisc.newButton(44, 40, 64, 20, TextInst.translatable("nbteditor.multi_line_text.replace"), btn -> {
				if (goToNext(Screen.hasShiftDown(), true))
					replaceSel();
			}));
			addWidget(MultiVersionMisc.newButton(112, 40, 64, 20, TextInst.translatable("nbteditor.multi_line_text.replace_all"), btn -> {
				boolean first = true;
				int prevCursor = cursor;
				cursor = 0;
				while (goToNext(false, false)) {
					if (first)
						first = false;
					else {
						undo.remove(0);
						onUndoDiscard();
					}
					replaceSel();
				}
				if (first)
					cursor = prevCursor;
			}));
			addWidget(MultiVersionMisc.newButton(180, 40, 20, 20, TextInst.translatable("nbteditor.multi_line_text.x"), btn -> {
				OverlaySupportingScreen.setOverlayStatic(null);
			}));
			
			if (selStart != selEnd)
				findValue = getSelectedText();
			find.setMaxLength(Integer.MAX_VALUE);
			find.setText(findValue);
			find.setChangedListener(str -> findValue = str);
			setFocused(find);
			
			replace.setMaxLength(Integer.MAX_VALUE);
			replace.setText(replaceValue);
			replace.setChangedListener(str -> replaceValue = str);
		}
		
		private boolean goToNext(boolean backward, boolean wrap) {
			if (findValue.isEmpty())
				return false;
			if (backward) {
				if (cursor == 0 || !goToRange(text, findValue, cursor - 1, true))
					return wrap && goToRange(text, findValue, text.length(), true);
			} else {
				if (!goToRange(text, findValue, cursor, false))
					return wrap && goToRange(text, findValue, 0, false);
			}
			return true;
		}
		private boolean goToRange(String str, String expr, int start, boolean last) {
			if (regex) {
				if (last)
					str = str.substring(0, start);
				try {
					Matcher matcher = Pattern.compile(expr).matcher(str);
					if (!matcher.find(last ? 0 : start))
						return false;
					int numMatches = 0;
					do {
						numMatches++;
						selStart = matcher.start();
						selEnd = matcher.end();
						if (selStart == selEnd)
							return false;
					} while (last && matcher.find());
					if (last) {
						matcher.reset();
						for (int i = 0; i < numMatches; i++)
							matcher.find();
					}
					lastRegexMatch = matcher;
				} catch (PatternSyntaxException e) {
					return false;
				}
			} else {
				int i = last ? str.substring(0, start).lastIndexOf(expr) : str.indexOf(expr, start);
				if (i == -1)
					return false;
				selStart = i;
				selEnd = selStart + expr.length();
			}
			cursor = selEnd;
			cursorX = -1;
			return true;
		}
		private void replaceSel() {
			if (selStart == selEnd)
				return;
			if (!regex) {
				write(replaceValue);
				return;
			}
			StringBuilder replacement = new StringBuilder();
			lastRegexMatch.appendReplacement(replacement, replaceValue);
			replacement.delete(0, lastRegexMatch.start());
			write(replacement.toString());
		}
		
		@Override
		public void renderPre(MatrixStack matrices, int mouseX, int mouseY, float delta) {
			MultiVersionDrawableHelper.fill(matrices, -16, -16, 216, 76, 0xC8101010);
		}
		
		@Override
		protected boolean mouseClickedPre(double mouseX, double mouseY, int button) {
			if (isMouseOver(mouseX, mouseY) && !(mouseX >= 0 && mouseX <= 200 && mouseY >= 0 && mouseY <= 60))
				dragging = true;
			return false;
		}
		@Override
		protected boolean mouseReleasedPre(double mouseX, double mouseY, int button) {
			dragging = false;
			return false;
		}
		@Override
		public boolean mouseDraggedPre(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
			if (dragging)
				addTranslation(deltaX, deltaY, 0);
			return false;
		}
		
		@Override
		public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
			if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
				OverlaySupportingScreen.setOverlayStatic(null);
				return true;
			}
			if (keyCode == GLFW.GLFW_KEY_ENTER) {
				goToNext(Screen.hasShiftDown(), true);
				return true;
			}
			if (keyCode == GLFW.GLFW_KEY_TAB) {
				if (getFocused() == find)
					setFocused(replace);
				else
					setFocused(find);
				return true;
			}
			if (keyCode == GLFW.GLFW_KEY_R && Screen.hasControlDown() && !Screen.hasShiftDown() && !Screen.hasAltDown()) {
				regex = !regex;
				regexBtn.setMessage(TextInst.translatable("nbteditor.multi_line_text.regex." + (regex ? "on" : "off")));
				return true;
			}
			
			return super.keyPressed(keyCode, scanCode, modifiers);
		}
		
		@Override
		public boolean isMouseOver(double mouseX, double mouseY) {
			return mouseX >= -16 && mouseX <= 216 && mouseY >= -16 && mouseY <= 76;
		}
	}
	
	private static final TextRenderer textRenderer = MainUtil.client.textRenderer;
	
	public static MultiLineTextFieldWidget create(MultiLineTextFieldWidget prev, int x, int y, int width, int height,
			String text, Function<String, Text> formatter, boolean newLines, Consumer<String> onChange) {
		if (prev == null)
			return new MultiLineTextFieldWidget(x, y, width, height, text, formatter, newLines, onChange);
		if (prev.newLines != newLines)
			throw new IllegalArgumentException("Cannot convert to/from newLines on MultiLineTextFieldWidget");
		prev.x = x;
		prev.y = y;
		prev.width = width;
		prev.height = height;
		prev.setText(text);
		prev.setFormatter(formatter);
		prev.setChangeListener(onChange);
		prev.generateLines();
		if (prev.isMultiFocused())
			prev.onFocusChange(false);
		return prev;
	}
	public static MultiLineTextFieldWidget create(MultiLineTextFieldWidget prev, int x, int y, int width, int height,
			String text, boolean newLines, Consumer<String> onChange) {
		return create(prev, x, y, width, height, text, null, newLines, onChange);
	}
	
	private int x;
	private int y;
	private int width;
	private int height;
	private String text;
	private Function<String, Text> formatter;
	private final boolean newLines;
	private Consumer<String> onChange;
	protected int maxLines;
	private int bgColor;
	private int cursorColor;
	private int selColor;
	private boolean shadow;
	
	private final List<Text> lines;
	private final List<Text> renderedLines;
	private final List<Map.Entry<String, Integer>> undo;
	private int undoPos;
	private int cursor;
	private int selStart;
	private int selEnd;
	private int cursorBlinkTracker;
	private int cursorX;
	private int scroll;
	
	protected MultiLineTextFieldWidget(int x, int y, int width, int height, String text,
			Function<String, Text> formatter, boolean newLines, Consumer<String> onChange) {
		text = MultiVersionMisc.stripInvalidChars(text, newLines);
		
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.text = text;
		this.formatter = formatter;
		this.newLines = newLines;
		this.onChange = str -> {
			generateLines();
			onChange.accept(str);
		};
		this.maxLines = Integer.MAX_VALUE;
		this.bgColor = 0x55000000;
		this.cursorColor = 0xFFFFFFFF;
		this.selColor = 0x55FFFFFF;
		this.shadow = true;
		
		this.lines = new ArrayList<>();
		this.renderedLines = new ArrayList<>();
		this.undo = new ArrayList<>();
		this.undo.add(0, Map.entry(text, text.length()));
		this.undoPos = 0;
		setCursor(text.length());
		this.cursorX = -1;
		
		generateLines();
	}
	protected MultiLineTextFieldWidget(int x, int y, int width, int height, String text, boolean newLines, Consumer<String> onChange) {
		this(x, y, width, height, text, null, newLines, onChange);
	}
	
	public MultiLineTextFieldWidget setFormatter(Function<String, Text> formatter) {
		this.formatter = formatter;
		generateLines();
		return this;
	}
	public MultiLineTextFieldWidget setChangeListener(Consumer<String> onChange) {
		this.onChange = str -> {
			generateLines();
			onChange.accept(str);
		};
		return this;
	}
	public MultiLineTextFieldWidget setMaxLines(int maxLines) {
		this.maxLines = maxLines;
		return this;
	}
	public MultiLineTextFieldWidget setBackgroundColor(int bgColor) {
		this.bgColor = bgColor;
		return this;
	}
	public MultiLineTextFieldWidget setCursorColor(int cursorColor) {
		this.cursorColor = cursorColor;
		return this;
	}
	public MultiLineTextFieldWidget setSelectionColor(int selColor) {
		this.selColor = selColor;
		return this;
	}
	public MultiLineTextFieldWidget setShadow(boolean shadow) {
		this.shadow = shadow;
		return this;
	}
	
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}
	
	public void setText(String text) {
		if (this.text.equals(text))
			return;
		selStart = 0;
		selEnd = this.text.length();
		write(text);
	}
	public String getText() {
		return text;
	}
	
	public boolean allowsNewLines() {
		return newLines;
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		MultiVersionDrawableHelper.fill(matrices, x, y, x + width, y + height, bgColor);
		
		boolean scissor = !ConfigScreen.isMacScrollPatch();
		if (scissor) {
			MinecraftClient client = MainUtil.client;
			RenderSystem.enableScissor((int) (x * client.getWindow().getScaleFactor()), client.getWindow().getHeight() - (int) ((y + height) * client.getWindow().getScaleFactor()), (int) (width * client.getWindow().getScaleFactor()), (int) (height * client.getWindow().getScaleFactor()));
		}
		matrices.push();
		matrices.translate(0.0, scroll, 0.0);
		
		renderHighlightsBelow(matrices, mouseX, mouseY, delta);
		
		int yOffset = y;
		for (Text line : renderedLines) {
			MultiVersionDrawableHelper.drawText(matrices, textRenderer, line, x + textRenderer.fontHeight, yOffset + textRenderer.fontHeight, -1, shadow);
			yOffset += textRenderer.fontHeight * 1.5;
		}
		
		renderHighlightsAbove(matrices, mouseX, mouseY, delta);
		renderHighlight(matrices, getSelStart(), getSelEnd(), selColor);
		
		if (isMultiFocused() && cursorBlinkTracker / 6 % 2 == 0) {
			Point cursor = getXYPos(this.cursor);
			MultiVersionDrawableHelper.fill(matrices, cursor.x, cursor.y, cursor.x + 1, cursor.y + textRenderer.fontHeight, cursorColor);
		}
		
		matrices.pop();
		if (scissor)
			RenderSystem.disableScissor();
	}
	protected void renderHighlightsBelow(MatrixStack matrices, int mouseX, int mouseY, float delta) {}
	protected void renderHighlightsAbove(MatrixStack matrices, int mouseX, int mouseY, float delta) {}
	protected void renderHighlight(MatrixStack matrices, int start, int end, int color) {
		Point startPos = getXYPos(start);
		Point endPos = getXYPos(end);
		if (startPos.y == endPos.y)
			MultiVersionDrawableHelper.fill(matrices, startPos.x, startPos.y, endPos.x, endPos.y + textRenderer.fontHeight, color);
		else {
			int line = 0;
			int lineY;
			while ((lineY = startPos.y + line * (int) (textRenderer.fontHeight * 1.5)) < endPos.y) {
				Point lineStart = line == 0 ? startPos : new Point(x + textRenderer.fontHeight, lineY);
				MultiVersionDrawableHelper.fill(matrices, lineStart.x, lineStart.y, x + width - textRenderer.fontHeight, lineStart.y + textRenderer.fontHeight, color);
				line++;
			}
			MultiVersionDrawableHelper.fill(matrices, x + textRenderer.fontHeight, lineY, endPos.x, endPos.y + textRenderer.fontHeight, color);
		}
	}
	
	@Override
	public void tick() {
		cursorBlinkTracker++;
	}
	
	protected void generateLines() {
		lines.clear();
		renderedLines.clear();
		
		String text = this.text;
		Text formattedText = (formatter == null ? TextInst.of(text) : formatter.apply(text));
		boolean endsWithNewLine = false;
		while (!text.isEmpty()) {
			if (text.charAt(0) == '\n') {
				endsWithNewLine = true;
				lines.add(TextInst.of("\n"));
				renderedLines.add(TextInst.of(""));
				text = text.substring(1);
				formattedText = MainUtil.substring(formattedText, 1);
				continue;
			}
			int charPos = 1;
			while (textRenderer.getWidth(MainUtil.substring(formattedText, 0, charPos)) < width - textRenderer.fontHeight * 2) {
				charPos++;
				if (text.length() < charPos || text.charAt(charPos - 1) == '\n')
					break;
			}
			endsWithNewLine = charPos - 1 < text.length() && text.charAt(charPos - 1) == '\n';
			int extraPos = charPos - 1 + (endsWithNewLine ? 1 : 0);
			lines.add(MainUtil.substring(formattedText, 0, extraPos));
			renderedLines.add(MainUtil.substring(formattedText, 0, charPos - 1));
			text = text.substring(extraPos);
			formattedText = MainUtil.substring(formattedText, extraPos);
		}
		if (endsWithNewLine) {
			Text emptyLine = TextInst.of("");
			lines.add(emptyLine);
			renderedLines.add(emptyLine);
		}
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!isMouseOver(mouseX, mouseY))
			return false;
		
		setCursor(getCharPos(mouseX, mouseY - scroll), true);
		cursorX = -1;
		return true;
	}
	
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (!isMouseOver(mouseX, mouseY))
			return false;
		
		int selEnd = getCharPos(mouseX, mouseY - scroll);
		onCursorMove(selEnd, Math.min(selStart, selEnd), Math.max(selStart, selEnd));
		this.selEnd = selEnd;
		cursor = selEnd;
		cursorX = -1;
		return true;
	}
	public int getSelStart() {
		return Math.min(selStart, selEnd);
	}
	public int getSelEnd() {
		return Math.max(selStart, selEnd);
	}
	
	public int getCursor() {
		return cursor;
	}
	
	private int getCharPos(int mouseX, int mouseY) {
		if (lines.isEmpty())
			return 0;
		
		int line = (mouseY - y - textRenderer.fontHeight) / (int) (textRenderer.fontHeight * 1.5);
		if (line >= lines.size())
			line = lines.size() - 1;
		
		Text lineValue = renderedLines.get(line);
		int lineLen = lineValue.getString().length();
		int charPos = 0;
		while (textRenderer.getWidth(MainUtil.substring(lineValue, 0, charPos)) < mouseX - x - textRenderer.fontHeight) {
			charPos++;
			if (charPos > lineLen)
				break;
		}
		if (charPos != 0)
			charPos--;
		
		for (int i = 0; i < line; i++)
			charPos += lines.get(i).getString().length();
		
		return charPos;
	}
	private int getCharPos(double mouseX, double mouseY) {
		return getCharPos((int) mouseX, (int) mouseY);
	}
	private Point getXYPos(int charPos) {
		int lineX = 0;
		int lineY = 0;
		if (charPos >= text.length()) {
			lineX = lines.isEmpty() ? 0 : textRenderer.getWidth(lines.get(lines.size() - 1));
			lineY = lines.isEmpty() ? 0 : lines.size() - 1;
		} else {
			int i = 0;
			for (Text line : lines) {
				int lineLen = line.getString().length();
				if (lineLen <= charPos) {
					lineY++;
					charPos -= lineLen;
				} else {
					lineX = textRenderer.getWidth(MainUtil.substring(lines.get(i), 0, charPos));
					break;
				}
				i++;
			}
		}
		
		return new Point(lineX + textRenderer.fontHeight + x, lineY * (int) (textRenderer.fontHeight * 1.5) + textRenderer.fontHeight + y);
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		if (isMouseOver(mouseX, mouseY)) {
			int maxScroll = -Math.max(0, lines.size() * (int) (textRenderer.fontHeight * 1.5) + textRenderer.fontHeight + height / 3 - height);
			if (amount < 0 && scroll > maxScroll) {
				scroll += amount * 5;
				if (scroll < maxScroll)
					scroll = maxScroll;
			}
			if (amount > 0 && scroll < 0) {
				scroll += amount * 5;
				if (scroll > 0)
					scroll = 0;
			}
			
			return true;
		}
		return false;
	}
	
	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
	}
	
	public void setCursor(int cursor, boolean select) {
		int selStart = this.selStart;
		int selEnd = this.selEnd;
		if (select && Screen.hasShiftDown())
			selEnd = cursor;
		else {
			selStart = cursor;
			selEnd = cursor;
		}
		onCursorMove(cursor, Math.min(selStart, selEnd), Math.max(selStart, selEnd));
		this.cursor = cursor;
		this.selStart = selStart;
		this.selEnd = selEnd;
	}
	public void setCursor(int cursor) {
		setCursor(cursor, false);
	}
	
	private void write(String text) {
		while (undoPos > 0) {
			undo.remove(0);
			undoPos--;
		}
		
		text = MultiVersionMisc.stripInvalidChars(text, newLines);
		onEdit(text, getSelStart(), getSelEnd() - getSelStart());
		this.text = new StringBuilder(this.text).replace(getSelStart(), getSelEnd(), text).toString();
		setCursor(getSelStart() + text.length());
		undo.add(0, Map.entry(this.text, cursor));
		onChange.accept(this.text);
	}
	
	public String getSelectedText() {
		return text.substring(getSelStart(), getSelEnd());
	}
	
	private void moveCursorUp() {
		Point pos = getXYPos(cursor);
		if (cursorX == -1)
			cursorX = pos.x;
		if (pos.y == getXYPos(0).y) {
			setCursor(0, true);
			cursorX = -1;
		} else
			setCursor(getCharPos(cursorX, pos.y - (int) (textRenderer.fontHeight * 1.5)), true);
	}
	private void moveCursorDown() {
		Point pos = getXYPos(cursor);
		if (cursorX == -1)
			cursorX = pos.x;
		if (pos.y == getXYPos(text.length()).y) {
			setCursor(text.length(), true);
			cursorX = -1;
		} else
			setCursor(getCharPos(cursorX, pos.y + (int) (textRenderer.fontHeight * 1.5)), true);
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (Screen.isSelectAll(keyCode)) {
			onCursorMove(text.length(), 0, text.length());
			selStart = 0;
			selEnd = text.length();
			cursor = selEnd;
			cursorX = -1;
			return true;
		}
		if (Screen.isCopy(keyCode)) {
			MainUtil.client.keyboard.setClipboard(onCopy(getSelectedText(), getSelStart(), getSelEnd() - getSelStart()));
			return true;
		}
		if (Screen.isPaste(keyCode)) {
			this.write(pasteFilter(onPaste(MainUtil.client.keyboard.getClipboard(), getSelStart(), getSelEnd() - getSelStart())));
			cursorX = -1;
			return true;
		}
		if (Screen.isCut(keyCode)) {
			MainUtil.client.keyboard.setClipboard(onCopy(getSelectedText(), getSelStart(), getSelEnd() - getSelStart()));
			this.write("");
			cursorX = -1;
			return true;
		}
		if (isUndo(keyCode)) {
			if (undoPos < undo.size() - 1) {
				Map.Entry<String, Integer> undoData = undo.get(++undoPos);
				onUndo(undoData.getKey());
				text = undoData.getKey();
				setCursor(undoData.getValue());
				cursorX = -1;
				onChange.accept(text);
			}
			return true;
		}
		if (isRedo(keyCode)) {
			if (undoPos > 0) {
				Map.Entry<String, Integer> undoData = undo.get(--undoPos);
				onRedo(undoData.getKey());
				text = undoData.getKey();
				setCursor(undoData.getValue());
				cursorX = -1;
				onChange.accept(text);
			}
			return true;
		}
		if (isFind(keyCode)) {
			OverlaySupportingScreen.setOverlayStatic(new FindAndReplaceWidget());
			return true;
		}
		switch (keyCode) {
			case GLFW.GLFW_KEY_LEFT: {
				if (Screen.hasControlDown()) {
					this.setCursor(this.getWordSkipPosition(true, false), true);
				} else {
					this.moveCursor(-1);
				}
				cursorX = -1;
				return true;
			}
			case GLFW.GLFW_KEY_RIGHT: {
				if (Screen.hasControlDown()) {
					this.setCursor(this.getWordSkipPosition(false, false), true);
				} else {
					this.moveCursor(1);
				}
				cursorX = -1;
				return true;
			}
			case GLFW.GLFW_KEY_UP: {
				if (Screen.hasControlDown())
					setCursor(0, true);
				else
					moveCursorUp();
				return true;
			}
			case GLFW.GLFW_KEY_DOWN: {
				if (Screen.hasControlDown())
					setCursor(text.length(), true);
				else
					moveCursorDown();
				return true;
			}
			case GLFW.GLFW_KEY_BACKSPACE: {
				this.erase(true);
				cursorX = -1;
				return true;
			}
			case GLFW.GLFW_KEY_DELETE: {
				this.erase(false);
				cursorX = -1;
				return true;
			}
			case GLFW.GLFW_KEY_HOME: {
				setCursor(0, true);
				cursorX = -1;
				return true;
			}
			case GLFW.GLFW_KEY_END: {
				setCursor(text.length(), true);
				cursorX = -1;
				return true;
			}
			case GLFW.GLFW_KEY_ENTER: {
				if (newLines && getNumNewLines(text) + 1 < maxLines) {
					write("\n");
					cursorX = -1;
				}
				return true;
			}
		}
		return false;
	}
	protected String pasteFilter(String toPaste) {
		toPaste = MultiVersionMisc.stripInvalidChars(toPaste, newLines);
		int numNewLines = getNumNewLines(text);
		int toPasteNewLines = getNumNewLines(toPaste);
		while (numNewLines + toPasteNewLines + 1 > maxLines) {
			int i = toPaste.lastIndexOf('\n');
			if (i == -1)
				break;
			toPaste = new StringBuilder(toPaste).deleteCharAt(i).toString();
			toPasteNewLines--;
		}
		return toPaste;
	}
	protected int getNumNewLines(String str) {
		int numNewLines = 0;
		for (char c : str.toCharArray()) {
			if (c == '\n')
				numNewLines++;
		}
		return numNewLines;
	}
	
	public static boolean isUndo(int code) {
		return code == GLFW.GLFW_KEY_Z && Screen.hasControlDown() && !Screen.hasShiftDown() && !Screen.hasAltDown();
	}
	public static boolean isRedo(int code) {
		return code == GLFW.GLFW_KEY_Y && Screen.hasControlDown() && !Screen.hasShiftDown() && !Screen.hasAltDown();
	}
	public static boolean isFind(int code) {
		return code == GLFW.GLFW_KEY_F && Screen.hasControlDown() && !Screen.hasShiftDown() && !Screen.hasAltDown();
	}
	
	// passOneSpace requires that one section of whitespace is passed, either at the end or beginning of the search
	// if false, any beginning whitespace is passed, then everything until, but not including, the end whitespace is passed
	private int getWordSkipPosition(boolean backward, boolean passOneSpace) {
		boolean findingText = true;
		boolean foundSpace = false;
		boolean findingText2 = false;
		for (int i = cursor + (backward ? -1 : 0); 0 <= i && i < text.length(); i += (backward ? -1 : 1)) {
			char c = text.charAt(i);
			if (c == ' ' || c == '\n') {
				if (findingText2)
					;
				else if (findingText)
					foundSpace = true;
				else if (foundSpace || !passOneSpace) {
					if (backward)
						return i + 1;
					return i;
				} else
					findingText2 = true;
			} else if (findingText2) {
				if (backward)
					return i + 1;
				return i;
			} else
				findingText = false;
		}
		if (backward)
			return 0;
		return text.length();
	}
	
	private void moveCursor(int offset) {
		this.setCursor(this.getCursorPosWithOffset(offset), true);
	}
	
	private int getCursorPosWithOffset(int offset) {
		return Util.moveCursor(this.text, Screen.hasShiftDown() ? cursor : (offset > 0 ? getSelEnd() : getSelStart()), offset);
	}
	
	private void erase(boolean backwards) {
		if (Screen.hasControlDown()) {
			this.eraseWords(backwards);
		} else {
			this.eraseCharacters(backwards ? -1 : 1);
		}
	}
	private void eraseWords(boolean backwards) {
		if (this.text.isEmpty()) {
			return;
		}
		if (selStart != selEnd) {
			this.write("");
			return;
		}
		this.eraseCharacters(this.getWordSkipPosition(backwards, true) - getSelStart());
	}
	private void eraseCharacters(int characterOffset) {
		while (undoPos > 0) {
			undo.remove(0);
			undoPos--;
		}
		
		int k;
		if (this.text.isEmpty()) {
			return;
		}
		if (selStart != selEnd) {
			this.write("");
			return;
		}
		int i = this.getCursorPosWithOffset(characterOffset);
		int j = Math.min(i, getSelStart());
		if (j == (k = Math.max(i, getSelStart()))) {
			return;
		}
		onEdit("", j, k - j);
		String string = new StringBuilder(this.text).delete(j, k).toString();
		this.text = string;
		this.setCursor(j);
		undo.add(0, Map.entry(this.text, cursor));
		onChange.accept(this.text);
	}
	
	@Override
	public boolean charTyped(char chr, int modifiers) {
		if (SharedConstants.isValidChar(chr)) {
			this.write(Character.toString(chr));
			cursorX = -1;
			return true;
		}
		return false;
	}
	
	
	protected void onCursorMove(int cursor, int selStart, int selEnd) {}
	protected void onEdit(String insertedText, int pos, int overwrittenLen) {}
	protected void onUndo(String newText) {}
	protected void onRedo(String newText) {}
	protected void onUndoDiscard() {}
	protected String onCopy(String text, int pos, int len) {
		return text;
	}
	protected String onPaste(String text, int pos, int overwrittenLen) {
		return text;
	}
	protected void markUndo() {
		while (undoPos > 0) {
			undo.remove(0);
			undoPos--;
		}
		undo.add(0, Map.entry(this.text, cursor));
	}
	
	
	
	@Override
	public SelectionType getType() {
		return SelectionType.NONE;
	}
	
	@Override
	public void appendNarrations(NarrationMessageBuilder var1) {
		
	}
	
}
