package com.luneruniverse.minecraft.mod.nbteditor.screens.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import org.lwjgl.glfw.GLFW;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.EditableText;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.IdentifierInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVTooltip;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.OverlaySupportingScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigValueDropdown;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.luneruniverse.minecraft.mod.nbteditor.util.StyleUtil;
import com.luneruniverse.minecraft.mod.nbteditor.util.TextUtil;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class FormattedTextFieldWidget extends GroupWidget {
	
	private static class InternalTextFieldWidget extends MultiLineTextFieldWidget {
		private static class EventEditorWidget extends GroupWidget implements InitializableOverlay<Screen> {
			public enum ClickAction {
				NONE(null),
				OPEN_URL(ClickEvent.Action.OPEN_URL),
				RUN_COMMAND(ClickEvent.Action.RUN_COMMAND),
				SUGGEST_COMMAND(ClickEvent.Action.SUGGEST_COMMAND),
				CHANGE_PAGE(ClickEvent.Action.CHANGE_PAGE),
				COPY_TO_CLIPBOARD(ClickEvent.Action.COPY_TO_CLIPBOARD);
				
				public static ClickAction get(ClickEvent.Action value) {
					for (ClickAction action : values()) {
						if (action.value == value)
							return action;
					}
					if (value == ClickEvent.Action.OPEN_FILE)
						return NONE;
					throw new IllegalArgumentException("Invalid click action: " + value);
				}
				
				private final ClickEvent.Action value;
				private ClickAction(ClickEvent.Action value) {
					this.value = value;
				}
				public ClickEvent toEvent(String value) {
					if (this == NONE)
						return null;
					return new ClickEvent(this.value, value);
				}
				
				@Override
				public String toString() {
					if (this == NONE)
						return "none";
					return MVMisc.getClickEventActionName(value);
				}
			}
			public enum HoverAction {
				NONE(null),
				SHOW_TEXT(HoverEvent.Action.SHOW_TEXT),
				SHOW_ITEM(HoverEvent.Action.SHOW_ITEM),
				SHOW_ENTITY(HoverEvent.Action.SHOW_ENTITY);
				
				public static HoverAction get(HoverEvent.Action<?> value) {
					for (HoverAction action : values()) {
						if (action.value == value)
							return action;
					}
					throw new IllegalArgumentException("Invalid hover action: " + value);
				}
				
				private final HoverEvent.Action<?> value;
				private HoverAction(HoverEvent.Action<?> value) {
					this.value = value;
				}
				public HoverEvent toEvent(String value) {
					if (this == NONE)
						return null;
					JsonObject json = new JsonObject();
					json.addProperty("action", MVMisc.getHoverEventActionName(this.value));
					json.add("contents", new Gson().fromJson(value, JsonElement.class));
					return MVMisc.getHoverEvent(json);
				}
				
				@Override
				public String toString() {
					if (this == NONE)
						return "none";
					return MVMisc.getHoverEventActionName(value);
				}
			}
			public interface EventPairCallback {
				public void onEventChange(ClickEvent clickEvent, HoverEvent hoverEvent);
			}
			
			private int x;
			private int y;
			private final ConfigValueDropdown<ClickAction> clickActionDropdown;
			private final TranslatedGroupWidget clickActionField;
			private final NamedTextFieldWidget clickValueField;
			private final ConfigValueDropdown<HoverAction> hoverActionDropdown;
			private final TranslatedGroupWidget hoverActionField;
			private final NamedTextFieldWidget hoverValueField;
			private final ButtonWidget ok;
			private final ButtonWidget cancel;
			
			public EventEditorWidget(ClickEvent clickEvent, HoverEvent hoverEvent, EventPairCallback onDone) {
				ClickEvent.Action clickAction = (clickEvent == null ? null : clickEvent.getAction());
				String clickValue = (clickEvent == null ? "" : clickEvent.getValue());
				HoverEvent.Action<?> hoverAction = (hoverEvent == null ? null : hoverEvent.getAction());
				String hoverValue = (hoverEvent == null ? "" : MVMisc.getHoverEventContentsJson(hoverEvent).toString());
				
				clickActionDropdown = ConfigValueDropdown.forEnum(ClickAction.get(clickAction), ClickAction.NONE, ClickAction.class);
				clickActionDropdown.setWidth(150);
				clickActionField = addElement(TranslatedGroupWidget.forWidget(clickActionDropdown, 0, 0, 0));
				clickValueField = addWidget(new NamedTextFieldWidget(0, 0, 150, 16))
						.name(TextInst.translatable("nbteditor.formatted_text.click_event_value"));
				clickValueField.setMaxLength(Integer.MAX_VALUE);
				clickValueField.setText(clickValue);
				
				hoverActionDropdown = ConfigValueDropdown.forEnum(HoverAction.get(hoverAction), HoverAction.NONE, HoverAction.class);
				hoverActionDropdown.setWidth(150);
				hoverActionDropdown.addValueListener(value -> updateOk());
				hoverActionField = addElement(TranslatedGroupWidget.forWidget(hoverActionDropdown, 0, 0, 0));
				hoverValueField = addWidget(new NamedTextFieldWidget(0, 0, 150, 16))
						.name(TextInst.translatable("nbteditor.formatted_text.hover_event_value"));
				hoverValueField.setMaxLength(Integer.MAX_VALUE);
				hoverValueField.setText(hoverValue);
				hoverValueField.setChangedListener(str -> updateOk());
				
				ok = addWidget(MVMisc.newButton(0, 0, 150, 20, TextInst.translatable("nbteditor.ok"), btn -> {
					onDone.onEventChange(clickActionDropdown.getValidValue().toEvent(clickValueField.getText()),
							hoverActionDropdown.getValidValue().toEvent(hoverValueField.getText()));
					OverlaySupportingScreen.setOverlayStatic(null);
				}));
				cancel = addWidget(MVMisc.newButton(0, 0, 150, 20, TextInst.translatable("nbteditor.cancel"), btn -> {
					OverlaySupportingScreen.setOverlayStatic(null);
				}));
				
				addDrawable(hoverActionField);
				addDrawable(clickActionField);
			}
			
			@Override
			public void init(Screen parent, int width, int height) {
				x = width / 2;
				y = height / 2;
				
				clickActionField.setTranslation(x - 152, y - 34, 0);
				clickValueField.x = x + 2;
				clickValueField.y = y - 32;
				
				hoverActionField.setTranslation(x - 152, y - 12, 0);
				hoverValueField.x = x + 2;
				hoverValueField.y = y - 10;
				
				ok.x = x - 152;
				ok.y = y + 12;
				
				cancel.x = x + 2;
				cancel.y = y + 12;
			}
			
			private void updateOk() {
				if (hoverActionDropdown.getValidValue() == HoverAction.NONE) {
					ok.active = true;
					return;
				}
				try {
					hoverActionDropdown.getValidValue().toEvent(hoverValueField.getText());
					ok.active = true;
				} catch (Exception e) {
					ok.active = false;
				}
			}
			
			@Override
			public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
				MainUtil.client.currentScreen.renderBackground(matrices);
				MVDrawableHelper.drawCenteredTextWithShadow(matrices, MainUtil.client.textRenderer,
						TextInst.translatable("nbteditor.formatted_text.events"),
						x, y - 38 - MainUtil.client.textRenderer.fontHeight, -1);
				super.render(matrices, mouseX, mouseY, delta);
			}
			
			@Override
			public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
				if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
					OverlaySupportingScreen.setOverlayStatic(null);
					return true;
				}
				if (keyCode == GLFW.GLFW_KEY_ENTER) {
					if (ok.active)
						ok.onPress();
					return true;
				}
				
				return super.keyPressed(keyCode, scanCode, modifiers);
			}
		}
		
		public static InternalTextFieldWidget create(InternalTextFieldWidget prev, int x, int y, int width, int height,
				Text text, boolean newLines, Style base, Consumer<Text> onChange) {
			if (prev == null)
				return new InternalTextFieldWidget(x, y, width, height, text, newLines, base, onChange);
			if (prev.allowsNewLines() != newLines)
				throw new IllegalArgumentException("Cannot convert to/from newLines on FormattedTextFieldWidget");
			if (!StyleUtil.identical(prev.base, base))
				throw new IllegalArgumentException("Cannot change base on FormattedTextFieldWidget");
			prev.setTextChangeListener(onChange);
			prev.ignoreNextSetText = true;
			MultiLineTextFieldWidget.create(prev, x, y, width, height, text.getString(),
					str -> prev.text, newLines, str -> prev.onChange.accept(prev.text));
			prev.setFormattedText(text);
			return prev;
		}
		
		private Consumer<Text> onChange;
		private final Style base;
		private final Style baseReset;
		private final List<Style> styles;
		private Style cursorStyle;
		private Text text;
		private boolean ignoreNextEditStyles;
		private boolean ignoreNextSetText;
		private final List<Text> undo;
		private int undoPos;
		
		protected InternalTextFieldWidget(int x, int y, int width, int height, Text text, boolean newLines, Style base, Consumer<Text> onChange) {
			super(x, y, width, height, text.getString(), newLines, null);
			this.onChange = onChange;
			this.base = base;
			this.baseReset = StyleUtil.minus(StyleUtil.RESET_STYLE, base);
			this.styles = new ArrayList<>();
			this.text = text.copy();
			this.undo = new ArrayList<>();
			this.undo.add(text);
			undoPos = 0;
			genStyles(text, base, 0);
			setFormatter(str -> this.text);
			setChangeListener(str -> this.onChange.accept(this.text));
			onEdit("", 0, 0);
			generateLines();
		}
		
		public InternalTextFieldWidget setTextChangeListener(Consumer<Text> onChange) {
			this.onChange = onChange;
			return this;
		}
		
		private void genStyles(Text text, Style parent, int index) {
			int len = MVMisc.getContent(text).length();
			Style style = text.getStyle().withParent(parent);
			if (len > 0) {
				setStyle(index, style);
				index += len;
			}
			for (Text child : text.getSiblings()) {
				genStyles(child, style, index);
				index += MVMisc.stripInvalidChars(child.getString(), allowsNewLines()).length();
			}
		}
		
		private void setStyle(int index, Style style) {
			while (styles.size() <= index)
				styles.add(styles.size(), null);
			styles.set(index, style);
		}
		
		private Style getStyle(int index) {
			Style output = base;
			for (int i = 0; i <= index && i < styles.size(); i++) {
				Style style = styles.get(i);
				if (style != null)
					output = style;
			}
			return output;
		}
		
		private void applyFormatting(Formatting formatting) {
			int start = getSelStart();
			int end = getSelEnd();
			
			if (start == end) {
				if (cursorStyle == null)
					cursorStyle = getStyle(start == 0 ? 0 : start - 1);
				if (StyleUtil.hasFormatting(cursorStyle, formatting))
					cursorStyle = withoutFormatting(cursorStyle, formatting);
				else
					cursorStyle = withFormatting(cursorStyle, formatting);
				return;
			}
			
			Style startStyle = getStyle(start);
			Style endStyle = getStyle(end);
			
			boolean filled = StyleUtil.hasFormatting(startStyle, formatting);
			setStyle(start, withFormatting(startStyle, formatting));
			for (int i = start + 1; i < end && i < styles.size(); i++) {
				Style style = styles.get(i);
				if (style != null && !StyleUtil.hasFormatting(style, formatting)) {
					styles.set(i, withFormatting(style, formatting));
					filled = false;
				}
			}
			setStyle(end, endStyle);
			
			if (filled) {
				setStyle(start, withoutFormatting(startStyle, formatting));
				for (int i = start + 1; i < end && i < styles.size(); i++) {
					Style style = styles.get(i);
					if (style != null)
						styles.set(i, withoutFormatting(style, formatting));
				}
			}
			
			markUndo();
			onEdit("", start, 0);
			generateLines();
			onChange.accept(text);
		}
		private Style withFormatting(Style style, Formatting formatting) {
			if (formatting == Formatting.RESET)
				return baseReset;
			return style.withFormatting(formatting);
		}
		private Style withoutFormatting(Style style, Formatting formatting) {
			return StyleUtil.minusFormatting(style, StyleUtil.RESET_STYLE.withColor(base.getColor()), formatting);
		}
		
		private void applyStyleChange(UnaryOperator<Style> changer, boolean regenerateLines) {
			int start = getSelStart();
			int end = getSelEnd();
			
			if (start == end) {
				if (cursorStyle == null)
					cursorStyle = getStyle(start == 0 ? 0 : start - 1);
				cursorStyle = changer.apply(cursorStyle);
				return;
			}
			
			Style startStyle = getStyle(start);
			Style endStyle = getStyle(end);
			
			setStyle(start, changer.apply(startStyle));
			for (int i = start + 1; i < end && i < styles.size(); i++) {
				Style style = styles.get(i);
				if (style != null)
					styles.set(i, changer.apply(style));
			}
			setStyle(end, endStyle);
			
			markUndo();
			onEdit("", start, 0);
			if (regenerateLines)
				generateLines();
			onChange.accept(text);
		}
		
		private void applyColor(Formatting color, boolean shadow) {
			if (shadow) {
				int shadowColor = (MVMisc.scaleRgb(color.getColorValue(), 0.25) | 0xFF000000);
				applyStyleChange(style -> style.withShadowColor(shadowColor), true);
			} else
				applyFormatting(color);
		}
		
		public void setFormattedText(Text text) {
			styles.clear();
			genStyles(text, base, 0);
			ignoreNextEditStyles = true;
			setText(text.getString());
			ignoreNextEditStyles = false; // If onEdit doesn't get called since text is the same
		}
		public Text getFormattedText() {
			return text;
		}
		@Override
		public void setText(String text) {
			if (ignoreNextSetText) {
				ignoreNextSetText = false;
				return;
			}
			super.setText(text);
		}
		
		private Style getInitialCustomStyle() {
			return (getSelStart() == getSelEnd() ?
					(cursorStyle == null ? getStyle(getSelStart() == 0 ? 0 : getSelStart() - 1) : cursorStyle) :
						getStyle(getSelStart()));
		}
		private void showCustomColor(boolean shadow) {
			Style initialStyle = getInitialCustomStyle();
			int initialColor = (initialStyle.getColor() == null ?
					(base.getColor() == null ? -1 : base.getColor().getRgb()) : initialStyle.getColor().getRgb());
			if (shadow) {
				int initialShadow = (initialStyle.getShadowColor() == null ?
						(base.getShadowColor() == null ? MVMisc.scaleRgb(initialColor, 0.25) : base.getShadowColor()) : initialStyle.getShadowColor());
				InputOverlay.show(
						TextInst.translatable("nbteditor.formatted_text.custom_color.shadow"),
						new ColorSelectorWidget.ColorSelectorInput(initialShadow),
						rgb -> applyStyleChange(style -> style.withShadowColor(rgb | 0xFF000000), true));
			} else {
				InputOverlay.show(
						TextInst.translatable("nbteditor.formatted_text.custom_color"),
						new ColorSelectorWidget.ColorSelectorInput(initialColor),
						rgb -> applyStyleChange(style -> style.withColor(rgb), true));
			}
		}
		private void showEvents() {
			Style initialStyle = getInitialCustomStyle();
			OverlaySupportingScreen.setOverlayStatic(new EventEditorWidget(
					initialStyle.getClickEvent(), initialStyle.getHoverEvent(),
					(clickEvent, hoverEvent) -> applyStyleChange(
							style -> style.withClickEvent(clickEvent).withHoverEvent(hoverEvent), false)),
					200);
		}
		private void showInsertion() {
			Style initialStyle = getInitialCustomStyle();
			InputOverlay.show(
					TextInst.translatable("nbteditor.formatted_text.insertion"),
					StringInput.builder()
							.withDefault(initialStyle.getInsertion() == null ? "" : initialStyle.getInsertion())
							.build(),
					insertion -> applyStyleChange(style -> style.withInsertion(insertion.isEmpty() ? null : insertion), false));
		}
		private void showFont() {
			Style initialStyle = getInitialCustomStyle();
			InputOverlay.show(
					TextInst.translatable("nbteditor.formatted_text.font"),
					StringInput.builder()
							.withDefault(initialStyle.font == null ? "" : initialStyle.font.toString())
							.withValidator(font -> font.isEmpty() || IdentifierInst.isValid(font))
							.withSuggestions((str, cursor) -> {
								SuggestionsBuilder builder = new SuggestionsBuilder(str, 0);
								for (Identifier font : MainUtil.client.fontManager.fontStorages.keySet()) {
									String fontStr = font.toString();
									if (fontStr.startsWith(str))
										builder.suggest(fontStr);
								}
								return builder.buildFuture();
							})
							.build(),
					font -> applyStyleChange(style -> style.withFont(font.isEmpty() ? null : IdentifierInst.of(font)), true));
		}
		
		@Override
		protected void renderHighlightsBelow(MatrixStack matrices, int mouseX, int mouseY, float delta) {
			Style initialStyle = getStyle(0);
			int start = (initialStyle.getClickEvent() != null || initialStyle.getHoverEvent() != null || initialStyle.getInsertion() != null ? 0 : -1);
			for (int i = 0; i < styles.size(); i++) {
				Style style = styles.get(i);
				if (style == null)
					continue;
				if (style.getClickEvent() != null || style.getHoverEvent() != null || style.getInsertion() != null) {
					if (start == -1)
						start = i;
				} else if (start != -1) {
					renderHighlight(matrices, start, i, 0x55FFAA00);
					start = -1;
				}
			}
			if (start != -1)
				renderHighlight(matrices, start, getText().length(), 0x55FFAA00);
		}
		
		@Override
		public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
			if (super.keyPressed(keyCode, scanCode, modifiers))
				return true;
			
			if (Screen.hasControlDown() && !Screen.hasShiftDown()) {
				Formatting formatting = switch (keyCode) {
					case GLFW.GLFW_KEY_B -> Formatting.BOLD;
					case GLFW.GLFW_KEY_I -> Formatting.ITALIC;
					case GLFW.GLFW_KEY_U -> Formatting.UNDERLINE;
					case GLFW.GLFW_KEY_D -> Formatting.STRIKETHROUGH;
					case GLFW.GLFW_KEY_K -> Formatting.OBFUSCATED;
					case GLFW.GLFW_KEY_BACKSLASH -> Formatting.RESET;
					default -> null;
				};
				if (formatting != null) {
					applyFormatting(formatting);
					return true;
				}
			}
			
			if (Screen.hasControlDown() && Screen.hasShiftDown()) {
				switch (keyCode) {
					case GLFW.GLFW_KEY_C -> showCustomColor(hasShadowKeyDown());
					case GLFW.GLFW_KEY_E -> showEvents();
					case GLFW.GLFW_KEY_I -> showInsertion();
					case GLFW.GLFW_KEY_F -> showFont();
				}
			}
			
			return false;
		}
		
		@Override
		protected void onCursorMove(int cursor, int selStart, int selEnd) {
			if (getCursor() == cursor && getSelStart() == selStart && getSelEnd() == selEnd)
				return;
			cursorStyle = null;
		}
		
		@Override
		protected void onEdit(String insertedText, int pos, int overwrittenLen) {
			while (undoPos > 0) {
				undo.remove(0);
				undoPos--;
			}
			
			if (ignoreNextEditStyles)
				ignoreNextEditStyles = false;
			else if (overwrittenLen == 0) {
				Style style = getStyle(pos);
				setStyle(pos, style);
				for (int i = 0; i < insertedText.length(); i++)
					styles.add(pos, null);
				if (cursorStyle != null)
					setStyle(pos, cursorStyle);
				else if (pos == 0)
					setStyle(0, style);
			} else {
				Style startStyle = getStyle(pos);
				Style endStyle = null;
				for (int i = 0; i < overwrittenLen && pos < styles.size(); i++) {
					Style style = styles.remove(pos);
					if (style != null)
						endStyle = style;
				}
				if (endStyle != null && (pos >= styles.size() || styles.get(pos) == null))
					setStyle(pos, endStyle);
				if (pos < styles.size()) {
					for (int i = 0; i < insertedText.length(); i++)
						styles.add(pos, null);
				}
				if (!insertedText.isEmpty())
					setStyle(pos, startStyle);
			}
			
			String afterEdit = new StringBuilder(getText()).replace(pos, pos + overwrittenLen, insertedText).toString();
			EditableText text = TextInst.literal("");
			String part = "";
			Style style = !styles.isEmpty() && styles.get(0) != null ? styles.get(0) : base;
			for (int i = 0; i < afterEdit.length(); i++) {
				Style newStyle = (i == 0 || i >= styles.size() ? null : styles.get(i));
				if (newStyle != null) {
					if (newStyle.equals(style))
						styles.set(i, null);
					else {
						text.append(TextInst.literal(part).setStyle(style));
						part = "";
						style = newStyle;
					}
				}
				part += afterEdit.charAt(i);
			}
			if (!part.isEmpty())
				text.append(TextInst.literal(part).setStyle(style));
			undo.add(0, text);
			this.text = text;
			
			while (styles.size() > afterEdit.length())
				styles.remove(afterEdit.length());
		}
		
		@Override
		protected void onUndo(String newText) {
			if (undoPos < undo.size() - 1) {
				text = undo.get(++undoPos);
				styles.clear();
				genStyles(text, Style.EMPTY, 0);
			}
		}
		
		@Override
		protected void onRedo(String newText) {
			if (undoPos > 0) {
				text = undo.get(--undoPos);
				styles.clear();
				genStyles(text, Style.EMPTY, 0);
			}
		}
		
		@Override
		protected void onUndoDiscard() {
			while (undoPos > 0) {
				undo.remove(0);
				undoPos--;
			}
			undo.remove(0);
		}
		
		@Override
		protected String onCopy(String text, int pos, int len) {
			return TextInst.toJsonString(TextUtil.substring(this.text, pos, pos + len));
		}
		
		@Override
		protected String onPaste(String text, int pos, int overwrittenLen) {
			try {
				Text textValue = pasteFilter(TextUtil.fromJsonSafely(text));
				String textValueStr = textValue.getString();
				int textLen = textValueStr.length();
				
				Style endStyle = getStyle(pos);
				for (int i = 0; i < overwrittenLen && pos < styles.size(); i++) {
					Style style = styles.remove(pos);
					if (style != null)
						endStyle = style;
				}
				if (endStyle != null && (pos >= styles.size() || styles.get(pos) == null))
					setStyle(pos, endStyle);
				if (pos < styles.size()) {
					for (int i = 0; i < textLen; i++)
						styles.add(pos, null);
				}
				
				genStyles(textValue, Style.EMPTY, pos);
				ignoreNextEditStyles = true;
				
				return textValueStr;
			} catch (Exception e) {
				return text;
			}
		}
		private Text pasteFilter(Text toPaste) {
			toPaste = TextUtil.stripInvalidChars(toPaste, allowsNewLines());
			int numNewLines = getNumNewLines(getText());
			int toPasteNewLines = getNumNewLines(toPaste);
			while (numNewLines + toPasteNewLines + 1 > maxLines) {
				int i = TextUtil.lastIndexOf(toPaste, '\n');
				if (i == -1)
					break;
				toPaste = TextUtil.deleteCharAt(toPaste, i);
				toPasteNewLines--;
			}
			return toPaste;
		}
		private int getNumNewLines(Text text) {
			AtomicInteger output = new AtomicInteger(0);
			text.visit(str -> {
				int numNewLines = getNumNewLines(str);
				if (numNewLines != 0)
					output.setPlain(output.getPlain() + numNewLines);
				return Optional.empty();
			});
			return output.getPlain();
		}
	}
	
	public static FormattedTextFieldWidget create(FormattedTextFieldWidget prev, int x, int y, int width, int height,
			Text text, boolean newLines, Style base, Consumer<Text> onChange) {
		if (prev == null)
			return new FormattedTextFieldWidget(x, y, width, height, text, newLines, base, onChange);
		prev.x = x;
		prev.y = y;
		prev.width = width;
		prev.height = height;
		prev.field = InternalTextFieldWidget.create(prev.field, x, y + 24, width, height - 24, text, newLines, base, onChange);
		prev.clearWidgets();
		prev.init();
		prev.addElement(prev.field);
		prev.addTickable(prev.field);
		prev.setText(text);
		prev.setChangeListener(onChange);
		if (prev.isMultiFocused())
			prev.setMultiFocused(false);
		return prev;
	}
	public static FormattedTextFieldWidget create(FormattedTextFieldWidget prev, int x, int y, int width, int height,
			List<Text> lines, Style base, Consumer<List<Text>> onChange) {
		return create(prev, x, y, width, height, TextUtil.joinLines(lines), true, base, text -> onChange.accept(TextUtil.splitText(text)));
	}
	
	private static boolean hasShadowKeyDown() {
		return StyleUtil.SHADOW_COLOR_EXISTS && Screen.hasAltDown();
	}
	
	private int x;
	private int y;
	private int width;
	private int height;
	private InternalTextFieldWidget field;
	private ButtonDropdownWidget colors;
	private ButtonWidget font;
	private int lastFont;
	private long lastFontChange;
	
	protected FormattedTextFieldWidget(int x, int y, int width, int height, Text text, boolean newLines, Style base, Consumer<Text> onChange) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.field = InternalTextFieldWidget.create(null, x, y + 24, width, height - 24, text, newLines, base, onChange);
		init();
		addElement(field);
		addTickable(field);
	}
	private void init() {
		if (width < 16 * 20 + (ConfigScreen.isHideFormatButtons() ? 0 : 20 + 4 + 5 * 20 + (4 + 20) * 2)) {
			colors = addElement(new ButtonDropdownWidget(x, y, 20, 20, TextInst.literal("⬛").formatted(Formatting.AQUA), 20, 20));
			for (Formatting formatting : Formatting.values()) {
				if (!formatting.isColor())
					break;
				colors.addButton(TextInst.literal("⬛").formatted(formatting), btn -> {
					field.applyColor(formatting, hasShadowKeyDown());
					colors.setOpen(false);
				}, createColorButtonTooltip(formatting));
			}
			colors.build();
		} else {
			colors = null;
			int i = 0;
			for (Formatting formatting : Formatting.values()) {
				if (!formatting.isColor())
					break;
				addWidget(MVMisc.newButton(x + i * 20, y, 20, 20, TextInst.literal("⬛").formatted(formatting),
						btn -> field.applyColor(formatting, hasShadowKeyDown()), createColorButtonTooltip(formatting)));
				i++;
			}
		}
		
		if (!ConfigScreen.isHideFormatButtons()) {
			int afterColorsX = x + (colors == null ? 16 * 20 : 20);
			
			int i = 0;
			for (Formatting formatting : new Formatting[] {Formatting.BOLD, Formatting.ITALIC, Formatting.UNDERLINE,
					Formatting.STRIKETHROUGH, Formatting.OBFUSCATED, Formatting.RESET}) {
				Text btnText;
				MVTooltip btnTooltip;
				if (formatting == Formatting.RESET) {
					btnText = TextInst.of("");
					if (ConfigScreen.isKeybindsHidden())
						btnTooltip = new MVTooltip(TextInst.of(formatting.getName()));
					else {
						btnTooltip = new MVTooltip(
								TextInst.of(formatting.getName()),
								TextInst.translatable("nbteditor.keybind.formatted_text.reset"));
					}
				} else {
					btnText = TextInst.literal(formatting.name().substring(0, 1)).formatted(formatting);
					btnTooltip = new MVTooltip(TextInst.of(formatting.getName()));
				}
				addWidget(MVMisc.newButton(
						afterColorsX + 24 + i * 20 + (formatting == Formatting.RESET ? 4 + 20 * 3 + 4 : 0), y, 20, 20,
						btnText, btn -> field.applyFormatting(formatting), btnTooltip));
				i++;
			}
			
			addWidget(MVMisc.newButton(afterColorsX, y, 20, 20,
					TextInst.literal("⬛").setStyle(Style.EMPTY.withColor(0x9999C0).withFormatting(Formatting.ITALIC)),
					btn -> field.showCustomColor(hasShadowKeyDown()),
					createFormatButtonTooltip("custom_color", true)));
			
			addWidget(MVMisc.newButton(afterColorsX + 24 + 5 * 20 + 4, y, 20, 20,
					TextInst.literal("E"),
					btn -> field.showEvents(),
					createFormatButtonTooltip("events", false)));
			addWidget(MVMisc.newButton(afterColorsX + 24 + 5 * 20 + 4 + 20, y, 20, 20,
					TextInst.literal("I"),
					btn -> field.showInsertion(),
					createFormatButtonTooltip("insertion", false)));
			font = addWidget(MVMisc.newButton(afterColorsX + 24 + 5 * 20 + 4 + 20 * 2, y, 20, 20,
					TextInst.literal("F"), // Gets replaced before rendering
					btn -> field.showFont(),
					createFormatButtonTooltip("font", false)));
		}
	}
	private MVTooltip createColorButtonTooltip(Formatting color) {
		Text name = TextInst.of(color.getName());
		if (ConfigScreen.isKeybindsHidden() || !StyleUtil.SHADOW_COLOR_EXISTS)
			return new MVTooltip(name);
		return new MVTooltip(name, TextInst.translatable("nbteditor.keybind.formatted_text.shadow"));
	}
	private MVTooltip createFormatButtonTooltip(String name, boolean color) {
		String nameKey = "nbteditor.formatted_text." + name;
		if (ConfigScreen.isKeybindsHidden())
			return new MVTooltip(nameKey);
		String keybindKey = "nbteditor.keybind.formatted_text." + name;
		if (color && StyleUtil.SHADOW_COLOR_EXISTS)
			return new MVTooltip(nameKey, keybindKey, "nbteditor.keybind.formatted_text.shadow");
		return new MVTooltip(nameKey, keybindKey);
	}
	
	public FormattedTextFieldWidget setChangeListener(Consumer<Text> onChange) {
		field.setTextChangeListener(onChange);
		return this;
	}
	public FormattedTextFieldWidget setMaxLines(int maxLines) {
		field.setMaxLines(maxLines);
		return this;
	}
	public FormattedTextFieldWidget setBackgroundColor(int bgColor) {
		field.setBackgroundColor(bgColor);
		return this;
	}
	public FormattedTextFieldWidget setCursorColor(int cursorColor) {
		field.setCursorColor(cursorColor);
		return this;
	}
	public FormattedTextFieldWidget setSelectionColor(int selColor) {
		field.setSelectionColor(selColor);
		return this;
	}
	public FormattedTextFieldWidget setShadow(boolean shadow) {
		field.setShadow(shadow);
		return this;
	}
	public FormattedTextFieldWidget setOverscroll(boolean overscroll) {
		field.setOverscroll(overscroll);
		return this;
	}
	
	public FormattedTextFieldWidget suggest(Screen screen, BiFunction<String, Integer, CompletableFuture<Suggestions>> suggestions) {
		field.suggest(screen, suggestions);
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
	
	public void setText(Text text) {
		field.setFormattedText(text);
	}
	public void setText(List<Text> lines) {
		setText(TextUtil.joinLines(lines));
	}
	public Text getText() {
		return field.getFormattedText();
	}
	
	public List<Text> getTextLines() {
		return TextUtil.splitText(getText());
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		setFocused(isMultiFocused() ? field : null);
		field.render(matrices, mouseX, mouseY, delta);
		
		if (colors != null) {
			matrices.push();
			matrices.translate(0.0, 0.0, 1.0);
			colors.render(matrices, mouseX, mouseY, delta);
			matrices.pop();
		}
		
		if (font != null) {
			long time = System.currentTimeMillis();
			if (lastFontChange < time - 200) {
				lastFontChange = time;
				lastFont += Math.floor(Math.random() * 2) + 1;
				font.setMessage(TextInst.literal(lastFont % 3 + "")
						.styled(style -> style.withFont(IdentifierInst.of("nbteditor", "fancy_f"))));
			}
		}
		
		super.render(matrices, mouseX, mouseY, delta);
	}
	
	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
	}
	
}
