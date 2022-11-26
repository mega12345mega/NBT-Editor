package com.luneruniverse.minecraft.mod.nbteditor.screens;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import org.lwjgl.glfw.GLFW;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.EditableText;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.StringVisitable.StyledVisitor;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

public class MultiLineTextFieldWidget implements Drawable, Element, Selectable {
	
	private static final TextRenderer textRenderer = MainUtil.client.textRenderer;
	
	
	private final int x;
	private final int y;
	private final int width;
	private final int height;
	private String text;
	private final Function<String, Text> formatter;
	private final Consumer<String> onChange;
	
	private final List<String> lines;
	private final List<Text> formattedLines;
	private final List<Map.Entry<String, Integer>> undo;
	private int undoPos;
	private int cursor;
	private int selStart;
	private int selEnd;
	private int cursorBlinkTracker;
	private int cursorX;
	private int scroll;
	
	public MultiLineTextFieldWidget(int x, int y, int width, int height, String text, Function<String, Text> formatter, Consumer<String> onChange) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.text = text;
		this.formatter = formatter;
		this.onChange = str -> {
			generateLines();
			onChange.accept(str);
		};
		
		this.lines = new ArrayList<>();
		this.formattedLines = new ArrayList<>();
		this.undo = new ArrayList<>();
		this.undo.add(0, Map.entry(text, text.length()));
		this.undoPos = 0;
		setCursor(text.length());
		this.cursorX = -1;
		
		generateLines();
	}
	public MultiLineTextFieldWidget(int x, int y, int width, int height, String text, Consumer<String> onChange) {
		this(x, y, width, height, text, null, onChange);
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		Screen.fill(matrices, x, y, x + width, y + height, 0x55000000);
		
		boolean scissor = !ConfigScreen.isMacScrollPatch();
		if (scissor) {
			MinecraftClient client = MainUtil.client;
			RenderSystem.enableScissor((int) (x * client.getWindow().getScaleFactor()), client.getWindow().getHeight() - (int) ((y + height) * client.getWindow().getScaleFactor()), (int) (width * client.getWindow().getScaleFactor()), (int) (height * client.getWindow().getScaleFactor()));
		}
		matrices.push();
		matrices.translate(0, scroll, 0);
		
		int yOffset = y;
		if (formatter == null) {
			for (String line : lines) {
				textRenderer.drawWithShadow(matrices, line, x + textRenderer.fontHeight, yOffset + textRenderer.fontHeight, -1);
				yOffset += textRenderer.fontHeight * 1.5;
			}
		} else {
			for (Text line : formattedLines) {
				textRenderer.drawWithShadow(matrices, line, x + textRenderer.fontHeight, yOffset + textRenderer.fontHeight, -1);
				yOffset += textRenderer.fontHeight * 1.5;
			}
		}
		
		Point selStart = getXYPos(getSelStart());
		Point selEnd = getXYPos(getSelEnd());
		if (selStart.y == selEnd.y)
			Screen.fill(matrices, selStart.x, selStart.y, selEnd.x, selEnd.y + textRenderer.fontHeight, 0x55FFFFFF);
		else {
			int line = 0;
			int lineY;
			while ((lineY = selStart.y + line * (int) (textRenderer.fontHeight * 1.5)) < selEnd.y) {
				Point lineStart = line == 0 ? selStart : new Point(x + textRenderer.fontHeight, lineY);
				Screen.fill(matrices, lineStart.x, lineStart.y, x + width - textRenderer.fontHeight, lineStart.y + textRenderer.fontHeight, 0x55FFFFFF);
				line++;
			}
			Screen.fill(matrices, x + textRenderer.fontHeight, lineY, selEnd.x, selEnd.y + textRenderer.fontHeight, 0x55FFFFFF);
		}
		
		if (cursorBlinkTracker / 6 % 2 == 0) {
			Point cursor = getXYPos(this.cursor);
			Screen.fill(matrices, cursor.x, cursor.y, cursor.x + 1, cursor.y + textRenderer.fontHeight, 0xFFFFFFFF);
		}
		
		matrices.pop();
		if (scissor)
			RenderSystem.disableScissor();
	}
	
	public void tick() {
		cursorBlinkTracker++;
	}
	
	private void generateLines() {
		lines.clear();
		
		String text = this.text;
		while (!text.isEmpty()) {
			int charPos = 1;
			String line;
			while (textRenderer.getWidth(line = text.substring(0, charPos)) < width - textRenderer.fontHeight * 2) {
				charPos++;
				if (text.length() < charPos)
					break;
			}
			lines.add(line.substring(0, charPos - 1));
			text = text.substring(charPos - 1);
		}
		
		if (formatter != null) {
			formattedLines.clear();
			
			formatter.apply(this.text).visit(new StyledVisitor<Boolean>() {
				private int i = 0;
				private EditableText line = TextInst.literal("");
				
				@Override
				public Optional<Boolean> accept(Style style, String str) {
					if (i >= lines.size())
						return Optional.of(true);
					
					int targetLength = lines.get(i).length();
					int currentLength = line.getString().length();
					if (currentLength + str.length() >= targetLength) {
						line.append(TextInst.literal(str.substring(0, targetLength - currentLength)).fillStyle(style));
						formattedLines.add(line);
						line = TextInst.literal("");
						i++;
						if (currentLength + str.length() > targetLength)
							accept(style, str.substring(targetLength - currentLength));
					} else
						line.append(TextInst.literal(str).fillStyle(style));
					return Optional.empty();
				}
			}, Style.EMPTY);
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
		
		selEnd = getCharPos(mouseX, mouseY - scroll);
		cursor = selEnd;
		cursorX = -1;
		return true;
	}
	private int getSelStart() {
		return Math.min(selStart, selEnd);
	}
	private int getSelEnd() {
		return Math.max(selStart, selEnd);
	}
	
	private int getCharPos(int mouseX, int mouseY) {
		if (lines.isEmpty())
			return 0;
		
		int line = (mouseY - y - textRenderer.fontHeight) / (int) (textRenderer.fontHeight * 1.5);
		if (line >= lines.size())
			line = lines.size() - 1;
		
		int charPos = 0;
		String lineStr = lines.get(line);
		while (textRenderer.getWidth(lineStr.substring(0, charPos)) < mouseX - x - textRenderer.fontHeight) {
			charPos++;
			if (charPos > lineStr.length())
				break;
		}
		if (charPos != 0)
			charPos--;
		
		for (int i = 0; i < line; i++)
			charPos += lines.get(i).length();
		
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
			for (String line : lines) {
				if (line.length() <= charPos) {
					lineY++;
					charPos -= line.length();
				} else {
					lineX = textRenderer.getWidth(line.substring(0, charPos));
					break;
				}
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
		this.cursor = cursor;
		if (select && Screen.hasShiftDown())
			this.selEnd = cursor;
		else {
			this.selStart = cursor;
			this.selEnd = cursor;
		}
	}
	public void setCursor(int cursor) {
		setCursor(cursor, false);
	}
	
	private void write(String text) {
		while (undoPos > 0) {
			undo.remove(0);
			undoPos--;
		}
		
		text = SharedConstants.stripInvalidChars(text);
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
			selStart = 0;
			selEnd = text.length();
			cursor = selEnd;
			cursorX = -1;
			return true;
		}
		if (Screen.isCopy(keyCode)) {
			MainUtil.client.keyboard.setClipboard(this.getSelectedText());
			return true;
		}
		if (Screen.isPaste(keyCode)) {
			this.write(MainUtil.client.keyboard.getClipboard());
			cursorX = -1;
			return true;
		}
		if (Screen.isCut(keyCode)) {
			MainUtil.client.keyboard.setClipboard(this.getSelectedText());
			this.write("");
			cursorX = -1;
			return true;
		}
		if (isUndo(keyCode)) {
			if (undoPos < undo.size() - 1) {
				Map.Entry<String, Integer> undoData = undo.get(++undoPos);
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
				text = undoData.getKey();
				setCursor(undoData.getValue());
				cursorX = -1;
				onChange.accept(text);
			}
			return true;
		}
		switch (keyCode) {
			case GLFW.GLFW_KEY_LEFT: {
				if (Screen.hasControlDown()) {
					this.setCursor(this.getWordSkipPosition(-1), true);
				} else {
					this.moveCursor(-1);
				}
				cursorX = -1;
				return true;
			}
			case GLFW.GLFW_KEY_RIGHT: {
				if (Screen.hasControlDown()) {
					this.setCursor(this.getWordSkipPosition(1), true);
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
				this.erase(-1);
				cursorX = -1;
				return true;
			}
			case GLFW.GLFW_KEY_DELETE: {
				this.erase(1);
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
		}
		return false;
	}
	
	public static boolean isUndo(int code) {
		return code == GLFW.GLFW_KEY_Z && Screen.hasControlDown() && !Screen.hasShiftDown() && !Screen.hasAltDown();
	}
	public static boolean isRedo(int code) {
		return code == GLFW.GLFW_KEY_Y && Screen.hasControlDown() && !Screen.hasShiftDown() && !Screen.hasAltDown();
	}
	
	private int getWordSkipPosition(int wordOffset) {
		return this.getWordSkipPosition(wordOffset, cursor);
	}
	
	private int getWordSkipPosition(int wordOffset, int cursorPosition) {
		return this.getWordSkipPosition(wordOffset, cursorPosition, true);
	}
	
	private int getWordSkipPosition(int wordOffset, int cursorPosition, boolean skipOverSpaces) {
		int i = cursorPosition;
		boolean bl = wordOffset < 0;
		int j = Math.abs(wordOffset);
		for (int k = 0; k < j; ++k) {
			if (bl) {
				while (skipOverSpaces && i > 0 && this.text.charAt(i - 1) == ' ') {
					--i;
				}
				while (i > 0 && this.text.charAt(i - 1) != ' ') {
					--i;
				}
				continue;
			}
			int l = this.text.length();
			if ((i = this.text.indexOf(32, i)) == -1) {
				i = l;
				continue;
			}
			while (skipOverSpaces && i < l && this.text.charAt(i) == ' ') {
				++i;
			}
		}
		return i;
	}
	
	private void moveCursor(int offset) {
		this.setCursor(this.getCursorPosWithOffset(offset), true);
	}
	
	private int getCursorPosWithOffset(int offset) {
		return Util.moveCursor(this.text, Screen.hasShiftDown() ? cursor : (offset > 0 ? getSelEnd() : getSelStart()), offset);
	}
	
	private void erase(int offset) {
		if (Screen.hasControlDown()) {
			this.eraseWords(offset);
		} else {
			this.eraseCharacters(offset);
		}
	}
	private void eraseWords(int wordOffset) {
		if (this.text.isEmpty()) {
			return;
		}
		if (selStart != selEnd) {
			this.write("");
			return;
		}
		this.eraseCharacters(this.getWordSkipPosition(wordOffset) - getSelStart());
	}
	private void eraseCharacters(int characterOffset) {
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
		String string = new StringBuilder(this.text).delete(j, k).toString();
		this.text = string;
		this.setCursor(j);
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
	
	
	
	@Override
	public void appendNarrations(NarrationMessageBuilder var1) {
		
	}
	
	@Override
	public SelectionType getType() {
		return SelectionType.NONE;
	}
	
}
